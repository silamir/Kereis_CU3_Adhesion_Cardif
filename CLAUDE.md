# Module externe CU#3 — Adhésion Prévoyance Cardif

Ce dépôt construit **`tahore-document-processing`** — aussi écrit `Tahore_document_processing` côté
client, c'est le même composant, désigné dans les spécifications sous le nom de « module externe ».
Il automatise la création des dossiers d'adhésion Prévoyance Cardif chez Kereis : il lit les lots
documentaires qualifiés par le DMS, contrôle leur complétude et leur éligibilité, crée le dossier dans
Tahoré et déclenche la sortie qui convient — certificat d'adhésion, demande de renseignements, reprise
manuelle ou fiche d'action.

Les règles de gestion (RG) et critères d'acceptation (CA) cités partout dans ce fichier renvoient aux
spécifications fonctionnelles v4. **Ne jamais inventer une règle : si elle n'est pas référencée, elle
n'est pas arbitrée.**

<!-- SPECKIT START -->
<!-- Bloc géré par l'extension agent-context de Spec Kit (`/speckit.agent-context.update`).
     Son contenu est régénéré à chaque plan : ne rien écrire d'important ici.
     Tout le reste du fichier est préservé. -->
<!-- SPECKIT END -->

## Où commence et où s'arrête ce module

**Point d'entrée.** Le module démarre **après** la qualification du lot par le DMS. Il scanne
régulièrement les statuts du DMS et ne prend que les documents de son domaine en `pending_automation`
**et** sans n° de dossier (RG-2.1.2, RG-3.5.1). Ce double critère est le seul verrou anti-doublon.

**Hors périmètre** — décrit ici parce que cela fixe les données disponibles, mais **à ne pas construire** :

| Amont | Porté par |
|---|---|
| Réception mail / courrier / espace adhésion vers le DMS | DMS |
| Séparation des pièces, typage, association au label d'adhésion | DMS |
| Qualification « Adhésion Cardif » sur détection du bulletin d'adhésion | DMS |
| Envoi du lot à Delos, extraction, scores de confiance, retour JSON | Delos (CU#1) |
| Intégration des JSON dans les métadonnées et passage en `pending_automation` | DMS |
| Contrôles de cohérence entre pièces et statuage de recevabilité | Tahoré (moteur de recevabilité) |
| Émission des courriers sortants, routage, relances, classement sans suite | Tahoré (comm. sortantes internes) |

**Dans le périmètre** : contrôle de complétude et d'éligibilité avant création, création et pré-saisie
dans Tahoré, gestion des états DMS, création de l'acte des courriers reçus et calcul des attendus,
aiguillage des quatre routes de sortie, traitement des compléments de dossier.

## Architecture cible — Spring Boot, hexagonale

Le CU#3 est construit en **Spring Boot avec une architecture hexagonale**. Ce choix n'est pas décoratif
ici : le module est presque entièrement fait d'adaptateurs vers des systèmes tiers dont les contrats ne
sont pas encore stables. Le domaine doit pouvoir être écrit, testé et validé métier **avant** que les
payloads Tahoré soient connus.

**Le domaine** ne contient que ce qui est vrai indépendamment de toute technique : le lot et ses pièces,
les règles de complétude, d'éligibilité, de complexité, de cohérence, le calcul de la route de sortie,
la liste des motifs de demande de renseignements. Aucune annotation Spring, aucun type HTTP, aucun DTO
d'API tierce ne franchit cette frontière.

**Les ports pilotes** (ce qui actionne le domaine) : le déclenchement d'un cycle de traitement. Un seul
adaptateur pilote en pratique — l'ordonnanceur qui déclenche le scan périodique. Le module n'expose
aucune API entrante : rien ne l'appelle, il va chercher son travail.

**Les ports pilotés** (ce dont le domaine a besoin) — un port par capacité métier, jamais un port par
système tiers :

| Port | Responsabilité | Adaptateur |
|---|---|---|
| `LotRepository` | trouver les lots à traiter, lire leurs pièces et leurs données extraites | DMS `GET /input/documents` |
| `EtatDocumentPort` | écrire un statut, une référence de dossier, un rattachement, une trace | DMS `PUT`, `bind` |
| `DossierPort` | créer personnes, adhésion, avenant ; pré-saisir | Tahoré `personnes` / `adhesions` / `avenants` |
| `ActePort` | attacher les courriers reçus, calculer les attendus | Tahoré `actes` / `calculerAttendus` (NEW) |
| `DecisionPort` | prendre décision, valider, demander des renseignements | Tahoré `avenantEnCours/*` (NEW) |
| `ReferentielProduitPort` | résoudre ids de garanties et code formule | `referentiel-produit` |
| `ActionManuellePort` | émettre une fiche d'action manuelle | à définir |

**Ce que la frontière achète concrètement ici.** Les cinq développements NEW côté Tahoré n'existent pas
encore : `ActePort` et `DecisionPort` sont donc implémentés par des adaptateurs bouchonnés, et le domaine
est complet et testé sans eux. La structure de `metadata` (les données extraites) n'est pas connue : le
mapping vit dans l'adaptateur DMS, et le jour où la convention Kereis est communiquée, seul cet
adaptateur change. Les contrôles de cohérence sont portés par Tahoré (RG-2.2.6) : le domaine consomme un
verdict via un port, il ne le recalcule pas.

**Tests.** Les règles de gestion se testent sur le domaine seul, sans Spring, sans conteneur, sans
réseau : chaque critère d'acceptation (CA) devient un test de domaine. Les adaptateurs se testent
séparément par des tests de contrat contre les schémas d'API réels. Un test qui a besoin de démarrer le
contexte Spring pour vérifier une règle métier signale une fuite du domaine vers l'infrastructure.

**Le piège à éviter.** L'erreur classique est de calquer les ports sur les systèmes (`DmsPort`,
`TahorePort`) : la frontière n'achète alors plus rien, puisque la forme du tiers a traversé. Les ports
ci-dessus sont nommés par le besoin du domaine, pas par le fournisseur.


**Outillage autour du socle.** Le détail de la stack à installer autour de Spring Boot (génération de code, ArchUnit, WireMock, tests dérivés des 80 assertions métier) et son ordre d’installation sont dans `docs/stack-technique.md`. Ces choix sont dictés par deux contraintes de projet : un budget IA serré et une disponibilité métier rare.

## Stack confirmée (accord du 20/08/2026)

Développement sur l'environnement Silamir, puis intégration chez le client.

| Élément | Valeur | Contrainte à connaître |
|---|---|---|
| Langage | **Java 25** | Spring Boot 4 exige Java 21 au minimum (Jakarta EE 11) : Java 25 est valide. |
| Framework | **Spring Boot 4.0.7** | Version majeure. Repose sur Spring Framework 7, Spring Security 7, Hibernate 7.1. |
| Sérialisation | **Jackson 3** | Piège principal de Boot 4 : les packages passent de `com.fasterxml.jackson` à `tools.jackson`, sauf `jackson-annotations`. Deux valeurs par défaut de sérialisation changent **sans erreur de compilation** — dates et `BigDecimal`. Tout DTO exposé doit être couvert par un test de forme. |
| Build | **Maven** | Cohérent avec `openapi-generator-maven-plugin`, déjà prévu pour la génération des clients. |
| Déploiement | **PAASv3** | |
| Image d'exécution | **`base-jdk25-dhi-alpine`** | Image durcie du registre interne Kereis, **inaccessible depuis Silamir**. Le `Dockerfile` la prend en `ARG BASE_IMAGE` ; en local on utilise un équivalent public de même JDK, et la bascule se fait sans toucher au reste. |
| Méthode | **GitHub Spec Kit** | `.specify/memory/constitution.md` et `CLAUDE.md` sont à la racine de ce dépôt. |

**Entrée du programme.** La réponse 200 de `GET /private/secure/input/documents` est l'entrée de
référence : `contracts/DMS_GET_input_documents_200.json`. Les jeux de simulation en dérivent.

**Simulation des données.** Les extractions Delos sont générées depuis le contrat de champs
(`CU3_Delos_Contrat_JSON_v1.xlsx`), un fichier JSON par document, puis déposées dans la réponse DMS.
Voir `docs/placement-extraction-delos.md` pour l'emplacement retenu et son alternative.

## Vocabulaire

Ces termes ont un sens précis et non interchangeable. Les employer tels quels dans le code, les tests et
les spécifications.

| Terme | Sens |
|---|---|
| **Lot** | Ensemble des pièces reçues en une fois. Correspond au *reception group* du DMS. L'unité de traitement — jamais la pièce isolée. |
| **BA** | Bulletin d'adhésion. Sa présence qualifie le lot en « Adhésion Cardif » et conditionne tout le reste. |
| **QSS / QS** | Questionnaire de santé simplifié / complet. Un QSS avec une réponse « oui » rend le dossier non recevable et déclenche la demande d'un QS complet. |
| **RM** | Rapport médical. |
| **DR** | Demande de renseignements. Regroupe tous les attendus manquants en un seul envoi. Le workflow automatique s'arrête après la première. |
| **Attendu** | Document que Tahoré calcule comme encore nécessaire, après prise en compte des courriers reçus. |
| **Acte** | Objet Tahoré portant les courriers reçus rattachés à une adhésion, avec le verdict de recevabilité. |
| **Avenant** | Objet Tahoré portant les garanties. L'adhésion initiale se matérialise par un avenant de souscription initiale. |
| **Recevabilité** | Verdict recevable / non recevable d'une pièce, plus la liste des motifs d'irrecevabilité. |
| **Complément de dossier** | Pièces reçues en réponse à une DR, rattachées à un dossier existant. |
| **Fiche d'action manuelle** | Sortie de dernier recours. Aucune API Tahoré n'existe pour ce cas. |
| **Stop control** | Validation gestionnaire imposée avant l'envoi d'un courrier. Levé progressivement. |
| **Cas nominal** | Toutes pièces P1 présentes, tous scores Delos suffisants, titulaire RIB M./Mme/Mlle, aucun QSS avec « oui » coché (RG-2.3.1). Cible : 80 % automatisés. |

**Systèmes** — **DMS** : gestion documentaire, indexation, métadonnées et états. **Delos** : moteur
d'extraction documentaire (IDP), livré par le CU#1, prérequis absolu. **Tahoré** : outil de gestion des
adhésions. **TIARA** : indexation manuelle de secours côté DMS.

## Machine à états

L'état vit dans le DMS, jamais dans le module. Une instance qui redémarre sans mémoire doit reprendre
correctement.

```
pending_automation (sans n° de dossier)   ← point d'entrée du module
   ├─→ création Tahoré OK → n° de dossier écrit dans les métadonnées DMS
   │                        → sort définitivement du périmètre d'écoute
   │                        → puis état « attaché » + indexation à l'adhésion (après création de l'acte)
   └─→ pending_processing  ← reprise manuelle, six causes exactement (RG-3.5.4)
```

**Les six causes de `pending_processing`** : erreur Delos · produit hors périmètre CU#3 · complexité
détectée · données insuffisantes · document à analyser manuellement · rediffusion complète du lot
incluant le BA.

**Invariant d'idempotence.** Si la création Tahoré réussit mais que l'écriture du n° de dossier dans le
DMS échoue, le cycle suivant doit rapprocher le lot du dossier existant et **ne jamais** en créer un
second (RG-3.5.5, CA-3.5.4). C'est le scénario de panne le plus important à couvrir par les tests.

**Indisponibilité Tahoré.** Le lot reste en `pending_automation`, le dossier est en « En attente
d'alimentation », la demande est conservée et rejouée sans perte (RG-3.1.7).

## Les sorties, toutes nommées

Toute exécution se termine sur exactement une de ces sorties. Il n'y a pas de septième cas.

1. **Certificat d'adhésion émis** — aucun attendu et QSS tout à « non ». `prendreDecision` puis `valider`.
2. **DR émise, en attente de validation** — attendus présents et aucun courrier à analyser manuellement.
3. **`pending_processing`** — documents à analyser par la gestion, ou l'une des six causes.
4. **Fiche d'action manuelle** — tout autre cas. Aucune API disponible.
5. **Lot rejeté** — BA absent **et** aucun n° de dossier. Seul cas de rejet total (RG-2.1.3).
6. **Classé sans suite** — 2 mois sans réponse à la DR. Réouverture automatique si de nouvelles pièces
   arrivent (côté Tahoré).

## Contrats d'API

### Tahoré — séquence d'appels

Aucun Swagger Tahoré n'est disponible à ce jour. Ces chemins viennent de l'annexe des spécifications et
de la documentation technique interne : **les payloads, codes d'erreur et modalités d'authentification
restent à obtenir auprès d'IT Kereis.** Isoler ces appels derrière une abstraction.

| # | Appel | Retour | Statut |
|---|---|---|---|
| 1 | `POST /api/personnes` — un appel par personne | `id_assure`, `id_souscripteur`, `id_signataire`, ids bénéficiaires | Existant |
| 2 | `POST /api/adhesions` | `id_adhesion` | Existant |
| 3 | `POST /api/adhesions/{id}/avenants` | `id_avenant` | Existant |
| 4 | `POST /api/adhesions/{id}/actes` | `id_acte` | **NEW 1** (+ **NEW 2** pour `codeEmissaire`) |
| 5 | `POST /api/adhesions/{id}/actes/{id_acte}/calculerAttendus` | attendus actualisés | **NEW 3** |
| 6a | `POST …/avenantEnCours/prendreDecision` | confirmation | Existant |
| 6b | `POST …/avenantEnCours/valider` | certificat d'adhésion | **NEW 4** (option visa) |
| 6c | `POST …/avenantEnCours/demandeDeRens` | courriers de DR | **NEW 5** |

Les ids de garanties et le code formule se récupèrent dans le module `referentiel-produit`.
Le `nature_id` de l'acte vient du référentiel des natures de courriers Tahoré, **non encore communiqué**
(RG-3.3.7). Les cinq NEW n'étant pas livrés, les chemins correspondants restent derrière un interrupteur
de configuration désactivé par défaut.

### DMS — endpoints confirmés au Swagger

Relevé du 14/08/2026 sur `document-management-services-internal-i1.cbp-dev.com/private/open/docs`.

| Usage | Endpoint |
|---|---|
| Scan du périmètre d'écoute | `GET /private/secure/input/documents` — filtres, `extension=reception`, paging `limit`/`offset`/`totalCount` |
| Lecture d'un lot | `GET …/documents/{documentUuid}` · `GET …/{documentUuid}/content` |
| Écriture métadonnées, statut, n° de dossier | `PUT …/documents/{documentUuid}` |
| Indexation à l'adhésion | `POST …/documents/{documentUuid}/bind` — *binding to a case and an application* : le **case** est le n° de dossier Tahoré |
| Recherche sans référence | `GET …/documents/identifiers` |

### Le document entrant — objet source de presque tous les contrôles

Schéma de `data[]` dans la réponse de `GET /input/documents`, relevé au Swagger le 17/08/2026.
Enveloppe : `count`, `totalCount`, `_links{first,previous,next,last}`, `serverRequestId`, `data[]`.

| Chemin | Ce qu'il porte |
|---|---|
| `processingState.id` | **L'état de traitement** — c'est ce champ qui porte `pending_automation` et `pending_processing`. Seule valeur observée dans l'exemple : `READY` ; l'énumération complète reste à obtenir. |
| `binding.reference` | **Le dossier de rattachement.** Candidat au n° de dossier Tahoré, avec `boundAt`, `boundByApp`, `boundByUser`. Le second critère d'écoute devient « `binding` absent ou `binding.reference` vide ». |
| `reception.id` · `receptionOrder` | **Le lot** et le rang du document dedans. Le lot ne se crée pas, il se lit : grouper par `reception.id`. `reception.createdAt` est le point de départ naturel du SLA. |
| `indexation.nature` | **La nature de la pièce** — BA, RIB, mandat SEPA, QSS… Objet vide dans l'exemple : structure et référentiel à obtenir. C'est aussi ce référentiel à mapper vers le `nature_id` de l'API `actes`. |
| `indexation.domain.name` | Le domaine métier, qui délimite le périmètre d'écoute du module. |
| `indexation.reference` | La référence d'indexation (`input_document.indexation_reference` au mapping de tri). |
| `metadata` | **Objet libre** — c'est là que vivent les données extraites et leurs scores de confiance. La convention de clés est propre à Kereis et **reste à obtenir : c'est le point le plus structurant du module.** |
| `permissions.medicalConfidentiality` | Marqueur de confidentialité médicale, support du cloisonnement des données de santé. |
| `lock` | Objet de verrouillage. **Non exploité** : le module ne verrouille pas les lots. |
| `events[]` | **La piste d'audit, embarquée** : `id` (valeur observée `DOCUMENT_CREATED`), `triggeredAt`, `triggeredByApp`, `triggeredByUser`. RG-3.5.6 est couvert sans journal propre au module. |
| `content.active` | Contenu actif. Un document sans contenu actif, ou en archivage `DESTROYED`, n'est jamais retourné. |
| `associations[]` | Liens entre documents entrants — `type.id` observé : `MERGING`. |
| `indexation.insured` · `insurer` · `distributor` · `warranty` · `family` · `group` | Axes d'indexation, candidats pour discriminer le produit sans relire l'extraction. |
| `archivingState` · `archivingRequests[]` · `transfers[]` | Archivage et transferts externes (`transferredTo.id` observé : `TESSI`). Hors périmètre. |

**Endpoints existants proposés mais non arbitrés** — ils répondent à un besoin réel, aucun n'est cité par
les spécifications, tous doivent être validés avant usage : `GET …/{documentUuid}/sla` (SLA de 48 h
glissantes), `GET …/{documentUuid}/events` et `GET /private/secure/input/events` (piste d'audit),
`POST …/{documentUuid}/close` et `/reopen` (synchronisation du classement sans suite).

**Écartés par décision de conception** — les endpoints existent, le module ne les utilise pas :
`POST …/documents/lock` et `/unlock` (le module ne verrouille pas les lots ; le double critère
« `pending_automation` + `binding` vide » reste le seul verrou), et `POST …/{documentUuid}/clone`
(le module ne duplique pas les pièces en multi-adhésions).

**Interdits** — dépréciés dans le Swagger : tout le versioning des documents entrants et sortants,
`POST /private/secure/input/receptions`, `PUT …/content/rotate`, `GET …/export/stock/last/count`,
verrouillage des documents sortants, suppression de commentaire, retrait d'un document d'une transmission.

## Documents et champs

Dix documents alimentent les contrôles. Les libellés de champs viennent du dictionnaire
`CU3_IDP_Champs_Derniere_version.xlsx` et **se citent tels quels, sans reformulation**. La notation
`BA #37` désigne la ligne 37 de l'onglet D09 : « Bénéficiaire désigné - part attribuée (%) ».

| Code | Document | Priorité | Champs cadrés |
|---|---|---|---|
| D09 | Bulletin d'adhésion (BA) signé et paraphé | P1 | 50 |
| D10 | Recueil de consentement (RGPD) | P1 | 5 |
| QSS | Questionnaire de santé simplifié | P1 | 6 |
| D13 | Mandat SEPA | P1 | 6 |
| D14 | RIB | P1 | 5 |
| D11 | Questionnaire de santé complet (QS) | P2 | 4 — **incomplet** |
| D17 | Statuts / Kbis / bilans / liasses fiscales | P2 | 2 — **incomplet** |
| D12 | Rapport médical (RM) | P3 | 15 |
| D15 | Proposition d'assurance (PA) signée | P3 | 14 |
| D16 | Bon pour Accord (BPA) signé | P3 | 14 |

Hors dictionnaire : la **convention de signature électronique** (P1, pièce obligatoire mais sans onglet)
et l'**enveloppe du mail** (objet et corps, porteurs de la référence de dossier).

Un score de confiance Delos à 0 vaut **donnée manquante**, pas donnée douteuse (RG-1.2.2, RG-2.1.7).

## Pièges relevés

- **Le lot, pas la pièce.** Delos reçoit le lot entier en une fois, jamais pièce par pièce (RG-1.2.1).
  Sur un complément, à l'inverse, Delos n'est relancé que sur la pièce manquante (RG-3.2.2).
- **Purge Delos à 10 minutes.** Les données extraites disparaissent côté Delos dix minutes après
  traitement (RG-1.2.7). Les métadonnées DMS sont la seule source durable.
- **Certaines pièces ne passent pas par Delos.** PV d'AG, attestation fiscale, courrier simple : elles
  se rattachent directement au dossier (RG-1.2.6).
- **Titulaire du RIB ≠ M./Mme/Mlle** signale un contrat professionnel : dossier complexe, justificatifs
  professionnels demandés (RG-2.3.2).
- **Répartition des bénéficiaires ≠ 100 %** déclenche une DR automatique (RG-2.2.3).
- **IBAN du RIB ≠ IBAN du mandat SEPA**, ou titulaires différents : alerte et notification gestionnaire
  (RG-2.2.2).
- **QS reçu depuis une adresse `@bnpparibas.com`**, ou sans nom ni prénom : non recevable (RG-2.2.5).
- **Rediffusion complète du lot incluant le BA** : traitement manuel, aucune création automatique. Ne
  jamais confondre avec un complément (RG-3.2.7).
- **~5 % des mails n'ont ni référence de dossier ni identité exploitable** : recherche dans le DMS puis
  dans Tahoré (RG-1.1.7).
- **Multi-adhésions dans un même mail** : **le module ne les traite pas.** Dès qu'un lot portant plusieurs
  adhésions est identifié, il est renvoyé au DMS en `pending_processing`, sans aucun appel Tahoré. La
  libération des pièces et la duplication du mandat SEPA et du RIB restent des gestes du gestionnaire
  (RG-2.3.3). Seule la détection est à notre charge.
- **Seuls les courriers ne nécessitant pas d'analyse manuelle sont attachés** à l'acte. Les autres
  restent en `pending_processing` (RG-3.3.3).

## Sources de vérité

| Fichier | Fait foi sur |
|---|---|
| `CU3_Adhesion_Prevoyance_Cardif_Specifications_v4` | Les règles de gestion et critères d'acceptation. Arbitre tout conflit. |
| `CU3_Sequence_Controles_APIs_v1.xlsx` | L'ordre des 68 étapes, les données consommées, leur document source, les inventaires d'API. Filtrer la colonne « Périmètre de build » sur « Module externe » donne les 32 étapes à construire. |
| `CU3_IDP_Champs_Derniere_version.xlsx` | Les libellés et formats de champs. |
| `CU3_Delos_Contrat_JSON_v1.xlsx` | **Le contrat de champs — source de vérité des documents cibles.** Une copie lisible par machine est versionnée dans `contracts/delos-contrat-champs.json`, avec l'empreinte SHA-256 du classeur ; `ContratChamps`, `CheminsExtraction` et les fixtures en dérivent tous. Ne jamais ressaisir un nom, un type, une énumération ou un caractère obligatoire à la main. |
| `CU3_Schema_Sequence.pptx` | La lecture visuelle de la séquence, phase par phase. |
| `.specify/memory/constitution.md` | Les invariants non négociables du module. |

## Conventions de travail

- **Une fonctionnalité Spec Kit par User Story** du périmètre : US-2.1, US-2.3, US-3.1, US-3.2, US-3.3,
  US-3.4, US-3.5. Nommer la fonctionnalité avec son identifiant d'US.
- **Les CA sont les critères de test.** Un critère d'acceptation sans test automatisé correspondant se
  signale au moment du plan, il ne se contourne pas.
- **Citer les références.** Toute règle implémentée porte son identifiant RG dans le code ou le test.
  Une règle sans référence est une invention à supprimer.
- **Signaler plutôt que combler.** Face à une donnée manquante — payload Tahoré, référentiel non
  communiqué, seuil non figé — écrire explicitement le point ouvert dans la spécification. Ne jamais le
  résoudre par une valeur par défaut silencieuse.
- **Distinguer les propositions des exigences.** Cinq étapes de la matrice sont des propositions de
  conception issues du Swagger DMS, pas des exigences : `lock`/`unlock`, `clone`, `sla`, `events`,
  `close`/`reopen`. Elles sont marquées « proposition » et attendent un arbitrage.

## Points ouverts

À faire trancher avant ou pendant le chiffrage. Ne pas les résoudre unilatéralement.

Classés par ce qu'ils bloquent. Les trois premiers empêchent d'écrire les adaptateurs ; les suivants
empêchent de figer des règles métier.

| Sujet | Question | Porteur |
|---|---|---|
| **Clés de `metadata`** | Convention Kereis de nommage des données extraites et de leurs scores. Sans elle, aucun contrôle sur les données n'est spécifiable précisément | IT Kereis |
| **Valeurs de `processingState.id`** | L'énumération complète. `pending_automation` et `pending_processing` doivent y figurer ; seul `READY` est observé | IT Kereis |
| **`indexation.nature`** | Structure de l'objet et référentiel de ses valeurs. Bloque le contrôle de complétude et le mapping vers `nature_id` | IT Kereis |
| Sémantique de `binding.reference` | Est-ce bien le n° de dossier Tahoré ? Écrit par `bind` ou par `PUT` sur les métadonnées ? | IT Kereis |
| Valeurs de `indexation.domain` | Délimite le périmètre d'écoute du module | IT Kereis |
| Filtrage du `GET` | Peut-on filtrer côté serveur sur `processingState` et sur l'absence de `binding`, ou faut-il filtrer côté module ? | IT Kereis |
| Contrats Tahoré | Payloads, codes d'erreur, authentification. Périmètre des trois API existantes | Maxime Falaize |
| Natures de courriers | Référentiel Tahoré et mapping avec `indexation.nature` (RG-3.3.7) | IT Kereis |
| Ids de garanties | Récupération dans `referentiel-produit`, paramétrage produit CGV4 (RG-3.1.4) | IT Kereis |
| Qui calcule la recevabilité | RG-2.2.6 la place dans Tahoré, RG-2.2.7 la fait transmettre par le module. Contradiction à trancher | IT Kereis + métier |
| Seuil Delos | Valeur du seuil de confiance minimal, par champ et par type de document (RG-1.2.2) | Métier + Delos |
| Champs obligatoires du BA | Liste exhaustive à figer (RG-2.1.7) | Métier |
| Comparaison des identités | Tolérance sur casse, accents, nom d'usage, double titulaire — pour le contrôle bancaire et le consentement | Métier |
| Multi-adhésions | Règle de **détection** à formaliser : mots-clés dans l'objet du mail, ou présence de plusieurs bulletins dans le lot. Le traitement, lui, est hors périmètre par décision de conception | Métier |
| Typage DMS | Recueil de consentement et convention de signature électronique non typés automatiquement | IT Kereis |
| Stop control | Planning de levée progressive (RG-4.2.3) | Métier |
| Dictionnaire | Onglets D11, D12, D15, D16, D17 incomplets ou en statut « À valider » | Métier |
