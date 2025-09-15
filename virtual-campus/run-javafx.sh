#!/bin/bash
set -euo pipefail

# 使用 JavaFX Maven 插件运行，以自动解析平台相关的 JavaFX 依赖
# 说明：此前脚本通过 --module-path 拼接依赖，无法正确加载平台特定的 JavaFX 本地库，
# 容易导致 “Module javafx.controls not found” 或本地库加载失败。
# 现在改为使用插件统一处理，保持在 IDE 与命令行的一致性。

echo "编译项目..."
./mvnw -q -DskipTests compile

echo "启动 JavaFX 应用 (mvn javafx:run)..."
./mvnw -q -DskipTests javafx:run

cat << 'EOF'
提示：
- 如果登录界面提示网络错误，请先在另一个终端启动后端：
    ./mvnw -q spring-boot:run
- 或者在 IDE 中运行 `VirtualCampusApplication`（取消注释 Spring Boot 主方法后）。
EOF
