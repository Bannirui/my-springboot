cmake_minimum_required(VERSION 3.25)

project(my-springboot Java)

# 检测jre版本 java -version的结果不是标准输出 而是标准错误 需要重定向
set(check_java java)
set(jre_ver_require 21)
execute_process(
        COMMAND sh -c "${check_java} -version 2>&1"
        OUTPUT_VARIABLE _v1
        ERROR_VARIABLE _v2
        OUTPUT_STRIP_TRAILING_WHITESPACE
)

if (_v2)
    message(FATAL_ERROR 检测${check_java}版本号出错, ${_v2})
endif ()

if (NOT _v1)
    message(FATAL_ERROR 没有检测到${check_java}版本信息)
endif ()

# 字符串处理提取版本号 找到x.x的字符串
string(REGEX MATCH "([0-9]+\.[0-9]+)" check_java_ver ${_v1})
if (NOT ${check_java_ver} GREATER_EQUAL ${jre_ver_require})
    message(FATAL_ERROR "${check_java}版本号不符合要求, 需要${jre_ver_require}, 实际为${check_java_ver}")
endif ()

message(STATUS "java版本号为${check_java_ver}")

# 检测maven
find_program(my_maven
    NAMES maven mvn)

if (${my_maven} STREQUAL "my_maven-NOTFOUND")
    message(FATAL_ERROR "No maven!")
endif ()

if (APPLE)
    set(my_home "/Users/dingrui")
elseif (LINUX)
    set(my_home "/home/dingrui")
else ()
    message(FATAL_ERROR "Unknow OS!")
endif ()

# 检测maven配置文件
set(my_maven_conf "${my_home}/.m2/settings.xml")
if (NOT EXISTS ${my_maven_conf})
    message(FATAL_ERROR "maven settings.xml not found!")
endif ()

message(STATUS "maven用户配置文件是${my_maven_conf}")

add_custom_target(
        package
        ALL
        COMMAND mvn clean package -U -s ${my_maven_conf} -f ${PROJECT_SOURCE_DIR}/pom.xml -Dmaven.test.skip=true -Dmaven.compile.source=${jre_ver_require} -Dmaven.compiler.target=${jre_ver_require}
        WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}
        COMMENT "maven打包"
)