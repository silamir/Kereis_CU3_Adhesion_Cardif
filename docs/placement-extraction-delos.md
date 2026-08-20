# Où déposer l'extraction Delos dans le DMS

Question posée par la simulation des données : un JSON d'extraction par document, puis **où** ce JSON
se range dans le endpoint officiel du DMS. Le schéma en offre deux emplacements possibles, et le choix
n'est pas neutre.

## Emplacement retenu — `data[].metadata`

```
GET /private/secure/input/documents
└── data[]
    └── metadata            ← objet libre
        └── delos           ← l'extraction, sous une clé de producteur
            ├── documentUuid, documentType
            ├── extraction { engine, engineVersion, extractedAt, pageCount,
            │                handwritten, globalConfidence }
            ├── fields { … arborescence du contrat de champs … }
            └── anomalies[]
```

Trois raisons.

1. **C'est le seul objet réellement libre du schéma.** Sur les onze objets qui apparaissaient vides
   dans le panneau Swagger, dix ont en fait une structure imposée (`indexation.nature` vaut
   `{id, name}`, `insured` vaut `{firstName, lastName}`, etc.). `metadata` est le seul sans forme
   contrainte, donc le seul qui puisse accueillir une charge utile propriétaire.
2. **`metadata` est renvoyé par le listing.** L'extraction arrive dans le même appel que le scan :
   un lot de cinq pièces se lit en une requête. C'est ce qui rend la réponse 200 utilisable comme
   entrée unique du programme, conformément au cadrage.
3. **La clé `delos` isole le producteur.** `metadata` est partagé : préfixer évite la collision avec
   d'autres applications qui y écriraient.

## Alternative écartée — `content/extractedData`

Le DMS expose `GET` et `PUT /private/secure/input/documents/{documentUuid}/content/extractedData`,
« Getting document extracted data (json format) ». Malgré le nom, ce n'est pas le bon canal ici :

- sa description précise qu'il est **« currently only used to store data extracted from depreciation
  tables »** — un usage déjà attribué, étranger à l'adhésion ;
- le corps est typé **`string`**, pas objet : le JSON y serait opaque, sans validation possible côté
  contrat ;
- il **n'est pas renvoyé par le listing**. Il faudrait un appel supplémentaire par document, soit
  cinq requêtes par lot au lieu de zéro, et la réponse 200 cesserait d'être une entrée suffisante.

À reconsidérer seulement si l'équipe DMS réserve `metadata` à un autre usage.

## Ce qui reste à obtenir

**La convention de nommage des clés de `metadata` est propre à Kereis et n'est pas documentée.** La
clé `delos` et l'arborescence sous `fields` sont donc *notre* proposition, pas un contrat validé.

Conséquence de conception, déjà appliquée : cette convention est **isolée dans
`DmsLotRepository`**, seule classe du module qui connaisse la forme du DMS. Quand la convention réelle
sera connue, une seule classe changera — le domaine et les cas d'usage ne bougeront pas.

Deux points à trancher avec l'équipe DMS :

1. La clé de premier niveau sous `metadata` (`delos` ? `extraction` ? un préfixe applicatif imposé ?).
2. Qui écrit `metadata` : Delos directement, ou le DMS lors de l'intégration de l'extraction ? Cela
   détermine si le module peut se contenter de lire.


## Réponse à la question : tout d'un coup, ou par UUID ?

La question se posait entre récupérer l'extraction dans le `metadata` du listing, ou passer par
l'UUID de chaque document pour aller chercher le « content ». Le schéma tranche, et écarte la
seconde branche.

### `content` n'est pas un candidat

`GET /private/secure/input/documents/{documentUuid}/content` **renvoie une URL S3 pré-signée vers le
PDF**, pas des données structurées. C'est le fichier, pas son extraction. Le seul endpoint par UUID
qui renvoie du JSON extrait est `content/extractedData`, et il reste écarté pour les raisons
ci-dessus : corps typé `string`, usage déjà attribué aux tableaux d'amortissement, absent du listing.

### Le listing suffit, à trois conditions

`metadata` est une **propriété** du document, pas une sous-ressource : il est renvoyé par défaut.
L'extraction Delos arrive donc dans l'appel de scan, sans requête supplémentaire. Mais trois
paramètres sont à poser, et deux d'entre eux sont des pièges silencieux.

| Paramètre | Pourquoi |
|---|---|
| **`extension=reception`** | `reception` **est** une sous-ressource, et le service n'en renvoie aucune par défaut. Sans ce paramètre, `reception.id` est absent — et c'est l'identifiant de lot. Le regroupement échoue sans erreur. |
| **`limit`** | Vaut **20 par défaut**. Un lot de cinq pièces passe, mais un scan large est tronqué sans avertissement. À poser explicitement. |
| **Filtres serveur** | `processingStateIdList`, `withoutReference`, `domainIdList`, `natureIdList`, `receptionUuidList` : le périmètre d'écoute se filtre côté DMS. Les documents hors périmètre ne traversent jamais le réseau. |

`withoutReference` mérite d'être relevé : il exprime directement le second critère du verrou
anti-doublon — un document sans référence de dossier. Ce que la conception plaçait dans le module est
donc obtenable comme filtre de requête.

### Conclusion

Un seul appel par cycle de scan, avec `extension=reception`, un `limit` explicite et les filtres
d'état et de rattachement. Pas d'appel par document. Les sous-ressources supplémentaires
(`events`, `associations`, `transfers`) restent disponibles à la demande, si un contrôle en a besoin :
`extension` est une liste.

Valeurs possibles de `extension` sur ce endpoint : `reception`, `archiving_requests`, `associations`,
`transfers`, `events`, `content`.
