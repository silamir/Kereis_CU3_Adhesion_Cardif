# Kereis — CU#3 Adhesion Prevoyance Cardif

Module externe de traitement automatise des dossiers d'adhesion Prevoyance Cardif. Il consomme les
lots de documents qualifies par le **DMS**, execute les controles metier a partir des donnees
extraites par **Delos**, et cree l'acte correspondant dans **Tahore**.

Nom du module cote client : `Tahore_document_processing`.

> Developpement sur l'environnement **Silamir**, puis integration chez le client. La stack locale
> reproduit la stack cible pour que le passage se fasse sans reecriture.

## Stack

| | |
|---|---|
| Langage | Java 25 |
| Framework | Spring Boot 4.0.7 — Spring Framework 7, **Jackson 3** |
| Build | Maven |
| Architecture | Hexagonale, verifiee par ArchUnit |
| Methode | GitHub Spec Kit |
| Deploiement cible | PAASv3, image `base-jdk25-dhi-alpine` |

## Demarrage

Aucun outil de la chaine n'etait installe sur la machine de generation : **le projet n'a pas encore
ete compile**. Installer JDK 25, Maven 3.9+ et Docker, puis :

```bash
mvn verify                                  # controles statiques, tests, architecture
mvn -P generate-clients generate-sources    # clients Java depuis contracts/
docker compose up --build                   # module + DMS et Tahore simules
./deploy/verifier-contrat-paas.sh           # les 7 points du contrat PaaS
```

## Organisation du depot

```
contracts/          Les 3 specifications OpenAPI, la reponse 200 de reference, et le tri des doublons
deploy/             Cible de deploiement neutre et verification du contrat PaaS
docs/               Documentation technique, dont docs/api/ en HTML autonome
src/main/java/      domain (modele + ports) · application (cas d'usage) · infrastructure (adaptateurs)
src/test/resources/fixtures/   Extractions Delos par document, reponses DMS par scenario
wiremock/           Stubs alimentes par ces fixtures
.specify/           Constitution du projet (Spec Kit)
CLAUDE.md           Contexte de travail pour l'agent
```

Detail et justification des choix : [`docs/organisation.md`](docs/organisation.md).

## Documentation

| Document | Contenu |
|---|---|
| [`docs/api/index.html`](docs/api/index.html) | Les trois contrats d'API en pages HTML autonomes |
| [`docs/stack-technique.md`](docs/stack-technique.md) | Outillage, budget, leviers de cout |
| [`docs/placement-extraction-delos.md`](docs/placement-extraction-delos.md) | Ou se range l'extraction Delos dans le DMS, et pourquoi |
| [`docs/ecarts-specifications-api.md`](docs/ecarts-specifications-api.md) | Ecarts releves entre les specifications CU#3 et les API reelles |
| [`.specify/memory/constitution.md`](.specify/memory/constitution.md) | Principes non negociables |

## Ecarts a arbitrer

Trois points bloquent la conception et sont documentes en detail dans
[`docs/ecarts-specifications-api.md`](docs/ecarts-specifications-api.md) :

1. **`PENDING_AUTOMATION` n'existe pas** dans l'enumeration du DMS, alors que les specifications en
   font le critere d'ecoute du module.
2. **La convention de cles de `metadata`** — ou vit l'extraction Delos — n'est pas documentee.
3. **Trois des cinq developpements Tahore attendus sont absents**, et un quatrieme fonctionne a
   l'inverse de ce qui etait specifie.

## Donnees

Les jeux de simulation sont **entierement synthetiques** : aucune donnee reelle, aucun IBAN valide,
aucune donnee de sante rattachee a une personne existante. Les donnees de sante reelles ne doivent
jamais apparaitre en journal ni en fixture — voir la constitution.
