---
allowed-tools: Bash(git status:*), Bash(git branch:*), Bash(git log:*), Bash(git push:*), Bash(gh pr create:*), Bash(git diff:*)
description: Create a pull request using GitHub template
---

## Context

- Current git status: !`git status`
- Current branch: !`git branch --show-current`
- Recent commits: !`git log --oneline -10`
- Pull request template: !`cat .github/pull_request_template.md`
- UI file changes: !`git diff --name-only origin/main...HEAD | grep -E 'composeApp/src/commonMain/kotlin/.*/ui/.*\.kt$' || echo "No UI changes"`

## Your task

1. First, push the current branch to remote repository with tracking
2. Check if there are UI file changes (files in `ui/` or `components/` directories)
3. If UI files were changed:
   - Run `/generate-previews` to ensure all Composables have @Preview functions
   - Include UI screenshot verification in the Test section
4. Create a pull request using the template from `.github/pull_request_template.md`
5. Fill in the template sections based on the commits and changes in this branch

## UI Change Guidelines

If UI files are modified:
- Run `/generate-previews` to auto-generate missing @Preview functions
- Ensure @Preview functions exist for Screen, Content, Component
- Container files do NOT need @Preview (ViewModel dependency)
- Run `/capture-screenshots` to verify visual changes