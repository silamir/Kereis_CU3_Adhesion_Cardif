# Contrats d'API

Source de verite des interfaces du module. Toute modification vient d'une livraison DSI, jamais
d'une edition locale.

| Fichier | Systeme | Chemins | Operations | Schemas |
|---|---|---|---|---|
| `01-Document-Management-Services.yaml` | DMS 3.13.18 | 163 | 190 | 270 |
| `02-API-DSI-SOLUTIONS-PREVOYANCE.yaml` | Tahore — adhesions, avenants, personnes, produits | 16 | 17 | 50 |
| `03-Tahore-Actes-Decisions.yaml` | Tahore — actes et decisions | 4 | 4 | 17 |
| `DMS_GET_input_documents_200.json` | Reponse 200 de reference, entree du programme | — | — | — |

Rendu lisible : [`../docs/api/index.html`](../docs/api/index.html).

---

## Tri des doublons

Analyse menee sur les trois specifications : doublons de fichier, chemins partages, schemas
homonymes, operations depreciees, schemas orphelins.

### Supprime

| Element | Raison |
|---|---|
| `03-Tahore-Actes-Decisions.source.json` | Contenu **strictement identique** au `.yaml` (seul le titre differe). Le titre d'origine, « API », est conserve dans le YAML sous `info.x-titre-origine` : le JSON n'apportait plus rien. |
| `00-INDEX.txt` | Remplace par ce README, et par le rendu HTML. |

Pour rappel, le fichier source `APIs_Swaggers_DMS_Prev_Tahore.txt` contenait deja un bloc en doublon
strict — la specification Prevoyance y figurait deux fois. Elle n'a ete conservee qu'une fois.

### 02 et 03 decrivent le meme espace d'URL

Aucun chemin n'est partage en apparence, mais c'est un artefact de presentation : les serveurs de
`02` incluent deja `/private/open`, alors que `03` porte ce prefixe dans ses chemins.

```
02 : serveur https://fare-ng-internal-i1.cbp-dev.com/private/open
     chemin  /api/adhesions/{adhesionId}/avenantEnCours/valider
     => /private/open/api/adhesions/{adhesionId}/avenantEnCours/valider

03 : serveur http://localhost:8082/tahore
     chemin  /private/open/api/adhesions/{adhesionId}/avenantEnCours/prendreDecision
```

Une fois resolus, les deux visent la **meme surface d'API Tahore**. Ce ne sont pas deux systemes,
mais **deux specifications partielles de la meme application** : `02` couvre le cycle de vie de
l'adhesion, `03` les actes et decisions. Les operations sont complementaires, pas redondantes.

**A demander a la DSI** : existe-t-il une specification unifiee ? A defaut, laquelle fait foi quand
les deux decrivent un meme objet, et quelle est l'URL d'environnement de `03` — son serveur declare
est `localhost`, donc une spec de developpement.

### Collision de noms a la generation

Trois schemas portent le meme nom dans `02` et `03`, et **deux ont un contenu different** :

| Schema | Etat |
|---|---|
| `ProduitRequete` | Contenu identique — sans consequence |
| `AssureRequete` | **Contenus differents** |
| `ErreurClientReponse` | **Contenus differents** |

Consequence pour le build : `02` et `03` ne doivent **jamais etre generes dans le meme package
Java**, sinon deux classes distinctes se disputent le meme nom. Le `pom.xml` genere aujourd'hui `01`
dans `…infrastructure.dms.generated` et `03` dans `…infrastructure.tahore.generated`. Si `02` est
ajoute, lui donner son propre package.

### 32 operations depreciees dans le DMS

Le DMS marque 32 operations `deprecated`. Le module ne doit en appeler aucune. Trois concernent
directement notre conception, et confirment les choix retenus :

| Operation depreciee | Ce que cela confirme |
|---|---|
| `POST /private/secure/input/receptions` | Le lot ne se cree pas. Il se lit, en regroupant par `reception.id`. |
| `GET /private/secure/input/receptions` | Idem : pas de parcours par lot, on part des documents. |
| `GET /private/secure/input/documents/{documentUuid}/reception` | Confirme l'usage de `extension=reception` sur le listing plutot qu'un appel par document. |

Sont egalement depreciees toutes les operations de `comments` et de `versions`, ainsi que
`output/documents/lock` et `unlock` — coherent avec l'abandon du verrouillage de lot.

### Points mineurs, sans action

- **2 schemas orphelins** dans le DMS (`CreationGroup`, `ExtendedReception`) : definis, jamais
  references. Le generateur produira deux classes inutiles.
- **10 groupes de schemas au contenu identique** dans le DMS, du type
  `InputPermissionsProperties == OutputPermissionsProperties` ou `InputDocumentId == OutputDocumentId`.
  C'est la symetrie entree/sortie de l'API, pas un defaut.
- **2 groupes dans `03`** : `AssureRequete == ProduitRequete` (tous deux `{id}`) et
  `GarantieReponse == PriseDecisionReponse` (tous deux `{id, libelle}`). Modelisation sommaire cote
  source ; sans effet, mais le typage n'apporte aucune garantie.
- `GET /supervision` apparait dans `01` et `02` : endpoints de supervision propres a chaque service,
  sur des hotes differents. Pas un doublon.

---

## Regenerer le rendu HTML

Les pages de `docs/api/` sont derivees de ces YAML. Apres toute mise a jour d'un contrat, les
regenerer pour qu'elles ne divergent pas.
