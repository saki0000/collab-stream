# CollabStream

> Synchronize two streaming archive videos by timestamp for collaborative viewing experiences

## 🎯 What is CollabStream?

**CollabStream enables viewers to watch two streaming archive videos perfectly synchronized by timestamp. Experience collaborative streams, tournaments, and multi-perspective content from different creators simultaneously.**

### ✨ Key Features
- **Dual-Video Sync**: Watch 2 archive videos perfectly aligned by timestamp (second-level precision)
- **Cross-Platform**: Android, iOS, Web, and Desktop support via Kotlin Multiplatform
- **YouTube & Twitch**: Select and synchronize archives from both major platforms
- **Simple Selection**: Choose any two archived streams to sync and enjoy together
- **Free Service**: Complete functionality available at no cost

### 🎮 Perfect For
- **Tournament Coverage**: Watch multiple streamers covering the same esports event from different perspectives
- **Collaborative Streams**: Experience group streams where creators covered identical content together
- **Gaming Communities**: Enjoy shared viewing experiences for special events and competitions
- **Content Comparison**: Compare different creators' approaches to the same game or challenge

### 📺 How It Works
1. **Select First Archive**: Choose a YouTube or Twitch archived stream
2. **Select Second Archive**: Pick another archived stream to sync with
3. **Auto-Sync**: CollabStream automatically synchronizes both videos by timestamp
4. **Enjoy Together**: Play, pause, and seek - both videos stay perfectly in sync

### 🔮 Future Plans
- Subscription plans with premium features
- Enhanced synchronization options
- Additional platform integrations

---

## 🛠️ For Developers

### Development Workflow

```bash
# 1. 要件策定 → GitHub Issue作成
/create-issue

# 2. Issue → 完全自動実装 → PR作成
/implement-issue https://github.com/owner/repo/issues/123
```

## 📱 Project Structure

This is a Kotlin Multiplatform project with automated development workflows:

### Core Modules

* **[/composeApp](./composeApp/src)** - Compose Multiplatform UI code shared across platforms
  - [commonMain](./composeApp/src/commonMain/kotlin) - Common UI code for all targets
  - [androidMain](./composeApp/src/androidMain/kotlin) - Android-specific UI implementations
  - [iosMain](./composeApp/src/iosMain/kotlin) - iOS-specific UI implementations  
  - [wasmJsMain](./composeApp/src/wasmJsMain/kotlin) - Web-specific UI implementations

* **[/shared](./shared/src)** - Business logic shared between all targets
  - [commonMain](./shared/src/commonMain/kotlin) - Core business logic, entities, repositories
  - [androidMain](./shared/src/androidMain/kotlin) - Android platform-specific implementations
  - [iosMain](./shared/src/iosMain/kotlin) - iOS platform-specific implementations
  - [wasmJsMain](./shared/src/wasmJsMain/kotlin) - Web platform-specific implementations

* **[/server](./server/src/main/kotlin)** - Ktor server application with API endpoints

* **[/iosApp](./iosApp/iosApp)** - iOS application entry point and SwiftUI integration

### Build Commands

```bash
# Web Development
./gradlew :composeApp:wasmJsBrowserDevelopmentRun

# Android Build  
./gradlew :composeApp:assembleDebug

# Server
./gradlew :server:run

# Tests
./gradlew test
```

## 🤖 Automated Development Workflow

### Architecture Overview

```
┌─────────────────────────────────────────┐
│          Design & Planning              │
│     (/create-issue command)             │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│       GitHub Issue Management          │
│    (GitHub CLI integration)            │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│     Automated Implementation           │
│    (/implement-issue command)          │
├─────────────────────────────────────────┤
│ ┌─────────────┐ ┌─────────────────────┐ │
│ │   Shared    │ │    Compose + Server │ │
│ │  (Sequential) │ │    (Parallel)       │ │
│ └─────────────┘ └─────────────────────┘ │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│        Quality Assurance              │
│   (Tests + Builds + Integration)        │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│         Pull Request Creation           │
│      (Automated PR generation)         │
└─────────────────────────────────────────┘
```

## 🔄 Complete Development Flow

### Phase 1: Design & Requirements

#### `/create-issue` - Interactive Requirements Gathering

```bash
/create-issue
```

**What it does:**
1. **Interactive Q&A** - 16 structured questions covering all aspects
2. **Design Doc Generation** - Creates comprehensive technical documentation
3. **GitHub Issue Creation** - Automatic issue creation with proper labels
4. **Template-based** - Uses proven design doc templates

**Generated Artifacts:**
- `docs/design-doc/{feature-name}.md` - Comprehensive design document
- GitHub Issue with `feature`/`maintenance` labels
- Technical requirements and acceptance criteria

**Question Categories:**
- Basic Information (name, scope, classification)
- Implementation Background (problems, strategic alignment)
- Technical Specifications (platforms, components, users)
- Goals & Success Metrics
- Technical Architecture & API Design
- Risk Assessment & Alternative Solutions

### Phase 2: Issue Analysis & Task Breakdown

#### `/implement-issue` - Automated Implementation

```bash
/implement-issue https://github.com/owner/repo/issues/123
# or
/implement-issue 123  # for current repository
```

**Automated Process:**

#### Step 1: Issue Analysis
- **GitHub CLI Integration** - Fetches issue details, labels, assignees
- **Context Creation** - Sets up `docs/context/{issue-number}/` workspace
- **Technical Requirements** - Extracts Kotlin Multiplatform requirements

#### Step 2: Intelligent Task Breakdown
- **task-breakdown-specialist Agent** - AI-powered layer analysis
- **3-Layer Architecture** - Separates concerns (shared/compose/server)
- **Platform Analysis** - Identifies Android/iOS/Web/Server requirements
- **Dependency Mapping** - Determines implementation order and parallel opportunities

### Phase 3: Parallel Implementation Strategy

#### Git Worktree Management
```bash
# Automatic worktree creation based on dependency analysis
git worktree add ../CollabStream-shared feature/issue-123
git worktree add ../CollabStream-server feature/issue-123
```

**Implementation Flow:**
1. **Shared Layer** (Sequential) - Foundation implementation
   - Entities, repositories, use cases
   - expect/actual platform abstractions
   - Unit tests with 90%+ coverage

2. **Compose + Server Layers** (Parallel) - Application layers
   - **Compose Layer**: UI screens, ViewModels, navigation
   - **Server Layer**: API endpoints, business logic integration
   - Platform-specific implementations
   - Comprehensive testing for each layer

3. **Integration Layer** (Sequential) - Final integration
   - End-to-end testing
   - Cross-platform build verification
   - Performance and quality checks

### Phase 4: Quality Assurance

#### Automated Quality Gates
```bash
# Automated execution for each layer
./gradlew :shared:test      # Unit tests
./gradlew :composeApp:test  # UI tests  
./gradlew :server:test      # API tests

# Integration verification
./gradlew build             # All platforms
./gradlew test              # Complete test suite
```

**Quality Standards:**
- **Unit Test Coverage**: 80%+ overall, 90%+ for business logic
- **Build Success**: All platforms (Android/iOS/Web/Server)
- **Code Quality**: Automated ktlint (via commit hooks)
- **Error Handling**: Maximum 3 retry attempts with user intervention

### Phase 5: Pull Request Automation

#### Automatic PR Creation
- **Existing `/pr` Integration** - Leverages established PR workflow
- **Context-Rich Descriptions** - Auto-generated from implementation context
- **Issue Linking** - Automatic close-issue-on-merge setup
- **Review-Ready State** - All quality gates passed before PR creation

## 🏗️ Technical Implementation

### Agent-Based Architecture

#### Core Agents
```
.claude/agents/
├── task-breakdown-specialist.md    # Layer analysis & task decomposition
├── kotlin-backend-specialist.md    # Server & shared layer implementation  
├── compose-multiplatform-specialist.md # UI layer implementation
└── knowledge/                       # Shared knowledge base
    ├── kotlin-multiplatform-patterns.md
    ├── layer-dependencies.md
    └── testing-patterns.md
```

#### Context Management System
```
docs/context/{issue-number}/
├── context.md              # Issue info & overall progress
├── analysis.md             # Requirements analysis
├── workflow-state.json     # Agent coordination state
├── tasks/
│   ├── shared-layer.md     # Core logic tasks
│   ├── compose-layer.md    # UI layer tasks
│   ├── server-layer.md     # API layer tasks
│   └── integration.md      # Integration tasks
└── implementation/
    ├── commits.md          # Git history
    ├── errors.md           # Error tracking
    └── verification.md     # Quality assurance
```

### Smart Parallel Execution

#### Dependency Analysis Algorithm
```typescript
interface ExecutionStrategy {
  phase1: ["shared_layer"]                    // Sequential foundation
  phase2: ["compose_layer", "server_layer"]  // Parallel application
  phase3: ["integration"]                     // Sequential final
}
```

**Automatic Strategy Selection:**
- **Simple Features** → Sequential single worktree
- **Medium Features** → Staged parallel execution
- **Complex Features** → Full parallel with multiple worktrees

### Error Recovery System

#### Intelligent Retry Logic
- **Compilation Errors** → 3 attempts with detailed logging
- **Test Failures** → Automatic test correction attempts
- **Build Failures** → Platform-specific diagnostics
- **Agent Failures** → Context preservation and manual intervention

#### State Management
- **Atomic Operations** → Each layer completion creates recovery point
- **Context Preservation** → All progress saved to `workflow-state.json`
- **Manual Override** → User can intervene at any recovery point

## 📊 Quality Metrics & Monitoring

### Performance Tracking
```yaml
Efficiency Metrics:
  - Implementation Time: Estimated vs. Actual
  - Parallel Execution Ratio: Concurrent vs. Sequential  
  - Agent Success Rate: Automated vs. Manual Intervention
  - Quality Gate Pass Rate: First-attempt vs. Retry

Quality Metrics:
  - Test Coverage: Per-layer and overall percentages
  - Build Success: Cross-platform compatibility
  - Code Quality: Lint compliance and complexity scores
  - Documentation: Auto-generated vs. manual content
```

### Continuous Improvement
- **Pattern Learning** → Successful implementations update knowledge base
- **Error Analysis** → Common failures enhance retry logic
- **Performance Optimization** → Execution time improvements
- **Template Evolution** → Design doc templates improve based on usage

## 🛠️ Configuration & Customization

### Environment Setup
```bash
# GitHub CLI (required)
gh auth login

# Gradle wrapper (included)
./gradlew --version

# Project initialization
/create-issue  # First-time setup guidance
```

### Workflow Customization

#### Agent Configuration
```yaml
# .claude/agents/task-breakdown-specialist.md
capabilities:
  - Issue analysis & requirements extraction
  - Layer-based task decomposition  
  - Parallel execution planning
  - Context-driven agent coordination
```

#### Quality Gates
```yaml
# docs/context/templates/workflow-state-template.json
quality_standards:
  test_coverage_minimum: 80%
  build_timeout_minutes: 10
  retry_limit: 3
  parallel_execution: true
```

## 📚 Knowledge Base

### Implementation Patterns
- **[Kotlin Multiplatform Patterns](/.claude/agents/knowledge/kotlin-multiplatform-patterns.md)**
- **[Layer Dependencies](/.claude/agents/knowledge/layer-dependencies.md)**  
- **[Testing Strategies](/.claude/agents/knowledge/testing-patterns.md)**

### Templates & Examples
- **[Design Doc Template](/docs/design-doc/template/design-doc-template.md)**
- **[Context Templates](/docs/context/templates/)**
- **[Issue Examples](/docs/examples/)** (created during usage)

## 🤝 Contributing & Team Workflow

### For Developers
```bash
# Individual feature development
/create-issue          # Design new feature
/implement-issue 123   # Implement designed feature

# Code review and collaboration  
gh pr review 456       # Review auto-generated PRs
git pull origin main   # Stay synchronized
```

### For Product Managers
```bash
# Requirements gathering
/create-issue          # Guided requirements collection
gh issue list          # Track implementation progress
gh pr list             # Monitor delivery pipeline
```

### For QA Engineers
- **Automated Testing** → All implementations include comprehensive tests
- **Quality Metrics** → Real-time visibility into coverage and quality
- **Integration Testing** → Cross-platform compatibility verification
- **Performance Monitoring** → Build time and execution metrics

## 🔗 External Resources

### Kotlin Multiplatform
- [Official Documentation](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)
- [Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform/#compose-multiplatform)
- [Kotlin/Wasm](https://kotl.in/wasm/)

### Community
- [#compose-web Slack Channel](https://slack-chats.kotlinlang.org/c/compose-web)
- [YouTrack Issues](https://youtrack.jetbrains.com/newIssue?project=CMP)

---

**Next Steps:**
1. Run `/create-issue` to design your first feature
2. Use `/implement-issue {number}` to auto-implement 
3. Review generated PRs and iterate

**Questions?** Check the [workflow documentation](./.claude/commands/) or create an issue for guidance.