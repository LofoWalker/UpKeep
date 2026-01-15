#!/usr/bin/env python3
"""
Script to create a GitHub PR for the current branch
"""
import subprocess
import json
import os
import sys

# Configuration
REPO_OWNER = "LofoWalker"
REPO_NAME = "UpKeep"
BRANCH_NAME = "feat/monorepo-hexagonal-architecture-setup"
BASE_BRANCH = "main"

def get_github_token():
    """Get GitHub token from environment or git config"""
    token = os.getenv("GITHUB_TOKEN")
    if token:
        return token

    # Try to get from git config
    try:
        result = subprocess.run(
            ["git", "config", "--get", "github.token"],
            capture_output=True,
            text=True
        )
        if result.stdout.strip():
            return result.stdout.strip()
    except:
        pass

    return None

def run_command(cmd):
    """Run a shell command"""
    result = subprocess.run(cmd, shell=True, capture_output=True, text=True)
    return result.stdout.strip(), result.stderr.strip(), result.returncode

def get_branch_status():
    """Check if branch exists locally and locally commits"""
    stdout, _, code = run_command(f"git log --oneline {BRANCH_NAME} -1")
    if code != 0:
        print(f"❌ Branch {BRANCH_NAME} not found locally")
        return False

    print(f"✓ Branch {BRANCH_NAME} exists")
    print(f"  Latest commit: {stdout}")
    return True

def push_branch():
    """Push the branch to origin"""
    print(f"\n📤 Pushing branch {BRANCH_NAME}...")
    stdout, stderr, code = run_command(f"git push origin {BRANCH_NAME} --force")

    if code != 0:
        print(f"❌ Push failed: {stderr}")
        return False

    print(f"✓ Branch pushed successfully")
    return True

def create_pr_via_gh_cli():
    """Try to create PR using GitHub CLI (gh)"""
    print("\n🔗 Creating PR via gh CLI...")

    # Check if gh is installed
    _, _, code = run_command("which gh")
    if code != 0:
        print("❌ GitHub CLI (gh) not installed. Install it with: brew install gh")
        return False

    # Authenticate if needed
    run_command("gh auth status")

    # Create PR
    cmd = f"""gh pr create \
        --repo {REPO_OWNER}/{REPO_NAME} \
        --title "feat: implement monorepo with hexagonal architecture setup (US 1.1)" \
        --body "## Description
Implements the initial monorepo structure with hexagonal architecture scaffolding.

## Changes
- Create monorepo structure with npm workspaces
- Setup React + Vite + TypeScript frontend (apps/web)
- Setup Quarkus + Java backend with hexagonal architecture (apps/api)
- Configure TailwindCSS and design tokens for frontend
- Implement feature-first organization for frontend
- Add health check endpoint as driving adapter example
- Add base classes and ports for hexagonal architecture

## Acceptance Criteria
- ✅ AC #1: Monorepo structure with React/Vite and Quarkus
- ✅ AC #2: Hexagonal architecture in backend
- ✅ AC #3: Feature-first organization in frontend
- ✅ AC #4: npm workspace configuration
- ✅ AC #5: Dependencies configured (npm install ready)

## Type of Change
- [x] New feature (non-breaking change which adds functionality)" \
        --head {BRANCH_NAME} \
        --base {BASE_BRANCH}
    """

    stdout, stderr, code = run_command(cmd)

    if code != 0:
        print(f"❌ PR creation failed: {stderr}")
        return False

    print(f"✓ PR created successfully!")
    print(f"  {stdout}")
    return True

def main():
    print("🚀 GitHub PR Creation Script")
    print(f"Repository: {REPO_OWNER}/{REPO_NAME}")
    print(f"Branch: {BRANCH_NAME} → {BASE_BRANCH}\n")

    # Check branch exists
    if not get_branch_status():
        sys.exit(1)

    # Push branch
    if not push_branch():
        print("⚠️ Push failed, but attempting to create PR anyway...")

    # Create PR
    if create_pr_via_gh_cli():
        print("\n✅ All done!")
        sys.exit(0)
    else:
        print("\n⚠️ PR creation failed. You may need to create it manually.")
        print(f"Visit: https://github.com/{REPO_OWNER}/{REPO_NAME}/compare/{BASE_BRANCH}...{BRANCH_NAME}")
        sys.exit(1)

if __name__ == "__main__":
    main()

