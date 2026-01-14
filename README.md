# Secure File Manager (CLI) – Java

## Contexte
Ce projet consiste à développer un **gestionnaire de fichiers sécurisé en ligne de commande (CLI)** en Java, sans interface graphique.  
Le développement est réalisé de manière **itérative**, avec des fonctionnalités ajoutées à chaque itération.
Développement itératif :
- Itération 1 : CRUD + navigation dans un répertoire autorisé + erreurs + CLI
- Itération 2 : intégrité (hash) + traçabilité (logs)
- Itération 3 : chiffrement + gestion des clés

## Prérequis
- Java 17+

## Compilation
Depuis le dossier `Livrables` :
```bash
javac -d bin -cp "src/sqlite-jdbc-3.51.1.0.jar" $(find src -name "*.java")

javac -d ./bin/ ./src/Main.java ./src/application/*.java ./src/cli/*.java ./src/domain/exception/*.java ./src/domain/repository/*.java ./src/domain/model/*.java ./src/infrastructures/filesystem/*.java ./src/infrastructures/security/*.java ./src/infrastructures/logging/*.java

Exécution
```bash
java -cp "bin:src/sqlite-jdbc-3.51.1.0.jar" Main
```
## Commandes disponibles (Itération 1)

help : affiche l’aide
pwd : affiche le répertoire courant (relatif au root autorisé)
ls : liste les fichiers et dossiers
cd <dossier> : se déplacer dans un sous-dossier
create <fichier> : créer un fichier
read <fichier> : lire le contenu d’un fichier
write <fichier> : écrire ou modifier un fichier
delete <fichier> : supprimer un fichier
exit : quitter l’application

## Gestion du périmètre autorisé

L’application fonctionne dans un répertoire racine autorisé défini au lancement.
Toute tentative de sortie de ce périmètre (ex : ../) est bloquée afin de garantir la sécurité du système de fichiers.


## Gestion des erreurs

Les erreurs courantes sont prises en compte :
- fichier ou dossier inexistant,
- permissions insuffisantes,
- commande invalide,
- tentative d’accès hors du répertoire autorisé.
Les messages affichés à l’utilisateur sont clairs et l’application ne s’arrête pas brutalement.

## Architecture du projet

L'application est structurée en 4 modules :

### CLI
Le module CLI (Command Line Interface) est responsable de l'interaction entre l'utilisateur et l'application. Il permet à l'utilisateur d'exécuter des commandes pour gérer des fichiers de manière sécurisée. **Aucun traitement métier n'y est effectué**. Voici les principales classes et fonctionnalités de ce module :

#### CommandLineInterface
Cette classe gère l'interface en ligne de commande. Elle initialise les services nécessaires, comme `FileService` pour la gestion des fichiers et `WorkingContext` pour maintenir le contexte de travail actuel. La méthode `start()` lance la boucle principale de l'application, où l'utilisateur peut entrer des commandes.

- **Fonctionnalités :**
    - Affichage d'un message de bienvenue et d'aide via `MenuRenderer`.
    - Lecture des entrées utilisateur et traitement des commandes.
    -

#### MenuRenderer
Cette classe est responsable de l'affichage des messages à l'utilisateur. Elle fournit des méthodes statiques pour afficher le message de bienvenue et l'aide.

### Application
Le module Application est le cœur de l'application, orchestrant les différentes fonctionnalités et cas d'utilisation. Il interagit avec les modules CLI et Infrastructure(via le Domain).

#### FileService
Cette classe gère les opérations liées aux fichiers, telles que la création, la suppression et la lecture de fichiers. Elle utilise le module Domaine pour appliquer les règles de gestion et garantir l'intégrité des opérations.

- **Fonctionnalités :**
    - `createFile(String filename)` : Crée un nouveau fichier avec le nom spécifié.
    - `deleteFile(String filename)` : Supprime le fichier spécifié.
    - `readFile(String filename)` : Lit le contenu du fichier spécifié.
    - `createRepository(String directoryName)` : Crée un nouveau répertoire pour organiser les fichiers.

#### WorkingContext
Cette classe maintient le contexte de travail actuel de l'utilisateur, en s'assurant qu'il reste dans le répertoire autorisé. Elle transforme les entrées de l'utilisateur en chemins sûrs et gère les déplacements dans le système de fichiers.

- **Fonctionnalités :**
    - `pwd()` : Affiche le répertoire courant.
    - `resolve(String input)` : Résout un chemin d'entrée en un chemin sûr.
    - `moveTo(Path newPath)` : Déplace le contexte de travail vers un nouveau chemin.

Ce module assure que toutes les opérations de fichiers sont effectuées dans un cadre sécurisé, respectant les principes de la triade CIA.

### Domaine
Définit les entités métier, les règles de gestion et la logique métier fondamentale.
Dans se dossier il n'y a pas vraiment de code, c'est un dossier où tout est défini, il ne dépend de personne mais l'infrastructure et l'application l'utilise pour comuniquer entre eux

### Infrastructure
c'est l'endroit où tout les accée vers l'exterieur sont réèlement fait, l'application appelle ces méthode via le domaine 


## Journalisation

Pour chaque action faite sur l'application (CRUD, erreur, ...) je stock :
La date exacte de l'action
Quel utilisateur à fait l'action
Quel est l'action
Quel fichier est touché par l'action