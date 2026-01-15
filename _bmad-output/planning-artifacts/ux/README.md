# UX Artifacts — Index

**Projet** : Upkeep  
**Version** : MVP  
**Dernière mise à jour** : Janvier 2026

---

## 📁 Fichiers

| Fichier | Description | Écrans |
|---------|-------------|--------|
| `wireframes-company-onboarding.md` | Flow d'onboarding Company jusqu'à la première allocation | 8 |
| `wireframes-maintainer-flow.md` | Flow Maintainer : claim, vérification, dashboard | 10 |
| `wireframes-company-dashboard.md` | Dashboard Company post-onboarding + Public Sponsorship Page | 6 |
| `wireframes-admin-support.md` | Outils Admin : Payout Runs, Support Queue, Disputes | 10 |
| `design-system.md` | Tokens, composants, patterns pour l'implémentation | — |

**Total** : 34 écrans wireframés

---

## 🎯 Couverture User Journeys (PRD)

| Journey | Persona | Statut | Fichier |
|---------|---------|--------|---------|
| Journey 1 : First Allocation | Alex (Company) | ✅ Complet | `wireframes-company-onboarding.md` |
| Journey 2 : Unclaimed Package | Alex (Company) | ✅ Complet | `wireframes-company-dashboard.md` |
| Journey 3 : Claim Package | Lina (Maintainer) | ✅ Complet | `wireframes-maintainer-flow.md` |
| Journey 4 : Payout Runs | Samira (Ops) | ✅ Complet | `wireframes-admin-support.md` |
| Journey 5 : Support/Disputes | Jules (Support) | ✅ Complet | `wireframes-admin-support.md` |

---

## 🧱 Custom Components à implémenter

### Shared (tous les flows)

| Composant | Priorité | Usage |
|-----------|----------|-------|
| `ProgressStepper` | P0 | Onboarding multi-step |
| `PackageCard` | P0 | Affichage package partout |
| `PayoutStatusBadge` | P0 | Statut paid/held/failed |
| `EmptyState` | P0 | États vides avec CTA |
| `StatsCard` | P1 | KPIs dashboards |
| `FileDropzone` | P1 | Upload lockfile |

### Company-specific

| Composant | Priorité | Usage |
|-----------|----------|-------|
| `BudgetBar` | P0 | Visualisation budget/allocated |
| `GuardrailBadge` | P0 | Règles min 3 / max 34% |
| `AllocationEditor` | P0 | Interface d'allocation |
| `MonthNavigator` | P1 | Navigation mensuelle dashboard |

### Admin-specific

| Composant | Priorité | Usage |
|-----------|----------|-------|
| `TimelineEvent` | P1 | Investigation timeline |
| `TicketCard` | P1 | Support queue |
| `SplitPane` | P1 | Conversation + Context |
| `ComparisonTable` | P2 | Disputes |

---

## 📐 Layout Templates

| Template | Usage |
|----------|-------|
| `OnboardingLayout` | Steps avec progress bar |
| `DashboardLayout` | Navbar + Tabs + Content |
| `AdminLayout` | Navigation admin différente |
| `PublicPageLayout` | Sponsorship page (header + content) |

---

## 📱 Responsive Strategy

| Viewport | Stratégie |
|----------|-----------|
| Desktop (1024px+) | Full experience, optimisé |
| Tablet (768-1023px) | Adapté, fonctionnel |
| Mobile (< 768px) | Readable, interactions simplifiées |

**Note MVP** : Focus desktop-first. Mobile = consultable mais pas optimisé pour les workflows complexes (allocation, admin).

---

## 🚀 Prochaines étapes

1. **Validation** — Review avec stakeholders si nécessaire
2. **Prototype** — Transposer en Figma pour tests utilisateurs (optionnel)
3. **Implémentation** — Commencer par les composants P0

---

## 📋 Checklist pré-dev

- [x] Wireframes Company Onboarding
- [x] Wireframes Maintainer Flow
- [x] Wireframes Company Dashboard
- [x] Wireframes Admin/Support
- [x] Design System défini
- [ ] Review stakeholder (optionnel)
- [ ] Setup shadcn/ui
- [ ] Créer stubs des composants custom

