# Plateforme de financement open-source

## 1. Contexte et problème

L’open-source est un pilier critique du logiciel moderne. Des millions d’entreprises utilisent quotidiennement des bibliothèques open-source pour faire fonctionner des produits à forte valeur économique.

Pourtant :

* Une grande partie de ces projets sont maintenus par **une ou deux personnes**.
* Le financement repose sur :

    * le bénévolat,
    * des sponsors ponctuels,
    * ou des modèles indirects (SaaS, consulting).
* Il existe un **décalage massif** entre l’usage économique réel d’un projet et la capacité de ses mainteneurs à en vivre.

Le problème n’est pas l’absence de bonne volonté, mais l’absence de **mécanisme structuré, rationnel et acceptable par les entreprises**.

---

## 2. Limites des solutions existantes

### GitHub Sponsors / OpenCollective

* Décision manuelle, projet par projet
* Découverte faible
* Aucun lien explicite avec l’usage réel en entreprise
* Approche émotionnelle ou opportuniste

### Modèles basés sur la popularité (stars, downloads)

* Fortement biaisés (dépendances transitives, CI/CD)
* Faciles à manipuler
* Favorisent les projets déjà dominants
* Ne reflètent ni le risque ni la criticité

### Résumé

Aucune solution ne permet aujourd’hui de :

> relier **usage réel**, **dépendance critique** et **financement structuré**.

---

## 3. Vision du projet

Créer une **plateforme générique de financement open-source**, orientée entreprises, dont l’objectif est :

> **Transformer la dépendance open-source en un financement durable des mainteneurs, basé sur le risque et l’usage réel.**

Ce n’est **ni une plateforme de dons**, ni un classement de popularité.
C’est un **mécanisme de révélation économique**.

---

## 4. Principe fondamental

### Postulat clé

Les entreprises sont prêtes à financer l’open-source **lorsque le risque est explicite, localisé et maîtrisable**.

La plateforme repose donc sur trois piliers :

1. **Usage réel déclaré** (dépendances effectives)
2. **Vote / allocation contrainte** (choix explicite)
3. **Financement de mainteneurs opt-in** (humains identifiés)

---

## 5. Acteurs

### 1. Entreprises

* Déclarent leurs dépendances open-source
* Allouent un budget mensuel
* Participent à un système de vote / allocation

### 2. Mainteneurs

* S’inscrivent volontairement
* Décrivent leur projet et leurs besoins
* Acceptent un cadre minimal (activité, contact, transparence)

### 3. Plateforme

* Agrège les signaux
* Encadre les règles
* Assure la redistribution et la traçabilité

---

## 6. Système de financement

### 6.1 Budget

Chaque entreprise définit :

* un budget mensuel open-source (ex : 500€, 2k€, 10k€)

Ce budget n’est **pas redistribué automatiquement**.
Il est **alloué par décision explicite**.

---

### 6.2 Système de vote / allocation

Chaque entreprise reçoit :

* un nombre fixe de points d’allocation par mois

L’allocation représente :

> “Voici les projets dont la défaillance nous mettrait en risque.”

Interprétation opérationnelle :

* les points servent à **répartir directement** le budget mensuel de l’entreprise (ex : 25 points = 25% du budget)
* la redistribution finale correspond à la somme des répartitions des entreprises, avec traçabilité

Règles :

* points non transférables
* votes révisables mensuellement
* impossibilité de voter pour ses propres projets
* **plafond par projet** (anti sponsor unique) : une entreprise ne peut pas allouer plus de **34%** de son budget mensuel à un seul package
* **dispersion minimale** : l’allocation doit couvrir au moins **N = 3** packages

---

### 6.3 Agrégation

Les fonds sont redistribués en fonction :

* du nombre d’entreprises dépendantes
* de l’intensité de leur allocation

Un projet soutenu par 20 entreprises reçoit mécaniquement plus qu’un projet soutenu par une seule.

---

## 7. Critères d’éligibilité des projets

### 7.1 Unité finançable

Pour relier usage réel et financement, l’unité finançable est le **package** (ex : `npm:tailwindcss`, `pypi:requests`, `maven:org.slf4j:slf4j-api`).

* Les entreprises déclarent leurs dépendances au niveau package.
* Les mainteneurs s’inscrivent (opt-in) pour recevoir des fonds associés à un ou plusieurs packages.

### 7.2 Éligibilité

Pour recevoir des fonds, un projet doit :

* être open-source
* avoir un ou plusieurs mainteneurs identifiés
* être actif (commits, issues, releases)
* accepter les règles de la plateforme

Si un package semble inactif, la plateforme affiche un **avertissement** afin d’orienter les entreprises (sans bloquer automatiquement les paiements en V1).

Optionnel (futur) :

* engagements de maintenance
* roadmap publique
* canal de communication dédié

---

## 8. Différenciation clé

La plateforme se distingue par :

* ❌ pas de classement par popularité
* ❌ pas de redistribution aveugle
* ✅ financement lié à un **risque partagé**
* ✅ décisions humaines, mais structurées

Ce n’est pas un système moral.
C’est un système **économique**.

---

## 9. Modèle de valeur pour les entreprises

### Ce qu’elles obtiennent

* visibilité sur leurs dépendances critiques
* réduction du bus factor
* relation directe avec les mainteneurs
* justification interne (CTO / DAF)

> “Nous ne donnons pas de l’argent.
> Nous sécurisons une dépendance.”

---

## 9.1 Transparence (acceptable entreprise)

Par défaut :

* public : uniquement des **agrégats** (ex : nombre d’entreprises finançant un package)
* mainteneurs : visibilité limitée (agrégats), sauf si une entreprise choisit d’être identifiable
* entreprises : trace complète en interne (historique, reçus, exports)

---

## 10. Modèle de valeur pour les mainteneurs

* revenus récurrents
* indépendance vis-à-vis d’un sponsor unique
* reconnaissance économique réelle
* alignement avec l’usage réel

---

## 11. Questions ouvertes (assumées)

* Cadre juridique exact (don vs contrat)
* Niveau d’engagement attendu des mainteneurs
* Gouvernance de la plateforme

Ces points sont **conçus pour être itératifs**, pas résolus dès la V1.

---

## 12. Vision long terme

À terme, la plateforme pourrait devenir :

* un standard de financement open-source en entreprise
* un indicateur de santé de l’écosystème
* un pont économique entre usage et maintenance

---

## 13. Résumé en une phrase

Une plateforme qui permet aux entreprises de financer durablement les mainteneurs open-source dont elles dépendent réellement, via un mécanisme de vote structuré et traçable.