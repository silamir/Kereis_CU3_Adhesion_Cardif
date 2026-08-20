# CU#3 — Stack technique du module externe

Complément à `CLAUDE.md`. Le client impose **Java + Spring Boot + architecture hexagonale** :
ce document ne discute pas ce socle, il décrit la couche d'outillage à ajouter autour, choisie
pour deux contraintes de projet :

- **Budget IA : 800 $ pour 3 développeurs, modèle Sonnet uniquement.**
- **Disponibilité métier rare** : chaque question posée au métier coûte des jours de calendrier.

Chaque brique ci-dessous est justifiée par l'un de ces deux leviers, jamais par la mode.

---

## 1. Ce que valent réellement 800 $

### Le repère terrain d'abord

La documentation Claude Code donne une moyenne observée en entreprise de **13 $ par développeur
et par jour actif**, soit 150 à 250 $ par développeur et par mois.

**800 $ à trois, c'est 267 $ par développeur, soit environ 20 jours actifs chacun — un mois de
travail.** C'est le cadrage honnête : l'enveloppe couvre la construction du module, pas une phase
d'exploration prolongée ni des reprises successives.

### Le calcul par tokens confirme le repère

Sonnet 5 est à 3 $ / MTok en entrée et 15 $ / MTok en sortie. Une lecture en cache coûte **0,1 ×**
le prix d'entrée. Sur un tour de travail typique (≈ 40 k tokens d'entrée, 2,5 k de sortie) :

| Régime | Coût / tour | Tours pour 800 $ |
|---|---|---|
| Entrée majoritairement en cache (35 k cache + 5 k frais) | 0,063 $ | **≈ 12 700** |
| Aucun cache (40 k frais) | 0,158 $ | ≈ 5 100 |

≈ 12 700 tours pour trois personnes sur vingt jours, cela fait environ 200 tours par personne et
par jour : les deux méthodes de calcul concordent. **Et le cache multiplie l'enveloppe par 2,5 —
c'est le premier levier, et il est gratuit** (§ 5).

### Le tarif d'introduction expire le 31 août 2026

Jusqu'à cette date Sonnet 5 est facturé **2 $ / 10 $** au lieu de 3 $ / 15 $, soit **+50 % de tours
pour le même budget**. Toute charge industrialisable — génération de squelettes, passes de revue en
masse, rédaction des tests — gagne à être exécutée avant fin août.

### Ce qui n'est pas le problème

Générer du code ne coûte presque rien : 600 lignes ≈ 0,12 $, 2 000 lignes ≈ 0,39 $. Le module
entier, écrit une fois, représente quelques dizaines de dollars.

**Le vrai risque budgétaire est la reprise** : réécrire trois fois le même adaptateur parce que la
frontière hexagonale était fausse, ou parce que le mapping des 72 champs Delos a dérivé. Toute la
stack ci-dessous vise à rendre l'erreur *détectable par un outil* plutôt que par un aller-retour
avec le modèle.

---

## 2. Génération de code — le levier le plus rentable

Principe : **toute ligne produite par un générateur est une ligne que Sonnet n'écrit pas, ne relit
pas, et ne peut pas se tromper en écrivant.** Coût en tokens : nul.

| Brique | Ce qu'elle génère ici | Pourquoi ce projet en particulier |
|---|---|---|
| **openapi-generator-maven-plugin** | Le client DMS complet et tous les DTO | Nous avons le Swagger réel. Le seul document d'entrée compte 32 chemins JSON imbriqués (`indexation`, `binding`, `reception`, `events[]`, `associations[]`, `permissions`) : plusieurs centaines de lignes de DTO qu'il serait absurde de dicter au modèle, et où une faute de frappe ne se voit qu'au runtime. |
| **jsonschema2pojo** (ou openapi-generator) | Les records du contrat Delos | Les 5 documents et 72 champs du contrat sont déjà spécifiés. Les schémas JSON sont régénérables depuis le modèle de génération, même s'ils ne figurent plus dans le classeur de relecture. |
| **MapStruct** | Les mappings domaine ↔ DTO dans les adaptateurs | Un champ oublié devient une **erreur de compilation qui désigne la ligne**. C'est la différence entre un diagnostic gratuit et une session de débogage facturée. |
| **Records Java + Lombok** | Constructeurs, `equals`, `hashCode`, accesseurs | Rien à écrire, rien à relire. |

> **Convention à trancher avant de générer** : la clé `metadata` du DMS reste inconnue. Elle doit
> rester **isolée dans l'adaptateur DMS**, jamais dans le domaine — sinon sa découverte déclenchera
> une reprise transverse.

---

## 3. Boucles de retour déterministes — remplacer le raisonnement par un diagnostic

Chaque outil de cette section transforme une classe d'erreur en message d'erreur immédiat. Un
compilateur ne consomme pas de tokens.

| Brique | Rôle | Ce qu'elle évite |
|---|---|---|
| **`jdtls-lsp`** (plugin officiel) | Active le serveur de langage Java (`jdtls`) : Claude reçoit **les erreurs de type et d'import automatiquement après chaque édition**, et navigue par symbole (définition, références, hiérarchie d'appels) au lieu de chercher par texte. | Le cycle « édite, lance Maven, lit la sortie, corrige » — remplacé par une correction dans le même tour. Et chaque « où est défini ce port » résolu par navigation plutôt que par `grep` puis lecture de plusieurs fichiers candidats. Voir aussi § 7. |
| **ArchUnit** | Traduit le principe hexagonal de la `constitution.md` en test : aucun import d'infrastructure dans le domaine, aucun port nommé d'après un système (`DmsPort` interdit). | La dérive architecturale silencieuse, découverte tard et corrigée cher. C'est l'assurance la plus rentable du projet. |
| **Spotless** + `palantir-java-format` | Formatage imposé, appliqué par `mvn spotless:apply`. | Zéro token dépensé en style, zéro bruit dans les diffs. |
| **Error Prone + NullAway** | Nullité vérifiée à la compilation. | Le contrat Delos impose « clé absente plutôt que `null` ». NullAway rend cette règle exécutable au lieu de déclarative. |
| **WireMock** | Simule le DMS depuis des fixtures enregistrées, **et les 5 API Tahoré non encore livrées**. | Le domaine devient testable *aujourd'hui*, sans attendre la livraison IT Kereis. C'est ce qui débloque le développement en parallèle. |
| **Spring Cloud Contract** (ou Pact) | Tests de contrat sur les 5 développements NEW. | À la livraison réelle, le test dit immédiatement si l'API correspond à la spécification. Sans cela, l'écart se découvre en recette. |
| **Testcontainers** | Base et dépendances réelles en test d'intégration. | Les faux positifs des mocks de persistance. |
| **ApprovalTests** | Test à fichier de référence : une ligne de test, le fichier *est* l'assertion. | Sur un mapping de 72 champs, remplace environ 72 assertions écrites à la main. Gain direct en tokens de sortie, et le fichier de référence est un **artefact lisible par le métier**. |

---

## 4. Les règles métier comme donnée — le levier « ressource métier »

C'est ici que se joue la réduction du temps métier, et le dispositif existe déjà.

**Le classeur `CU3_Sequence_Controles_APIs_v1.xlsx` contient 80 assertions de contrôle réparties sur
14 fiches. C'est la spécification exécutable du module.**

1. **Générer les squelettes de test depuis l'Excel.** Un script lit l'onglet « Fiches de contrôle »
   et produit un test JUnit par assertion, avec l'identifiant de règle (`RG-x.y.z`), un
   `@DisplayName` **en français reprenant le libellé du critère d'acceptation**, et un corps
   `@Disabled` à remplir. Conséquence : le métier valide un tableur qu'il sait lire, et le code en
   dérive. Il ne relit jamais de Java.
2. **Rapport de tests comme document de recette.** Le rapport Surefire, avec des noms de tests en
   français alignés sur les CA, se présente en réunion de validation. Un test rouge est une règle
   non couverte, visible sans traduction.
3. **Paramètres métier en YAML, pas en moteur de règles.** Délais, seuils, table de routage des
   motifs de DR : `@ConfigurationProperties` avec validation Bean Validation. Un fichier YAML
   commenté en français, modifiable sans recompiler.

> **Pourquoi pas Drools ni Camunda DMN.** Une table de décision DMN éditable par le métier est
> séduisante, mais coûte une dépendance, un moteur à apprendre et une chaîne de test propre — hors
> de portée de 800 $ à trois. À garder comme évolution *si* le métier demande un jour à éditer les
> tables lui-même. Le YAML couvre le besoin actuel.

**Ce qui reste à demander au métier est déjà borné** : l'onglet « Points à trancher » du contrat
Delos (1 bloquant, 2 importants, 6 à confirmer) et la liste de points ouverts de `CLAUDE.md`.
Aucune autre question ne devrait remonter.

---

## 5. Réglages Claude Code — l'essentiel est intégré

Avant tout outil tiers : **Claude Code mesure déjà sa propre consommation et expose les réglages qui
comptent.** Ce sont les gains les moins chers du projet.

### Mesurer

| Commande | Ce qu'elle donne |
|---|---|
| **`/usage`** | Tokens de la session en cours, détaillés en entrée / sortie / lecture de cache / écriture de cache, avec le coût. Sur un plan Pro/Max/Team/Enterprise, ajoute une **attribution par skill, sous-agent, plugin et serveur MCP**, et signale les comportements qui pèsent 10 % ou plus de la consommation récente — notamment **« long context » et « cache misses »**. C'est un diagnostic gratuit qui désigne le gaspillage. |
| **`/context`** | Ce qui occupe la fenêtre de contexte, poste par poste. À lancer dès qu'une session paraît lourde. |
| **`/insights`** | Rapport HTML sur les habitudes de travail à partir des sessions locales (jusqu'à 200) : points de friction, requêtes mal comprises, suggestions. Écrit dans `~/.claude/usage-data/report.html`. |
| **Statusline** | Peut afficher l'occupation du contexte en continu. À configurer pour les trois postes. |

### Le piège du cache à cinq minutes

Point critique pour ce projet. **Sur une clé API, la durée de vie du cache est de cinq minutes par
défaut** (une heure sur un abonnement). Passé ce délai, le premier message suivant reprocesse tout
le contexte au tarif plein — c'est le facteur 2,5 du § 1 qui s'évapore.

Deux conséquences pratiques :

- **Travailler par salves continues** plutôt qu'en picorant. Une pause café au milieu d'une session
  coûte un rechargement complet du contexte.
- **Vérifier si `ENABLE_PROMPT_CACHING_1H=1` s'applique à notre mode d'authentification.** La
  documentation le présente pour les crédits d'usage ; à tester sur clé API. À noter : l'écriture
  de cache en TTL 1 h coûte 2 × le prix d'entrée contre 1,25 × en TTL 5 min — l'arbitrage penche
  pour 1 h dès que le contexte est relu plus de deux fois.

### Borner le contexte par variable d'environnement

Deux variables officielles plafonnent la croissance du contexte, donc le coût d'entrée de chaque
tour. Elles sont le remède direct au signal « long context » de `/usage`.

| Variable | Effet | Réglage proposé |
|---|---|---|
| `CLAUDE_CODE_AUTO_COMPACT_WINDOW` | Taille de la fenêtre avant compaction automatique, de `100000` à `1000000` tokens. Entier simple obligatoire — `500k` est lu comme `500` puis ramené au minimum. | Un module de 5 000 à 8 000 lignes n'a aucun besoin d'une fenêtre à 1 M. La borner bas force la compaction tôt et plafonne mécaniquement le coût par tour. |
| `CLAUDE_AUTOCOMPACT_PCT_OVERRIDE` | Pourcentage de cette fenêtre (1 à 100) auquel la compaction se déclenche. Ne peut que **abaisser** le seuil, jamais le relever. | Compacter plus tôt coûte une requête de résumé mais évite de transporter un contexte gonflé à chaque message suivant. S'applique aussi aux sous-agents. |

Deux autres variables sont citées par la page « Manage costs » sans figurer dans la référence des
variables d'environnement : `ENABLE_PROMPT_CACHING_1H` (durée de cache portée à 1 h) et
`MAX_THINKING_TOKENS` (budget de réflexion sur les modèles à budget fixe). **À tester plutôt qu'à
supposer**, la première étant potentiellement le plus gros levier du projet — voir le piège du cache
ci-dessus.

### Réduire le contexte de base

- **`CLAUDE.md` doit descendre sous 200 lignes.** C'est la recommandation officielle, et notre
  fichier en fait plus de 300 : il est chargé à chaque démarrage de session, y compris pour des
  tâches qui n'en utilisent qu'un dixième. Les parties spécialisées (tableau complet des chemins
  JSON du DMS, table détaillée des API Tahoré, liste des documents) doivent devenir des **skills**,
  chargées à la demande.
- **`CLAUDE.md` doit rester stable.** Le cache fonctionne par correspondance de préfixe : le
  modifier dix fois par jour invalide tout ce qui suit. Ce qui bouge (tâche courante, notes de
  session) va ailleurs.
- **Instructions de compaction dans `CLAUDE.md`** — une section `# Compact instructions` indique
  quoi préserver lors du résumé automatique.
- **`/clear` entre deux tâches sans rapport.** Gratuit, et supprime le contexte mort que chaque
  message suivant transporte. `/rename` avant, `/resume` pour revenir.

### Décharger le travail verbeux

- **Hook de filtrage sur les sorties Maven.** Un hook `PreToolUse` sur `Bash` réécrit la commande
  pour ne remonter que les échecs. Sur ce projet, une exécution `mvn test` complète représente des
  milliers de lignes dont dix comptent :

  ```bash
  if [[ "$cmd" =~ ^(mvn|\./mvnw) ]]; then
    filtered_cmd="$cmd 2>&1 | grep -E '(ERROR|FAIL|Tests run:|BUILD)' | head -100"
  fi
  ```

- **Sous-agents pour les opérations volumineuses.** La sortie verbeuse reste dans le contexte du
  sous-agent, seul le résumé revient. Un sous-agent peut déclarer `model: haiku` dans sa
  configuration — à vérifier si Haiku est accessible (§ 9).
- **Outils CLI plutôt que serveurs MCP** quand les deux existent : pas de listage d'outils à porter
  en contexte. `/mcp` pour désactiver ce qui ne sert pas ; le panneau `/plugin` affiche un **coût en
  contexte** par plugin avant installation.

### Régler l'effort et éviter les surcoûts

- **`/effort`** — les tokens de réflexion sont facturés en sortie. Sur les tâches simples
  (formatage, génération répétitive), baisser l'effort. Réserver le niveau haut à l'architecture et
  aux règles métier délicates.
- **Mode plan (Shift+Tab) avant toute implémentation non triviale.** C'est le remède direct au
  risque identifié au § 1 : le modèle explore et propose une approche à valider avant d'écrire, ce
  qui évite la reprise.
- **`/rewind` ou double Échap** pour revenir à un point de reprise plutôt que de faire corriger une
  mauvaise piste.
- **Pas d'agent teams sur ce budget.** La documentation indique environ **7 × plus de tokens** que
  les sessions standard, chaque coéquipier maintenant sa propre fenêtre de contexte. Désactivé par
  défaut : le laisser désactivé.

---

## 6. Outils de suivi communautaires

Ils complètent `/usage` sur ce qu'il ne couvre pas : l'historique inter-sessions et l'affichage
permanent. Tous lisent les journaux JSONL locaux, sans envoyer de données.

| Outil | Rôle | Quand l'installer |
|---|---|---|
| **`ccusage`** (`npx ccusage@latest`) | Rapports `daily`, `weekly`, `monthly`, `session` et `blocks` (fenêtres de facturation de 5 h). Export `--json`, groupement par projet avec `--instances`. Aucune installation nécessaire. | **Dès le début.** C'est le suivi de consommation des trois postes, et le tableau de bord hebdomadaire à présenter. `--instances` isole ce que coûte le module CU#3. |
| **`ccstatusline`** | Statusline configurable (contexte, coût, modèle) affichée en permanence. | **Dès le début** — le retour immédiat est ce qui change les habitudes. |
| **Claude-Code-Usage-Monitor** | Suivi temps réel avec prévisions et alertes de seuil. | Si l'enveloppe se tend et qu'il faut un garde-fou par développeur. |
| **`ccflare`** | Tableau de bord web. | Optionnel, si un suivi partagé est demandé. |

> Ces outils sont communautaires : ni audités ni maintenus par Anthropic. `ccusage` s'exécute par
> `npx` sans installation, ce qui limite l'exposition.

---

## 7. Les plugins de réduction de tokens — ce qui vaut le coût

Ils existent, et se rangent en trois familles de valeur très inégale. La contrainte qui domine le
choix : **nous intervenons dans l'environnement d'un client, sur un périmètre qui touche des données
de santé sous RGPD.** Anthropic prévient explicitement qu'un plugin exécute du code arbitraire avec
les privilèges de l'utilisateur, et qu'elle ne peut ni contrôler ni vérifier son contenu. Le critère
n'est donc pas le gain seul, mais le gain rapporté à ce qu'une DSI peut accepter.

### Famille 1 — Compression des sorties d'outils : le seul gain vraiment démontré

La bonne catégorie, parce qu'elle attaque le plus gros poste évitable : les milliers de lignes qu'une
commande renvoie et dont dix comptent.

**RTK (Rust Token Killer)** — proxy CLI qui intercepte et compresse la sortie de plus de 100 commandes
de développement avant qu'elle n'atteigne le modèle. Il s'installe comme hook Claude Code réécrivant
les commandes Bash vers leur équivalent `rtk` : aucun surcoût de contexte par commande, et la
couverture s'étend aux sous-agents.

Ce qui le distingue des autres candidats : **des chiffres méthodologiquement présentables** — 89 % de
bruit retiré en moyenne sur plus de 2 900 commandes réelles, avec le détail par commande (`cargo test`
91,8 %, `git status` 80,8 %, `find` 78,3 %). Et un profil de sécurité défendable : binaire Rust
unique, compilé statiquement, sans dépendance externe.

Deux réserves à lever avant de le proposer :

- **Maven ne figure pas dans les commandes citées** (git, cargo, npm, ls, cat, find). C'est
  précisément notre première source de verbosité. À vérifier ; à défaut, le hook écrit à la main au
  § 5 couvre `mvn` de façon certaine, pour dix lignes de shell et zéro dépendance.
- Outil tiers : validation DSI comme le reste.

**Verdict : commencer par le hook maison sur `mvn`** — gratuit, auditable, immédiat. Évaluer RTK
ensuite si la sortie d'autres commandes pèse dans `/usage`.

### Famille 2 — Compression du contexte conversationnel : mauvais compromis ici

**claude-rolling-context** résume les anciens messages en conservant les récents mot pour mot, au lieu
de remplacer toute la conversation par un résumé comme le fait `/compact`. L'idée est bonne.
L'implémentation ne passe pas ici :

- C'est un **proxy local qui intercepte tout le trafic entre Claude Code et l'API Anthropic**
  (port 5588). Un intercepteur sur l'ensemble des requêtes IA, dans un environnement client touchant
  des données de santé : ce n'est pas une conversation à avoir avec la DSI de Kereis.
- 29 étoiles, un mainteneur. Surface de risque disproportionnée au gain.
- Le gain ne se matérialise **qu'au-delà de 100 k tokens de session**, et les projections annoncées
  sont théoriques, pas mesurées. Or nos sessions ne devraient pas atteindre ce volume si `/clear` est
  utilisé entre deux tâches.

**Verdict : non.** Les deux variables ci-dessus plus `/clear` obtiennent l'essentiel du même effet,
officiellement et sans rien intercepter.

### Famille 3 — RAG sur le dépôt : les 90 % ne sont pas mesurés

**token-reducer** annonce « 90 %+ » de réduction par récupération hybride (BM25 et vecteurs),
découpage AST et reranking, indexé localement en SQLite. Trois raisons de passer :

- **Le chiffre n'est pas étayé** : la démonstration est un schéma « 50 000 tokens → 500 », sans
  méthodologie. 44 étoiles, 25 commits.
- C'est exactement l'approche que le § 1 écarte : indexer sémantiquement un dépôt de 5 000 à
  8 000 lignes à structure imposée ne rembourse pas sa mise en place ni son maintien.
- Il annonce une analyse du graphe d'imports « sans serveur de langage » : version dégradée de ce que
  `jdtls-lsp` fait officiellement et mieux.

**Verdict : non.**

### La correction que je dois à la version précédente de ce document

J'avais écarté en bloc les outils de navigation de code. La conclusion tenait pour la famille 3, mais
j'avais manqué le bon outil : **le plugin officiel `jdtls-lsp`**, qui branche le serveur de langage
Java. Ce n'est pas un index sémantique à entretenir, c'est le mécanisme qui fait l'intelligence de
code de VS Code, et il apporte deux choses qu'aucun outil de cette section ne donne :

1. **Les diagnostics automatiques après chaque édition** — erreurs de type, imports manquants — sans
   lancer Maven. C'est la thèse du § 3 : un diagnostic déterministe et gratuit au lieu d'un
   aller-retour facturé. Sur du code Java généré (MapStruct, records, DTO OpenAPI), le compilateur a
   beaucoup à dire, et il le dit dans le même tour.
2. **La navigation par symbole** — définition, références, hiérarchie d'appels — au lieu d'un `grep`
   suivi de la lecture de plusieurs fichiers candidats.

Installation : `/plugin install jdtls-lsp@claude-plugins-official`, après installation du binaire
`jdtls` (le plugin ne l'installe pas). Marketplace officielle Anthropic : le seul de cette section à
ne poser aucune question de chaîne d'approvisionnement.

### Ce que contiennent vraiment les marketplaces Claude Code

Les trois familles ci-dessus mélangent des outils de nature différente. En s'en tenant strictement
aux **plugins Claude Code**, installables par `/plugin install`, la situation est la suivante.

**La marketplace officielle Anthropic n'a pas de catégorie « réduction de tokens ».** Ses catégories
sont : intelligence de code, intégrations externes, revue de sécurité automatique, workflows de
développement, styles de sortie. **La seule qui réduise effectivement la consommation est
l'intelligence de code** — et Anthropic la présente explicitement comme réduisant les lectures de
fichiers inutiles. Pour nous, c'est `jdtls-lsp`. Il n'y a pas d'autre plugin officiel dont l'objet
soit la réduction de tokens.

**La marketplace communautaire** (`anthropics/claude-plugins-community`) est vaste — plusieurs
milliers d'entrées — mais filtrée : chaque plugin a passé une analyse de sécurité automatisée et une
approbation manuelle, et il est épinglé à un commit précis. C'est un niveau de confiance sensiblement
supérieur à un dépôt GitHub quelconque. On y trouve notamment **`agent-memory`**, qui annonce « 60 à
90 % d'économie sur les coûts de tokens par compression de mémoire ». Même remarque que pour
token-reducer : l'annonce n'est pas une mesure, et le mécanisme (mémoire persistante entre sessions)
répond à un besoin de continuité longue que nous n'avons pas sur un module livré en un mois.

**Précision sur RTK** : ce n'est pas un plugin de marketplace mais un binaire CLI branché par un hook.
C'est justement ce qui en fait le candidat le plus solide — le travail se fait hors du modèle.

### L'anatomie d'un plugin décide de sa valeur

Un plugin Claude Code empaquette des skills, des agents, des hooks, des serveurs MCP ou LSP. **Le
type de composant prédit s'il réduit ou augmente la consommation** :

| Composant dominant | Effet réel |
|---|---|
| **Hooks** | **Réduit.** Le travail est fait hors du modèle, en shell : filtrage, compression, pré-traitement. Rien n'entre en contexte. |
| **Serveur LSP** | **Réduit.** Remplace des lectures de fichiers par des requêtes précises, et fournit les diagnostics gratuitement. |
| **Skills** | **Ambigu, souvent contre-productif.** Une skill est du texte de prompt : elle **coûte des tokens pour demander d'en consommer moins**. Défendable seulement si elle reste dormante et n'est invoquée que ponctuellement. |
| **Serveurs MCP** | **Augmente**, sauf si les définitions d'outils restent différées. Un serveur ajoute son inventaire au contexte. |

Autrement dit, un plugin dont l'argumentaire est « optimisation de tokens » mais qui livre
essentiellement des skills se paie lui-même en contexte. C'est le piège de cette catégorie.

### La bonne méthode : mesurer au lieu de croire les README

Claude Code donne de quoi trancher sans faire confiance à personne :

- Le panneau `/plugin`, onglet **Discover**, affiche un **coût en contexte estimé** pour chaque plugin
  **avant** installation, avec l'inventaire de ce qui sera installé (commandes, agents, skills, hooks,
  serveurs MCP et LSP).
- `claude plugin details <nom>` donne le coût projeté **ventilé entre permanent et à l'invocation**.
  C'est la seule donnée qui compte : le coût permanent est payé à chaque tour.
- L'onglet **Installed** signale sous **Not used recently** les plugins non utilisés depuis deux
  semaines sur au moins dix sessions, avec une ligne **Last used**. Ce sont des plugins qui coûtent
  du contexte et du démarrage pour rien : à désactiver.

**Protocole proposé** : mesurer une session type avec `/usage`, installer un candidat, remesurer la
même session type. Un plugin qui n'améliore pas le chiffre est retiré. C'est trois quarts d'heure de
travail qui évitent d'installer six plugins sur la foi de leurs README.

### Récapitulatif

| Piste | Gain | Risque | Décision |
|---|---|---|---|
| `jdtls-lsp` (officiel) | Diagnostics gratuits, navigation précise | Aucun — marketplace officielle | **Oui, étape 1** |
| Hook maison de filtrage `mvn` | Élimine la source de verbosité n° 1 | Aucun — dix lignes de shell | **Oui, étape 1** |
| `CLAUDE_CODE_AUTO_COMPACT_WINDOW` et `..._PCT_OVERRIDE` | Plafonne le contexte par tour | Aucun — officiel | **Oui, étape 1** |
| RTK | 89 % de bruit CLI en moins, mesuré | Tiers ; binaire statique, validation DSI | À évaluer après le hook |
| claude-rolling-context | Théorique, au-delà de 100 k tokens | Proxy interceptant tout le trafic IA | Non |
| token-reducer | Annoncé à 90 %, non mesuré | RAG local à maintenir | Non |
| `agent-memory` (communautaire) | Annoncé de 60 à 90 %, non mesuré | Marketplace filtrée, mais mémoire persistante inutile ici | Non |
| Plugins « optimisation » à base de skills | Négatif le plus souvent | Coûtent du contexte permanent | Non |

Le constat général mérite d'être retenu : **les gains les plus sûrs ne viennent pas d'un plugin à
installer mais d'une configuration à poser** — hooks, skills, LSP, bornes de compaction, `CLAUDE.md`
allégé, salves de travail continues. Les plugins tiers qui promettent 90 % attaquent soit un problème
que nous n'avons pas (sessions de plus de 100 k tokens), soit un problème déjà mieux traité
officiellement.

---

## 8. Ordre d'installation

Séquence pensée pour que chaque étape rende la suivante moins chère.

| # | Étape | Pourquoi maintenant |
|---|---|---|
| 1 | **Poste de travail** : `jdtls-lsp`, `ccstatusline`, hook de filtrage Maven, `CLAUDE.md` ramené sous 200 lignes, salves de travail continues | Ce sont les réglages qui rendent tous les tours suivants moins chers. Les faire après, c'est payer plein tarif entre-temps. |
| 2 | Squelette Maven + Spotless + ArchUnit + Error Prone/NullAway | Les garde-fous doivent précéder le code, pas le suivre. C'est ce qui empêche la reprise. |
| 3 | Génération du client DMS depuis le Swagger | Débloque tout le reste et supprime la plus grosse masse de code à dicter. |
| 4 | Génération des records du contrat Delos | Fige le contrat côté code avant que Delos ne livre. |
| 5 | Stubs WireMock : DMS et les 5 API Tahoré NEW | Rend le domaine testable sans dépendance externe. |
| 6 | Génération des tests depuis l'Excel des 80 assertions | Transforme la spécification métier en harnais de test. |
| 7 | Implémentation du domaine, test rouge par test rouge, en mode plan pour tout ce qui n'est pas trivial | À ce stade, chaque erreur est signalée par un outil. |
| 8 | Adaptateurs MapStruct | Les mappings manquants sont des erreurs de compilation. |
| 9 | Tests de contrat sur les API NEW à leur livraison | Vérifie l'écart spécification / livraison en une exécution. |

Les étapes 1 à 6 sont massivement industrialisables : **à mener avant le 31 août** pour bénéficier du
tarif d'introduction.

Rituel à tenir : **`ccusage daily` chaque matin, `/usage` à la fin de chaque session lourde.** Les
signaux « long context » et « cache misses » de `/usage` désignent directement ce qui dérive.

---

## 9. Points à confirmer

1. **Le budget de 800 $ est-il une enveloppe API, ou inclut-il des abonnements Claude Code ?** La
   réponse change tout. Sur abonnement, le cache dure une heure au lieu de cinq minutes, `/usage`
   affiche l'attribution par skill / sous-agent / plugin, et la logique de coût par token laisse la
   place à une logique de quota par siège.
2. **Haiku 4.5 est-il vraiment indisponible ?** À 1 $ / 5 $ il coûte un tiers de Sonnet, et les
   sous-agents peuvent déclarer `model: haiku` indépendamment du modèle principal. Router le travail
   mécanique dessus étirerait nettement l'enveloppe. Tu indiques Sonnet seul — je le prends comme
   une contrainte, mais elle vaut d'être revérifiée.
3. **`ENABLE_PROMPT_CACHING_1H=1` fonctionne-t-il sur notre mode d'authentification ?** À tester
   dès le premier jour : c'est potentiellement la plus grosse fuite silencieuse du projet.
4. **L'API Batch est-elle accessible** depuis l'environnement client ? 50 % de réduction sur les
   passes non interactives (génération des 72 tests de mapping, relecture des 80 assertions).
5. **Le client accepte-t-il ces dépendances ?** openapi-generator, MapStruct, ArchUnit, WireMock et
   Testcontainers sont standard dans un projet Spring Boot, mais une DSI peut imposer une liste
   blanche. Idem pour l'installation de plugins Claude Code et du binaire `jdtls` sur les postes. À
   valider avant l'étape 1, pas après.

---

## Sources

- [Manage costs effectively — Claude Code Docs](https://code.claude.com/docs/en/costs)
- [Discover and install prebuilt plugins — Claude Code Docs](https://code.claude.com/docs/en/discover-plugins)
- [ccusage](https://github.com/ryoppippi/ccusage)
- [ccstatusline](https://github.com/sirmalloc/ccstatusline)
- [Claude-Code-Usage-Monitor](https://github.com/Maciek-roboblog/Claude-Code-Usage-Monitor)
- [RTK — Rust Token Killer](https://github.com/rtk-ai/rtk)
- [claude-rolling-context](https://github.com/NodeNestor/claude-rolling-context)
- [token-reducer](https://github.com/madhan230205/token-reducer)
- [Environment variables — Claude Code Docs](https://code.claude.com/docs/en/env-vars)
- [Marketplace communautaire Anthropic](https://github.com/anthropics/claude-plugins-community)
