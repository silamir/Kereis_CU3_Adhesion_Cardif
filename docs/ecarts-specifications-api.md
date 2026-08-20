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

---

## 5. Nature des retours Delos — levé

**Question posée** : le classeur classait D10 et le QSS en « contrôle » à 100 %, ce qui laissait
craindre que Delos n'en extraie rien.

**Réponse obtenue.** La colonne décrit le **type de ce que Delos renvoie**, pas sa présence :

| Mention au classeur | Ce que Delos renvoie | Nombre |
|---|---|---|
| « À extraire » | la **valeur brute**, du type déclaré | 49 |
| « À vérifier / contrôle » | un **booléen** : la condition du libellé est remplie, ou non | 29 |
| « — ne pas extraire — » | rien ; le module calcule (cohérence RIB / mandat SEPA) | 1 |

Delos renvoie donc bien les 78 champs. L'objectif d'automatisation est atteignable : `allReponsesNegative`
arrive comme booléen, et suffit à statuer sur la recevabilité du QSS.

**Appliqué.** `ContratChamps.NatureRetour` porte la distinction, `typeEffectif()` rend `boolean` pour
une condition, et les fixtures émettent des booléens pour les 29 champs concernés. Deux scénarios
exercent une condition non remplie : `07-qss-avec-reponse-positive` et `08-consentement-refuse`.

**Effet de bord favorable sur le cloisonnement des données de santé.** Le QSS ne renvoie que des
booléens de synthèse — le détail des réponses ne traverse jamais le module. C'est exactement ce
qu'exige la constitution.

---

## 5 bis. Deux champs du QSS sont annoncés booléens tout en portant une énumération — à trancher

| Champ | Énumération déclarée | Problème |
|---|---|---|
| `QSS type` | `TYPE_QUESTIONNAIRE : QSS_SIMPLIFIE \| QS_COMPLET` | Un booléen ne peut pas porter `QSS_SIMPLIFIE`. Or le module doit savoir s'il traite un QSS ou un QS complet pour choisir la route de sortie. |
| `QSS emetteur` | `EMETTEUR_QUESTIONNAIRE : CONSEILLER_BNP \| ASSURE` | Même problème, et **RG-2.2.5 a besoin de l'émetteur réel** : un questionnaire complet émis par le conseiller est non recevable. Un booléen « émetteur conforme » embarquerait la règle dans Delos. |

Ces deux champs devraient être des **valeurs**, pas des conditions. `ContratChamps.contradictions()`
les remonte, et `ContratChampsTest` les fixe pour que le jour où le classeur est corrigé, le test le
signale.

**Question connexe, du même ordre.** Quatorze champs de condition ont un format déclaré non booléen,
dont cinq dates : `dateSignature` du recueil, du QSS et du mandat SEPA. Si Delos renvoie un booléen
pour une date, alors la règle « signature de moins de six mois » (fiche F06) **vit dans Delos**, et le
module ne peut plus ni la recalculer ni changer le seuil. À confirmer : est-ce le choix voulu, ou ces
dates doivent-elles arriver comme valeurs ?

---

## 6. Trois noms de champ ne sont pas uniques au classeur — corrige par defaut

Le classeur porte, a l'interieur du seul document D09, trois noms de variable utilises deux fois :

| Nom au classeur | Ligne | Sens | Ligne | Sens |
|---|---|---|---|---|
| `dateNaissance` | 13 | date de naissance de l'assure | 44 | date de naissance du beneficiaire |
| `commune` | 14 | lieu de naissance | 20 | localite de l'adresse |
| `pays` | 16 | pays de naissance | 18 | pays de l'adresse |

Dans un `metadata` a plat, la seconde occurrence ecrase la premiere : la date de naissance du
beneficiaire remplacerait celle de l'assure, silencieusement.

**Resolution appliquee.** Desambiguisation alignee sur le fichier de reference Delos, qui avait deja
traite ces trois cas : `beneficiaireDateNaissance`, `communeNaissance`, `paysNaissance`. Le choix est
coherent avec les champs beneficiaire deja nommes ainsi au classeur (`beneficiaireNom`,
`beneficiairePrenom`). Chaque renommage est trace dans `contracts/delos-contrat-champs.json` sous
`renommeParDefaut`, avec sa raison et le statut « A VALIDER PAR LE METIER ».

**A confirmer** via la colonne « Nom valide » du classeur. `ContratChampsTest` echoue si une
collision reapparait.
