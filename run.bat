@echo off
chcp 65001 >nul
setlocal
cd /d "%~dp0"

echo ============================================
echo   dev-prep 启动脚本
echo   目录: %CD%
echo ============================================

where java >nul 2>&1
if errorlevel 1 (
  echo [ERROR] 找不到 java，请确认 JDK 17 已安装并加入 PATH
  goto :fail
)
where mvn >nul 2>&1
if errorlevel 1 (
  echo [ERROR] 找不到 mvn，请确认 Maven 已安装并加入 PATH
  goto :fail
)

if /i "%~1"=="--skip-build" goto :run

echo.
echo [1/2] 打包（跳过测试）...
if exist "backend\mvn-settings.xml" (
  call mvn -s backend\mvn-settings.xml -f backend\pom.xml -DskipTests clean package
) else (
  call mvn -f backend\pom.xml -DskipTests clean package
)
if errorlevel 1 (
  echo [ERROR] Maven 打包失败
  goto :fail
)

:run
echo.
echo [2/2] 启动应用...
echo   接口: http://localhost:8080/api/v1/hello?name=Harness
echo   文档: http://localhost:8080/swagger-ui.html
echo   停止: 按 Ctrl+C
echo.
java -jar backend\target\project-0.0.1-SNAPSHOT.jar
if errorlevel 1 goto :fail
exit /b 0

:fail
echo.
echo 启动失败，请查看上方错误信息。
pause
exit /b 1
