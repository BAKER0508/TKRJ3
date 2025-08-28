#!/bin/bash

# 智能题库记忆系统 - 快速构建脚本
# 使用方法: ./build.sh [debug|release]

set -e  # 遇到错误立即退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 打印带颜色的信息
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查构建环境
check_environment() {
    print_info "检查构建环境..."
    
    # 检查Java版本
    if ! command -v java &> /dev/null; then
        print_error "Java未安装或未添加到PATH"
        exit 1
    fi
    
    JAVA_VERSION=$(java -version 2>&1 | grep -oP 'version "\\K[0-9]+')
    if [[ $JAVA_VERSION -lt 11 ]]; then
        print_error "需要Java 11或更高版本，当前版本: $JAVA_VERSION"
        exit 1
    fi
    print_success "Java版本: $JAVA_VERSION"
    
    # 检查Android SDK
    if [[ -z "$ANDROID_HOME" ]]; then
        print_error "ANDROID_HOME环境变量未设置"
        exit 1
    fi
    
    if [[ ! -d "$ANDROID_HOME" ]]; then
        print_error "Android SDK路径不存在: $ANDROID_HOME"
        exit 1
    fi
    print_success "Android SDK: $ANDROID_HOME"
    
    # 创建local.properties
    if [[ ! -f "local.properties" ]]; then
        echo "sdk.dir=$ANDROID_HOME" > local.properties
        print_info "已创建local.properties文件"
    fi
}

# 清理构建目录
clean_build() {
    print_info "清理构建目录..."
    ./gradlew clean
    print_success "构建目录已清理"
}

# 构建APK
build_apk() {
    local build_type=${1:-debug}
    
    print_info "开始构建${build_type}版本APK..."
    
    # 设置构建参数
    local gradle_task=""
    local output_dir=""
    
    case $build_type in
        "debug")
            gradle_task="assembleDebug"
            output_dir="app/build/outputs/apk/debug"
            ;;
        "release")
            gradle_task="assembleRelease"
            output_dir="app/build/outputs/apk/release"
            ;;
        *)
            print_error "无效的构建类型: $build_type，支持: debug|release"
            exit 1
            ;;
    esac
    
    # 执行构建
    ./gradlew $gradle_task --stacktrace
    
    # 检查构建结果
    if [[ -d "$output_dir" ]]; then
        local apk_files=$(find "$output_dir" -name "*.apk" | wc -l)
        if [[ $apk_files -gt 0 ]]; then
            print_success "APK构建成功！"
            echo ""
            echo "构建产物："
            find "$output_dir" -name "*.apk" -exec ls -lh {} \;
        else
            print_error "APK文件未找到"
            exit 1
        fi
    else
        print_error "输出目录不存在: $output_dir"
        exit 1
    fi
}

# 运行测试
run_tests() {
    print_info "运行单元测试..."
    ./gradlew test
    
    print_info "运行Lint检查..."
    ./gradlew lint
    
    print_success "所有测试通过"
}

# 安装APK到设备
install_apk() {
    local build_type=${1:-debug}
    local apk_path=""
    
    case $build_type in
        "debug")
            apk_path=$(find "app/build/outputs/apk/debug" -name "*.apk" | head -1)
            ;;
        "release")
            apk_path=$(find "app/build/outputs/apk/release" -name "*.apk" | head -1)
            ;;
    esac
    
    if [[ -z "$apk_path" ]]; then
        print_error "APK文件未找到，请先构建"
        exit 1
    fi
    
    # 检查设备连接
    local device_count=$(adb devices | grep -v "List of devices" | grep -v "^$" | wc -l)
    if [[ $device_count -eq 0 ]]; then
        print_error "未找到已连接的Android设备"
        exit 1
    fi
    
    print_info "安装APK到设备: $apk_path"
    adb install -r "$apk_path"
    print_success "APK安装成功"
}

# 显示帮助信息
show_help() {
    echo "智能题库记忆系统 - 构建脚本"
    echo ""
    echo "使用方法:"
    echo "  $0 [command] [options]"
    echo ""
    echo "命令:"
    echo "  debug          构建Debug版本APK (默认)"
    echo "  release        构建Release版本APK"
    echo "  test           运行测试"
    echo "  clean          清理构建目录"
    echo "  install        安装APK到设备"
    echo "  help           显示此帮助信息"
    echo ""
    echo "示例:"
    echo "  $0 debug       # 构建Debug版本"
    echo "  $0 release     # 构建Release版本"
    echo "  $0 test        # 运行测试"
    echo "  $0 install debug   # 安装Debug版本到设备"
}

# 主函数
main() {
    local command=${1:-debug}
    
    print_info "智能题库记忆系统 - Android构建工具"
    print_info "========================================"
    
    case $command in
        "debug"|"release")
            check_environment
            build_apk "$command"
            ;;
        "test")
            check_environment
            run_tests
            ;;
        "clean")
            clean_build
            ;;
        "install")
            local build_type=${2:-debug}
            install_apk "$build_type"
            ;;
        "all")
            check_environment
            clean_build
            run_tests
            build_apk "debug"
            build_apk "release"
            ;;
        "help"|"-h"|"--help")
            show_help
            ;;
        *)
            print_error "未知命令: $command"
            show_help
            exit 1
            ;;
    esac
}

# 如果脚本被直接执行
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi