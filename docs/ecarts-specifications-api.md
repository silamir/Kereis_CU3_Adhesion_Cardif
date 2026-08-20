# Ecarts entre les specifications CU#3 et les API reelles

Releves en confrontant les specifications fonctionnelles aux trois contrats OpenAPI livres. Chacun
demande un arbitrage avant que le code correspondant ne soit ecrit : ce sont les ecarts qui coutent
cher s'ils se decouvrent en recette.

---

## 1. `PENDING_AUTOMATION` n'existe pas — bloquant

Les specifications font demarrer le module sur un scan des documents en statut
`pending_automation`. Ce statut **n'existe pas dans le DMS**. L'enumeration reelle
(`InputDocumentProcessingStateId`) est :

```
READY · PENDING_INDEXATION · PENDING_PROCESSING · BOUND · MANIPULATED
ARCHIVED · PROCESSED · DISCARDED · REJECTED
```

Le mot « automation » n'apparait pas une seule fois dans les 16 963 lignes de la specification DMS.

**Consequence.** Le critere d'ecoute du module n'a pas de contrepartie dans l'API. `READY` est le
candidat le plus plausible, mais c'est une deduction, pas une donnee.

**Provisoire.** `cu3.dms.etats-ecoutes` dans `application.yaml`, positionne a `READY`, modifiable
sans recompiler.

**A trancher avec** l'equipe DMS et le metier : quel statut marque un lot qualifie et pret pour le
traitement automatise ?

*Point connexe confirme* : `PENDING_PROCESSING` existe bien, donc le renvoi d'un lot au gestionnaire
par changement de statut reste valide.

---

## 2. La convention de cles de `metadata` n'est pas documentee — bloquant

L'extraction Delos doit se ranger dans `data[].metadata`, seul objet reellement libre du schema DMS
(les dix autres qui paraissaient vides ont en fait une structure imposee). Mais la convention de
nommage des cles est propre a Kereis et n'est ecrite nulle part.

**Provisoire.** L'extraction est deposee sous `metadata.delos`, avec l'arborescence du contrat de
champs. C'est *notre* proposition.

**Mitigation deja en place.** Cette convention est isolee dans `DmsLotRepository`, seule classe du
module qui connaisse la forme du DMS. Quand la convention reelle sera connue, une seule classe
changera : ni le domaine, ni les cas d'usage.

**A trancher** : la cle de premier niveau, et qui ecrit `metadata` — Delos directement, ou le DMS
lors de l'integration de l'extraction.

Detail complet, dont l'alternative `content/extractedData` et pourquoi elle est ecartee :
[`placement-extraction-delos.md`](placement-extraction-delos.md).

---

## 3. Trois des cinq developpements Tahore attendus sont absents — important

| Attendu | Etat reel |
|---|---|
| **NEW 1** — `POST .../actes` | **Existe** : `POST /private/open/api/adhesions/{adhesionId}/acte`, au singulier |
| **NEW 2** — champ `codeEmissaire` | **Absent.** Le lien vers le document DMS passe apparemment par `courriersRecus[].partName` a l'aller et `ActeReponse.courriersRecus[].document.id` au retour — ce dernier decrit comme « Represente le document GED » |
| **NEW 3** — `calculerAttendus` | **Absent, et le modele est inverse** : `courriersAttendus` est un champ de la *requete* `ActeRequete`. C'est l'appelant qui declare les attendus, pas Tahore qui les calcule |
| **NEW 4** — option de visa sur `valider` | `avenantEnCours/valider` existe, mais **aucune notion de visa** |
| **NEW 5** — `demandeDeRens` | **Absent** |

**Le point le plus structurant est NEW 3** : si les attendus sont declares par le module, c'est lui
qui porte la regle de determination des pieces attendues. La conception supposait l'inverse.

---

## 4. `conformite` est un faux ami — a ne pas confondre

`GET /api/adhesions/{adhesionId}/conformite` ne renvoie **pas** la recevabilite documentaire, malgre
son nom. Il renvoie des indicateurs reglementaires : `TER`, `BLA`, `PPE`, `VR1`, `VR2`, `VIP`,
`PREMIUM` — lutte contre le blanchiment, vigilance renforcee, personne sensible.

**Consequence.** La contradiction relevee dans les specifications entre RG-2.2.6 (les controles de
coherence sont dans Tahore) et RG-2.2.7 (le module transmet le verdict de recevabilite) **reste
ouverte** : aucun endpoint n'y repond.

---

## Ce que les contrats ont en revanche resolu

Quatre points ouverts se ferment, sans arbitrage necessaire.

| Point | Reponse |
|---|---|
| Structure de `indexation.nature` | `{id: integer, name: string}` — et cote Tahore, `nature: 1` vaut « bulletin d'adhesion » |
| Enumeration des evenements | 60 valeurs de `events[].id`, dont `DOCUMENT_BOUND`, `DOCUMENT_CLOSED`, `AUTOMATIC_CASE_INDEXATION` |
| Types d'association | `MERGING`, `CLONING`, `EXTRACTION`, `REPLY`, `CONVERSION`, `REPATRIATION` |
| SLA | `sla` porte `{deadlineDate, urgencyDate, priority}`, `priority` valant `URGENT`, `NON_URGENT`, `NO_SLA` ou `OVERDUE` — directement exploitable pour les 48 heures |

Et deux pieges de requete, qui auraient echoue silencieusement :

- **`extension=reception` est obligatoire.** `reception` est une sous-ressource, non renvoyee par
  defaut : sans ce parametre, `reception.id` est absent, donc l'identifiant de lot aussi.
- **`limit` vaut 20 par defaut**, ce qui tronque un scan large sans avertissement.

Bonne nouvelle au passage : **`withoutReference`** est un filtre serveur qui exprime directement le
second critere du verrou anti-doublon.
