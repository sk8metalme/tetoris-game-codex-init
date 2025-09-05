# Tetris (Java 21 + Spring Boot + Thymeleaf)

[![CI](https://github.com/sk8metalme/tetoris-game-codex-init/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/sk8metalme/tetoris-game-codex-init/actions/workflows/ci.yml)

PCブラウザ向けシングルプレイのテトリス実装（MVP）。オニオンアーキテクチャを採用し、ドメインからTDDで実装を進めています。

## 要件/スタック
- Java 21, Gradle
- Spring Boot 3, Thymeleaf（Web層は今後拡張）
- テスト: JUnit5 + JaCoCo（段階的に引き上げ: 現在 gate= line ≥ 95%, branch ≥ 90%）
- コードスタイル: Google Java Style（Spotless）

## セットアップ
```bash
# テスト実行（HTMLレポート: build/reports/tests/test/index.html）
gradle test

# カバレッジレポート（HTML: build/reports/jacoco/test/html/index.html）
gradle jacocoTestReport

# リント（google-java-format）
gradle lint

# 本番ビルド
gradle build

# 開発起動（現時点ではWebエンドポイント最小）
gradle bootRun
```

## 現在の進捗（抜粋）
- Domain: 回転SRS（最小）、Boardの`placeAndClear`、ScoreRule（基本スコア）、GameStateの基本操作（left/right/soft/hard drop）、7-bagジェネレータを実装済み
- Docs: 設計・テスト方針を docs/ 配下に整備（詳細は下記）

## リポジトリガイド
- ガイドライン: `AGENTS.md`
- コンテキスト管理: `docs/context.md`
- 開発計画: `docs/plan.md`
- 設計書: `docs/design.md`
- テスト計画: `docs/test-plan.md`
- Claude連携メモ: `docs/claude/` 配下

## コミット/PR
- Conventional Commits 推奨（例: `feat(core): add seven-bag generator`）
- 小さく焦点を絞ったPR、差分300行未満を推奨

## ライセンス
TBD（プロジェクトポリシーに従う）

## CI / Coverage Policy
- Lint: Spotless（Google Java Format）を必須化。
- Test: `gradle test` 成功を必須化。レポートは `build/reports/tests/test/index.html`。
- Coverage: JaCoCo gate（bundle基準）を設定。
  - 現在: Lines ≥ 95%, Branches ≥ 90%（`build.gradle` の `jacocoTestCoverageVerification`）。
  - 段階的に引き上げ、最終目標: Lines/Branches ≥ 95%。
- ドキュメントのみ更新のPRは `detect` ジョブにより build ジョブをスキップ。
