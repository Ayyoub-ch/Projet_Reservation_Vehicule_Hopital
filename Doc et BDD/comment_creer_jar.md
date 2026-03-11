# Comment créer un fichier JAR

## Étapes pour créer un JAR

### 1. Compiler le projet
```bash
javac App/*.java
```
Cela crée les fichiers `.class` dans le dossier `App/`.

### 2. Créer un fichier Manifest (optionnel mais recommandé)
Crée le fichier `manifest.txt` à la racine du projet avec le contenu suivant :
```
Main-Class: App
Class-Path: lib/*
```

### 3. Créer le fichier JAR

#### Method A : Avec Manifest (recommandée)
```bash
jar cfm reservationvehicule.jar manifest.txt App/*.class
```

#### Method B : Sans Manifest
```bash
jar cf reservationvehicule.jar App/*.class
```

### 4. Ajouter les dépendances (si nécessaire)
Si tu veux inclure les dépendances dans le JAR:
```bash
jar uf reservationvehicule.jar -C lib .
```

### 5. Exécuter le JAR
```bash
java -jar reservationvehicule.jar
```

## Exemple complet

```bash
# Compiler
javac App/*.java

# Créer le manifest
echo Main-Class: App > manifest.txt
echo Class-Path: lib/* >> manifest.txt

# Créer le JAR
jar cfm reservationvehicule.jar manifest.txt App/*.class

# Exécuter
java -jar reservationvehicule.jar
```

## Remarques
- Remplace `App` par le nom de ta classe principale (celle qui contient `main()`)
- Le fichier JAR peut être placé dans `Doc et BDD/` ou n'importe où
- Utilise `jar cvf` au lieu de `jar cf` pour voir le détail de la création
