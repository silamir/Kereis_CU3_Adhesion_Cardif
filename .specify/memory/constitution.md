<!--
Sync Impact Report
Version: 1.0.0 (ratification initiale)
Périmètre : module externe du CU#3 — Adhésion Prévoyance Cardif (build Silamir)
Principes ajoutés : I à VII
Sections ajoutées : Contraintes données & conformité · Contrats d'interface · Workflow de développement · Gouvernance
Source : CU3_Adhesion_Prevoyance_Cardif_Specifications_v4 (4 Epics, 11 US) et la matrice
         CU3_Sequence_Controles_APIs_v1.xlsx (68 étapes, dont 32 dans ce périmètre)
Templates à propager : spec-template, plan-template, tasks-template — à vérifier après `specify init`
TODO : aucun principe en attente. Les points ouverts vivent dans CLAUDE.md, pas ici.
-->

# Constitution — Module externe CU#3 · Adhésion Prévoyance Cardif

Ce document régit la construction du **module externe** (`tahore-document-processing`), et lui seul.
Le DMS, Delos (CU#1) et Tahoré sont des systèmes tiers dont ce dépôt consomme les interfaces sans les
modifier. Les principes ci-dessous sont des portes bloquantes : un plan ou une implémentation qui les
enfreint ne passe pas la revue.

## Principes fondamentaux

### I. Le périmètre d'écoute est le seul verrou anti-doublon (NON NÉGOCIABLE)

Le module découvre son travail en interrogeant cycliquement le DMS. Ce filtre est la totalité de la
protection contre les doublons : il n'existe aucun autre garde-fou en aval.

- Le module MUST ne prendre en charge qu'un document de son domaine en statut `pending_automation`
  **et** dépourvu de n° de dossier. Les deux conditions sont indissociables (RG-2.1.2, RG-3.5.1).
- Dès qu'une création aboutit dans Tahoré, le module MUST écrire le n° de dossier ou d'adhésion dans
  les métadonnées DMS. C'est cette écriture qui sort le lot du périmètre d'écoute (RG-3.5.2).
- Le traitement MUST être idempotent : si la création Tahoré réussit mais que l'écriture DMS échoue, le
  cycle suivant MUST rapprocher le lot du dossier existant et MUST NOT en créer un second (RG-3.5.5).
- Le module MUST NOT traiter un état de progression interne comme source de vérité. L'état vit dans le
  DMS ; une instance qui redémarre sans mémoire doit reprendre correctement.

*Rationale : un doublon d'adhésion est une anomalie contractuelle, pas un incident technique. Toute
optimisation qui contourne ce double critère — cache local, file interne, marquage applicatif — recrée
un chemin où un lot peut être traité deux fois.*

### II. Rien ne bloque la création du dossier (NON NÉGOCIABLE)

Le gain métier vient de la pré-saisie, pas du filtrage. Un dossier imparfait créé avec des données
provisoires vaut mieux qu'un dossier non créé.

- Une pièce P1 manquante autre que le bulletin d'adhésion MUST donner lieu à une création dans Tahoré
  avec données provisoires, assortie d'une demande de renseignements (RG-2.1.8).
- Un champ obligatoire absent, ou dont le score de confiance Delos est à 0, MUST être traité comme
  manquant : donnée provisoire puis DR, jamais un refus de créer (RG-2.1.7, RG-3.1.6).
- Les contrôles de cohérence entre pièces MUST NOT être bloquants (RG-2.2.6).
- Le seul rejet total admis est l'absence de bulletin d'adhésion **et** de n° de dossier (RG-2.1.3).
- Le module MUST NOT introduire de verrou de son cru là où Tahoré n'en pose pas.

*Rationale : chaque blocage ajouté renvoie un dossier vers la saisie manuelle et détruit précisément la
valeur que le CU#3 doit produire. La cible est 80 % des cas nominaux automatisés (RG-2.3.7).*

### III. Le module pousse, il ne se fait pas interroger

Le sens de l'échange est tranché et ne se renégocie pas au fil des développements.

- Le module MUST pousser les données vers Tahoré via les API Tahoré. Tahoré MUST NOT être appelé à lire
  le DMS (RG-3.1.1).
- Tous les appels d'API du CU#3 MUST être pilotés par le module externe.
- Une indisponibilité de l'API Tahoré MUST être absorbée sans perte : le lot reste en
  `pending_automation`, la demande est conservée et rejouée au cycle suivant (RG-3.1.7).
- Le module MUST NOT dépendre d'un rappel entrant (webhook, callback) de Tahoré ou du DMS pour avancer.

*Rationale : le modèle « scan puis push » rend le module redémarrable à tout instant et sans état
partagé. Toute inversion de contrôle réintroduit une coordination distribuée que ni le DMS ni Tahoré
ne sont conçus pour porter.*

### IV. Toute décision est tracée et rejouable

Un contrôle dont on ne peut pas reconstituer le verdict n'est pas un contrôle : c'est un effet de bord.

- Chaque résultat de contrôle — éligible, complet, incomplet, repris manuellement — MUST être écrit
  horodaté dans les métadonnées DMS (RG-2.1.9).
- Chaque transition d'état MUST porter son horodatage et son acteur (RG-3.5.6).
- Chaque champ pré-saisi MUST porter sa source — Delos ou provisoire — et son horodatage (RG-3.1.8).
- Chaque anomalie de cohérence MUST être tracée avec son type et son horodatage (CA-2.2.4).
- La piste d'audit MUST s'appuyer sur les événements DMS existants plutôt que sur un journal propre au
  module, sauf décision contraire explicitement motivée.

*Rationale : sans piste d'audit, ni le SLA de 48 heures glissantes ni le taux d'automatisation ne sont
mesurables, et toute reprise manuelle repart de zéro — ce qui annule le bénéfice de l'automatisation
partielle.*

### V. Une sortie explicite pour chaque cas (NON NÉGOCIABLE)

Le workflow est fini et fermé. Il n'existe pas de cas « qui passe à travers ».

- Toute exécution MUST se terminer sur exactement une sortie nommée : dossier créé et certificat
  d'adhésion émis, DR émise, lot en `pending_processing`, fiche d'action manuelle, ou lot rejeté.
- Un cas non couvert par les trois premières routes MUST produire une fiche d'action manuelle, jamais un
  abandon silencieux ni une nouvelle branche implicite (RG-3.4.5).
- Le workflow automatique MUST s'arrêter après la première demande de renseignements. Les compléments
  reçus ensuite relèvent du traitement manuel (RG-3.2.6, RG-3.4.6).
- Le module MUST NOT inventer d'appel d'API pour un cas non spécifié : en l'absence d'API, la sortie est
  la fiche d'action manuelle.

*Rationale : les six causes documentées de passage en `pending_processing` (RG-3.5.4) forment un
inventaire clos. Une septième route non nommée est un dossier que personne ne reprend.*

### VI. Ce qui est appelé à changer est paramétrable

Plusieurs règles sont connues comme instables. Elles MUST vivre en configuration, pas dans le code.

- La règle de routage des DR — volet administratif vers le conseiller, volet médical vers l'assuré —
  MUST être modifiable sans redéploiement (RG-4.2.5). Une évolution Cardif est déjà annoncée pour
  septembre-octobre : toute la partie administrative basculera vers le conseiller (RG-4.2.6).
- Le seuil minimal de confiance Delos MUST être en paramétrage (RG-1.2.2).
- Les délais de relance (J+28, J+30) et de classement sans suite (2 mois) MUST être paramétrables
  (RG-4.2.4).
- Le stop control gestionnaire avant envoi MUST pouvoir être levé progressivement, périmètre par
  périmètre (RG-4.2.3).
- La liste des produits éligibles au périmètre CU#3 MUST être une donnée de configuration, pas une
  condition codée en dur (RG-2.1.5).

*Rationale : chacune de ces valeurs a déjà changé ou changera pendant la vie du projet. Les figer dans
le code transforme un ajustement métier en cycle de livraison.*

### VII. Le module ne fait pas le travail des autres systèmes

La frontière de responsabilité est une propriété du produit, pas une commodité d'implémentation.

- Le module MUST NOT implémenter la réception, la séparation, le typage ou la qualification des pièces :
  cela appartient au DMS, en amont de son point d'entrée.
- Le module MUST NOT extraire de données depuis le contenu des documents. Toute donnée métier MUST
  provenir des JSON Delos intégrés aux métadonnées DMS (RG-1.2.3). Une ré-extraction locale, un OCR de
  secours ou une heuristique de rattrapage sont interdits.
- Le module MUST NOT réimplémenter les contrôles de cohérence entre pièces : ils sont portés par le
  moteur de recevabilité Tahoré (RG-2.2.6). Le module les déclenche et consomme leur résultat.
- Le module MUST NOT écrire dans Tahoré autrement que par les API documentées.

*Rationale : le CU#3 est réalisé par des évolutions du DMS et de Tahoré plus la création de ce module.
Chaque règle métier dupliquée ici est une divergence garantie le jour où l'autre système évolue.*

## Contraintes de données et de conformité

- Les questionnaires de santé (QSS, QS) et les rapports médicaux portent des données de santé. Le module
  MUST NOT les persister au-delà de ce que le traitement exige, MUST NOT les faire apparaître dans les
  journaux, et MUST NOT les transmettre à un destinataire autre que celui prévu par la règle de routage.
- Le recueil de consentement RGPD conditionne le traitement des données de santé : sa présence et sa
  signature sont contrôlées (D10, champs 1 à 5) et son absence est un motif de DR.
- Les données extraites sont supprimées côté Delos dix minutes après traitement (RG-1.2.7). Le module
  MUST considérer les métadonnées DMS comme la seule source durable des données extraites.
- Le volet médical d'une DR MUST partir à l'assuré directement, jamais au conseiller bancaire
  (RG-4.2.2), y compris après la bascule administrative annoncée.
- Aucune donnée personnelle ou bancaire MUST apparaître dans un message d'erreur, une trace applicative
  ou un ticket automatique.

## Contrats d'interface

- Le module MUST NOT être développé contre un endpoint non confirmé. Un appel dont le contrat n'est pas
  validé par IT Kereis MUST être isolé derrière une abstraction et couvert par un test de contrat, afin
  que la découverte du vrai contrat ne se propage pas dans la logique métier.
- Cinq développements côté Tahoré conditionnent les phases 5 et 6 : création de l'acte portant les
  courriers reçus, champ `codeEmissaire`, `calculerAttendus`, option de mise en attente de visa sur
  `valider`, et `demandeDeRens` avec mise en attente de validation. Tant qu'ils ne sont pas livrés, les
  chemins correspondants MUST rester derrière un interrupteur de configuration désactivé par défaut.
- Les endpoints DMS marqués dépréciés dans le Swagger MUST NOT être utilisés — en particulier tout le
  versioning des documents entrants et sortants, et la création de groupe de réception.
- Toute hypothèse d'interface non confirmée MUST être signalée dans la spécification concernée sous une
  rubrique explicite, et MUST NOT être silencieusement résolue par une valeur par défaut.

## Workflow de développement

- Le découpage en fonctionnalités suit les User Stories des spécifications. Une fonctionnalité Spec Kit
  couvre une US ou un groupe cohérent d'US du périmètre module externe : US-2.1, US-2.3, US-3.1, US-3.2,
  US-3.3, US-3.4, US-3.5.
- Les critères d'acceptation (CA) des spécifications sont la source des critères de test. Un CA sans
  test automatisé correspondant MUST être signalé au moment du plan.
- La matrice `CU3_Sequence_Controles_APIs_v1.xlsx` fait foi sur l'ordre des étapes, les données
  consommées et leur document source. En cas de divergence entre une spécification et la matrice, la
  spécification l'emporte et la matrice MUST être corrigée.
- Les libellés de champs proviennent du dictionnaire `CU3_IDP_Champs_Derniere_version.xlsx`. Ils MUST
  être repris tels quels, sans reformulation, dans les spécifications comme dans le code.

## Gouvernance

**Autorité.** Les principes I à VII sont des portes bloquantes. Tout plan MUST être évalué au regard de
ces principes avant implémentation, et tout écart MUST être justifié explicitement dans le plan. Un
conflit entre une demande et un principe est traité comme un point critique à arbitrer, pas comme une
préférence d'implémentation.

**Amendements.** Toute modification de cette constitution MUST faire l'objet d'une justification écrite,
être validée par le responsable de lot, et donner lieu à une montée de version. Les modèles de
spécification, de plan et de tâches MUST être vérifiés et propagés dans la même opération.

**Versionnement.** SemVer. MAJEUR : retrait ou redéfinition incompatible d'un principe. MINEUR : ajout
d'un principe ou d'une section. CORRECTIF : clarification rédactionnelle sans changement de portée.

**Revue de conformité.** Chaque revue de code vérifie le respect des principes. Un écart non justifié
bloque la fusion. Les principes I, II et V sont non négociables : aucun écart n'y est admis.

**Version : 1.0.0 | Ratifiée le : 2026-08-17 | Dernier amendement : 2026-08-17**
