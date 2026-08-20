# Organisation du depot

Un depot de code, pas un dossier de projet. Les documents bureautiques de la phase d'analyse
— PDF de specifications, dictionnaires Excel, presentations — restent hors du depot : ils ont un
autre cycle de vie, ils ne se relisent pas en diff, et ils alourdissent chaque clone.

## Regle de rangement

Chaque repertoire de premier niveau repond a une seule question.

| Repertoire | Question a laquelle il repond | Cycle de vie |
|---|---|---|
| `contracts/` | *Avec quoi le module dialogue-t-il ?* | Suit les livraisons des DSI DMS et Tahore |
| `src/` | *Que fait le module ?* | Le travail quotidien |
| `wiremock/` | *Comment le fait-on tourner sans les vrais systemes ?* | Suit les fixtures |
| `deploy/` | *Comment le met-on en production ?* | Suit la plateforme |
| `docs/` | *Pourquoi ces choix ?* | Suit les arbitrages |
| `.specify/`, `CLAUDE.md` | *Comment travaille-t-on dessus ?* | Rare, stable |

Le module Maven est **a la racine** : `pom.xml`, `src/`, `Dockerfile`. Un developpeur clone, ouvre,
compile. Pas de niveau intermediaire qui n'apporterait rien tant qu'il n'y a qu'un artefact a livrer.

## Detail

```
Kereis_CU3_Adhesion_Cardif/
├── README.md                       Porte d'entree : stack, demarrage, ecarts a arbitrer
├── CLAUDE.md                       Contexte de travail pour l'agent (Spec Kit)
├── .specify/memory/constitution.md Principes non negociables
├── pom.xml                         Java 25, Spring Boot 4.0.7, outillage
├── Dockerfile                      Image de base en ARG : bascule Silamir / client
├── compose.yaml                    Stack locale : module + DMS et Tahore simules
│
├── contracts/                      Les contrats d'interface, source de verite
│   ├── 01-Document-Management-Services.yaml      DMS 3.13.18 — 163 chemins
│   ├── 02-API-DSI-SOLUTIONS-PREVOYANCE.yaml      Adhesions, avenants, personnes
│   ├── 03-Tahore-Actes-Decisions.yaml            Actes et decisions
│   ├── DMS_GET_input_documents_200.json          L'entree de reference du programme
│   └── README.md                                 Tri des doublons
│
├── deploy/
│   ├── README.md                   Le contrat PaaS, et ce qui manque sur PAASv3
│   ├── kubernetes/deployment.yaml  Cible neutre exprimant ce contrat
│   └── verifier-contrat-paas.sh    Verifie les 7 points sur l'image construite
│
├── docs/
│   ├── api/                        Les trois contrats en HTML autonome (sans CDN ni JS)
│   ├── organisation.md             Ce document
│   ├── stack-technique.md          Outillage, budget, leviers de cout
│   ├── placement-extraction-delos.md   Ou se range l'extraction, et pourquoi
│   └── ecarts-specifications-api.md    Ecarts specifications / API reelles
│
├── src/main/java/com/kereis/tahore/documentprocessing/
│   ├── domain/                     Modele et ports. Aucune dependance framework.
│   ├── application/                Cas d'usage, exprimes sur les ports
│   └── infrastructure/             Adaptateurs DMS et Tahore, configuration
├── src/main/resources/application.yaml
├── src/test/java/…                 Dont ArchitectureHexagonaleTest
├── src/test/resources/fixtures/
│   ├── delos/                      Une extraction par document du contrat de champs
│   ├── dms/                        Six reponses 200 completes, un scenario par fichier
│   └── catalogue.json              Index des scenarios
└── wiremock/                       Stubs DMS et Tahore, alimentes par ces fixtures
```

## Ce qui est volontairement absent

| Exclu | Pourquoi |
|---|---|
| PDF, XLSX, PPTX | Documents d'analyse : autre cycle de vie, illisibles en diff, lourds au clone |
| `CU3_Sequence_Controles.html` | Livrable de restitution, pas du code |
| Archives et captures d'ecran | Matiere premiere ; le contenu utile en a ete extrait dans `contracts/` |
| `APIs_Swaggers_DMS_Prev_Tahore.txt` | Agregat de depart, remplace par les trois specs decoupees |

Les fichiers HTML de `docs/api/` sont la seule exception a l'exclusion du HTML : ce sont des
**artefacts generes depuis `contracts/`**, regenerables, et destines a etre lus dans le depot.

## Interaction avec le cout d'exploitation

`CLAUDE.md` est charge a chaque session : il doit rester court et stable. Les parties specialisees
— tableau des chemins JSON du DMS, table des API Tahore, referentiel des documents — ont leur place
dans `docs/` ou dans une skill, pas dans `CLAUDE.md`. Le raisonnement complet est au § 5 de
[`stack-technique.md`](stack-technique.md).
