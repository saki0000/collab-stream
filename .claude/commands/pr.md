---
allowed-tools: Bash(git status:*), Bash(git branch:*), Bash(git log:*), Bash(git push:*), Bash(gh pr create:*)
description: Create a pull request using GitHub template
---

## Context

- Current git status: !`git status`
- Current branch: !`git branch --show-current`
- Recent commits: !`git log --oneline -10`
- Pull request template: !`cat .github/pull_request_template.md`

## Your task

1. First, push the current branch to remote repository with tracking
2. Create a pull request using the template from `.github/pull_request_template.md`
3. Fill in the template sections based on the commits and changes in this branch