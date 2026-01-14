#!/bin/bash
#
# organize_screenshots.sh
# Roborazziで生成されたスクリーンショットをScreenとComponentに分類
#

set -e

# カラー出力
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# ディレクトリ設定
ROBORAZZI_OUTPUT="composeApp/build/outputs/roborazzi"
SCREENS_DIR="screenshots/screens"
COMPONENTS_DIR="screenshots/components"

echo -e "${BLUE}=== Screenshot Capture & Organizer ===${NC}"
echo ""

# Step 1: Roborazziでスクリーンショットを記録
echo -e "${YELLOW}Step 1: Recording screenshots with Roborazzi...${NC}"
./gradlew :composeApp:recordRoborazziDebug --quiet || {
    echo "Error: Failed to record screenshots"
    exit 1
}
echo -e "${GREEN}✓ Screenshots recorded${NC}"
echo ""

# Step 2: 出力ディレクトリを作成
echo -e "${YELLOW}Step 2: Creating output directories...${NC}"
mkdir -p "$SCREENS_DIR"
mkdir -p "$COMPONENTS_DIR"
echo -e "${GREEN}✓ Directories created${NC}"
echo ""

# Step 3: スクリーンショットを分類
echo -e "${YELLOW}Step 3: Organizing screenshots...${NC}"

screen_count=0
component_count=0

# Roborazzi出力ディレクトリが存在するか確認
if [ ! -d "$ROBORAZZI_OUTPUT" ]; then
    echo "Warning: Roborazzi output directory not found: $ROBORAZZI_OUTPUT"
    echo "No screenshots to organize."
    exit 0
fi

# スクリーンショットを分類
for file in "$ROBORAZZI_OUTPUT"/*.png; do
    [ -e "$file" ] || continue  # ファイルが存在しない場合はスキップ

    filename=$(basename "$file")

    # ファイル名に "Screen" が含まれているかチェック
    if [[ "$filename" == *"Screen"* ]]; then
        cp "$file" "$SCREENS_DIR/"
        ((screen_count++))
    else
        cp "$file" "$COMPONENTS_DIR/"
        ((component_count++))
    fi
done

echo -e "${GREEN}✓ Screenshots organized${NC}"
echo ""

# Step 4: 結果を表示
echo -e "${BLUE}=== Results ===${NC}"
echo -e "Screens (git tracked):     ${GREEN}$screen_count${NC} files → $SCREENS_DIR/"
echo -e "Components (git ignored):  ${YELLOW}$component_count${NC} files → $COMPONENTS_DIR/"
echo ""

# Screen一覧を表示
if [ $screen_count -gt 0 ]; then
    echo -e "${BLUE}Screen screenshots:${NC}"
    ls -1 "$SCREENS_DIR"/*.png 2>/dev/null | while read f; do
        echo "  - $(basename "$f")"
    done
    echo ""
fi

# Component一覧を表示（最大10件）
if [ $component_count -gt 0 ]; then
    echo -e "${BLUE}Component screenshots (showing up to 10):${NC}"
    ls -1 "$COMPONENTS_DIR"/*.png 2>/dev/null | head -10 | while read f; do
        echo "  - $(basename "$f")"
    done
    if [ $component_count -gt 10 ]; then
        echo "  ... and $((component_count - 10)) more"
    fi
    echo ""
fi

echo -e "${GREEN}Done!${NC}"
