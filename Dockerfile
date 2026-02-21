# ============================================================
# CollabStream サーバー Dockerfile
# マルチステージビルドにより、ビルド環境とランタイム環境を分離する
# ============================================================

# ============================================================
# Stage 1: ビルドステージ
# Gradle を使用して Fat JAR を生成する
# ============================================================
FROM gradle:8-jdk17 AS build

WORKDIR /app

# Gradle のキャッシュを活用するため、依存関係ファイルを先にコピーする
# settings.gradle.kts には全モジュールの定義が含まれるため必須
COPY settings.gradle.kts .
COPY build.gradle.kts .
COPY gradle.properties .
COPY gradle/ gradle/
COPY gradlew .

# server モジュールは shared モジュールに依存するため、両方のビルドスクリプトをコピーする
COPY server/build.gradle.kts server/
COPY shared/build.gradle.kts shared/
COPY composeApp/build.gradle.kts composeApp/
COPY testing/build.gradle.kts testing/

# Gradle のキャッシュを事前に構築する（依存関係のダウンロード）
# ソースコードのコピー前に実行することでキャッシュを最大活用する
RUN gradle :server:dependencies --no-daemon || true

# ソースコードをコピーする
# server モジュールが shared モジュールに依存するため、両方必要
COPY server/src server/src
COPY shared/src shared/src

# Fat JAR を生成する
# Ktor Gradle プラグインが提供する buildFatJar タスクを使用する
RUN ./gradlew :server:buildFatJar --no-daemon

# ============================================================
# Stage 2: ランタイムステージ
# 軽量な JRE のみを含む Alpine ベースイメージを使用する
# ============================================================
FROM eclipse-temurin:17-jre-alpine AS runtime

WORKDIR /app

# ビルドステージから Fat JAR のみをコピーする
# これにより最終イメージを最小限のサイズに抑える
COPY --from=build /app/server/build/libs/server-all.jar app.jar

# サーバーが使用するポート番号
# Cloud Run は PORT 環境変数でポートを指定するため、デフォルト値として設定する
ENV PORT=8080

EXPOSE 8080

# JVM オプション:
# -XX:+UseContainerSupport: コンテナのメモリ制限を自動検出する
# -XX:MaxRAMPercentage=75.0: コンテナメモリの 75% を JVM ヒープに割り当てる
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-jar", \
    "app.jar"]
