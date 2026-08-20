# Deploiement

## Peut-on avoir une instance de PAASv3 en local ?

**Non, et ce n'est pas la bonne cible.** Une PaaS d'entreprise n'est pas un produit qu'on installe
sur un poste : c'est un assemblage de composants operes (orchestrateur, registre, routeur d'entree,
gestion des secrets, journalisation, observabilite) avec les conventions maison par-dessus. Meme si
l'image et les manifestes etaient publics, reproduire la chaine complete couterait plus que ce
qu'elle apporterait.

**Ce qui est reproductible localement, et qui suffit : le contrat.** Un deploiement echoue presque
toujours pour l'une des sept memes raisons, independamment de la PaaS. Elles sont testables sur un
simple Docker :

| # | Point du contrat | Pourquoi une PaaS le rejette |
|---|---|---|
| 1 | Le conteneur ne tourne pas en root | Politique de securite, quasi systematique sur images durcies |
| 2 | Le systeme de fichiers racine peut etre en lecture seule | `readOnlyRootFilesystem` frequent ; l'appli doit n'ecrire que dans un volume |
| 3 | Sonde de disponibilite (`readiness`) | Sans elle, la PaaS route du trafic vers une instance qui demarre encore |
| 4 | Sonde de vivacite (`liveness`) | Sans elle, une instance figee n'est jamais remplacee |
| 5 | Journaux sur la sortie standard | Une PaaS collecte stdout/stderr, pas des fichiers |
| 6 | Configuration entierement par variables d'environnement | Une image qui embarque sa configuration n'est pas promouvable d'un environnement a l'autre |
| 7 | Arret propre sur SIGTERM | Sinon chaque redeploiement coupe des traitements en cours |

`./verifier-contrat-paas.sh` verifie ces sept points sur l'image construite. Un module qui les passe
se deploie sur a peu pres n'importe quelle PaaS, PAASv3 comprise.

## Ce qu'il nous manque

Trois informations a demander a l'equipe PAASv3, et qui conditionnent la suite :

1. **Sur quoi PAASv3 repose-t-il ?** Kubernetes ou OpenShift, Cloud Foundry, ou une base
   propre ? Si c'est Kubernetes, `kind` ou `k3d` en local reproduit fidelement sondes, limites,
   ConfigMaps et Secrets, et `deploy/kubernetes/deployment.yaml` devient directement applicable.
2. **Quel format de manifeste ou de descriptif de service** attend la plateforme, et ou il se place
   dans le depot.
3. **Comment l'image `base-jdk25-dhi-alpine` est mise a disposition** : registre, authentification,
   et s'il existe un miroir accessible hors du reseau client. C'est le seul element de la stack
   cible que nous ne pouvons pas reproduire aujourd'hui.

En attendant ces reponses, `deploy/kubernetes/deployment.yaml` sert de cible neutre : il exprime le
contrat ci-dessus sous une forme executable, sans pretendre etre le manifeste PAASv3.

## Bascule d'image

Le `Dockerfile` prend l'image de base en argument :

```bash
# Silamir, developpement
docker build -t tahore-document-processing:0.1.0-SNAPSHOT .

# Client, integration
docker build --build-arg BASE_IMAGE=base-jdk25-dhi-alpine \
  -t tahore-document-processing:0.1.0-SNAPSHOT .
```

Aucun autre fichier ne change entre les deux environnements.
