# FullQuizz - Firebase Functions & Firestore Backend

Ce dossier contient le code serveur **Node.js (Firebase Cloud Functions v2)** pour orchestrer les parties multijoueurs en ligne, valider les réponses côté serveur et synchroniser les scores en temps réel.

---

## 🚀 Fonctions Disponibles

| Fonction | Type | Description |
| :--- | :--- | :--- |
| `createGameRoom` | Callable | Crée un salon de jeu avec un code PIN à 6 caractères, sélectionne et mélange les questions de la catégorie. |
| `joinGameRoom` | Callable | Permet au joueur 2 de rejoindre un salon existant via son code PIN et démarre la partie (`PLAYING`). |
| `submitAnswer` | Callable | Valide la réponse côté serveur, calcule les points (rapidité + combo) et met à jour Firestore en temps réel. |
| `onRoomUpdated` | Firestore Trigger | Déclenché à la fin d'une partie (`FINISHED`) pour incrémenter les stats et le classement mondial des joueurs. |
| `healthCheck` | HTTP Endpoint | Vérification de l'état du service (`GET /healthCheck`). |

---

## 🛠️ Déploiement en 1 Clic

### 1. Installation des dépendances
```bash
cd functions
npm install
```

### 2. Déployer sur votre projet Firebase
Assurez-vous d'être connecté à la CLI Firebase (`firebase login`) :
```bash
firebase deploy --only functions,firestore:rules
```

### 3. Tester en local avec l'Émulateur Firebase
```bash
npm run serve
```
L'interface de l'émulateur sera accessible sur `http://localhost:4000`.
