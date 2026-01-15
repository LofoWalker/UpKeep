# Import des Wireframes dans Figma

## 📁 Fichiers HTML disponibles

| Fichier | Contenu | Écrans |
|---------|---------|--------|
| `company-onboarding.html` | Landing, Signup, Workspace, Budget, Import, Allocate, Success | 7 |
| `company-dashboard.html` | Dashboard Overview, Edit Allocation, Public Sponsorship Page | 3 |
| `maintainer-flow.html` | Landing Maintainer, Claim, Verify, Payout, Dashboard | 6 |

**Total : 16 écrans prêts pour l'import**

---

## 🚀 Instructions d'import dans Figma

### Méthode 1 : Plugin "HTML to Figma" (Recommandé)

1. **Installer le plugin** :
   - Dans Figma, Menu > Plugins > Browse plugins
   - Rechercher "HTML to Figma"
   - Installer le plugin de Builder.io

2. **Ouvrir les fichiers HTML dans un navigateur** :
   ```bash
   # Dans le terminal, depuis ce dossier
   cd /home/lofo/Work/Upkeep/_bmad-output/planning-artifacts/ux/figma-import
   
   # Option 1: Ouvrir directement
   xdg-open company-onboarding.html
   
   # Option 2: Servir avec un serveur local (meilleur rendu)
   npx serve .
   # Puis ouvrir http://localhost:3000/company-onboarding.html
   ```

3. **Importer dans Figma** :
   - Dans ton fichier Figma, lancer le plugin HTML to Figma
   - Coller l'URL du fichier HTML
   - Le plugin convertira automatiquement en frames Figma

### Méthode 2 : Screenshot + Auto Layout

Si le plugin ne fonctionne pas correctement :

1. **Ouvrir chaque HTML dans le navigateur** à 100% zoom
2. **Prendre des screenshots** de chaque frame (ils sont délimités)
3. **Importer dans Figma** comme images de référence
4. **Recréer par-dessus** avec Auto Layout

### Méthode 3 : Copier le CSS dans Figma Dev Mode

Les fichiers utilisent des variables CSS cohérentes :

```css
--primary: #2563eb
--success: #16a34a
--warning: #d97706
--error: #dc2626
--text-primary: #0f172a
--text-secondary: #475569
--border-default: #e2e8f0
```

Tu peux créer ces couleurs comme Figma Styles.

---

## 🎨 Conseils pour Figma

### Créer les Styles de base

1. **Couleurs** : Créer un Color Style pour chaque variable CSS
2. **Typographie** : 
   - Font: Inter
   - Styles: H1 (28px/700), H2 (24px/600), Body (14px/400), Caption (12px/400)
3. **Spacing** : Utiliser un grid de 8px

### Composants à créer en priorité

1. `Button` (Primary, Secondary, Ghost)
2. `Input` (avec label)
3. `StatsCard`
4. `PackageCard`
5. `StatusBadge` (Paid, Held, Pending)
6. `ProgressStepper`

---

## 📦 Structure recommandée dans Figma

```
Upkeep Wireframes
├── 🎨 Styles
│   ├── Colors
│   ├── Typography
│   └── Effects
├── 🧱 Components
│   ├── Buttons
│   ├── Inputs
│   ├── Cards
│   └── Navigation
├── 📱 Screens
│   ├── Company Onboarding
│   ├── Company Dashboard
│   ├── Maintainer Flow
│   └── Admin (à ajouter)
└── 🔄 Flows (prototyping)
```

---

## ⚡ Raccourci : Template Figma Community

Si tu préfères partir d'une base, ces kits Figma gratuits sont compatibles avec le design system :

- [Shadcn/ui Figma Kit](https://www.figma.com/community/file/1203061493325953101)
- [Tailwind UI Kit](https://www.figma.com/community/file/958383439532195363)

Ils utilisent les mêmes conventions de style.

---

## 🔧 Génération des écrans Admin

Les écrans Admin (Payout Runs, Support Queue) n'ont pas encore été convertis en HTML. Tu veux que je les crée aussi ?

