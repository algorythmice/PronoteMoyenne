# PronoteMoyenne

Application Android (minSdk 24, targetSdk 36) pour consulter rapidement ses notes Pronote, devoirs et informations élève, avec sélection d'établissement assistée par géolocalisation et génération du QR Code TurboSelf.

## Fonctionnalités principales
- Connexion Pronote via ENT (liste préremplie + détection automatique d'URL Pronote par établissement).
- Récupération des notes courantes, calcul de la moyenne générale et affichage par matière (cache local pour consultation rapide).
- Récupération et affichage des devoirs à venir, regroupés par date et matière (cache local).
- Fiche infos élève (nom, classe, établissement) synchronisée avec Pronote.
- Onglet TurboSelf : génération du QR code de passage cantine (ou QR démo `demonstration`/`turboself`).
- Sélection d'établissement via proximité GPS ou recherche manuelle, liste intégrée depuis `assets/etablissements.json`.

## Architecture rapide
- `app/` : application Android principale (Kotlin, ViewBinding, Fragments).
  - `MainActivity` : écran de connexion ENT/Pronote + sélection d'établissement.
  - `HomeActivity` : conteneur navigation (drawer) pour `GradesFragment`, `HomeworksFragment`, `InfosFragment`, `TurboSelfFragment`.
  - `pronote/PronoteUtils` : appel Pronotekt, parsing des notes/devoirs, mapping ENT.
  - `grades/`, `homeworks/`, `infos/` : fragments + caches `SharedPreferences`.
  - `turboself/` : dépôt `TurboselfRepository`, stockage identifiants/QR, génération du bitmap.
  - `Utils` : helpers localisation, parsing JSON établissements, calculs moyenne, formats de date.
- Dépendances clés : [Pronotekt](https://github.com/algorythmice/pronotekt), [TurboSelf API](https://github.com/algorythmice/turboself-api), Gson, ZXing (QR), Google Play Services Location, Kotlinx Coroutines.

## Permissions & données
- `INTERNET`, `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION`, `READ/WRITE_EXTERNAL_STORAGE` (hérités). La localisation est optionnelle mais nécessaire pour la recherche d'établissement à proximité.
- Stockage local via `SharedPreferences` pour identifiants, URL Pronote, caches notes/devoirs/infos, QR TurboSelf.

## Configuration rapide
1) Lancer l'app : renseigner identifiants ENT/Pronote et choisir l'établissement (GPS ou recherche manuelle). Les identifiants sont sauvegardés via `LoginStorage`.
2) Notes/Devoirs : synchronisation via `PronoteUtils.syncPronoteData`; les données sont mises en cache et réaffichées automatiquement.
3) TurboSelf : saisir les identifiants TurboSelf (ou utiliser `demonstration` / `turboself` pour la démo) puis récupérer le QR code.

## Construire et exécuter
Depuis la racine du projet :
```bash
./gradlew :app:assembleDebug
./gradlew :app:installDebug
```
Ou depuis Android Studio, ouvrir le projet et exécuter la configuration `app`.

## Structure des modules
- `:app` (présent) — APK principal.
- `:Pronotekt`, `:TurboselfAPI` — modules bibliothèques présents en local pour intégration, mais sources officielles hébergées dans des dépôts séparés : [Pronotekt](https://github.com/algorythmice/pronotekt) et [TurboSelf API](https://github.com/algorythmice/turboself-api). L'app consomme directement les artefacts Maven déclarés dans `libs.versions.toml`.

## Tests
- Dépendances de test configurées (`junit`, `androidx.test.ext:junit`, `espresso-core`). Aucun test UI/unit fourni actuellement.

## Points d'attention
- La liste des ENT est codée en dur (`Const.kt`).
- La détection d'ENT repose sur la réflexion Pronotekt (`PronoteUtils.getEntFromString`).
- Les calculs de moyenne rebasculent les notes sur 20 et appliquent les coefficients d'origine.
- Les permissions localisation doivent être accordées pour la recherche automatique d'établissement.
