my-springboot
---

[TODO请看](./TODO.md)

A framework based on SpringBoot, focusing on different section

到了新公司，看了几个项目，有些地方比较费劲

- 脚本方式创建的脚手架项目

- 没有统一的项目框架，有Spring MVC的，有SpringBoot的

- 显式获取配置

- 远程调用需要显式传递调用方的meta信息

- 显式使用的Util工具过深依赖传递

因为上述原因，我觉得脚手架侧重于项目模板统一，而框架侧重于场景整合和开箱即用

### 1 特性

- 基于SpringBoot
- 统一配置
- 整合功能 注解式场景启动(@EnableXXX)
    - @EnableMsbFramework启用框架
    - @EnableMsbConfig启用配置中心(接入[携程的Apollo](https://github.com/apolloconfig/apollo.git))
    - @EnableMsbLog启用日志框架(接入[美团点评的Cat](https://github.com/dianping/cat.git))
- VM/程序启动参数开关
    - -Denv=xxx指定环境参数
        - dev
        - fat
        - uat
        - prod
    - -Dmsb.apollo=x
        - true框架本身启用apollo配置
        - false框架本身不适用apollo配置
    - --console.log=x
      - true 启用终端输出Banner
      - false 终端不输出Banner

### 2 quick start
- 启用框架
  - [doc](./doc/启用框架.md)
  - [code](./msb-samples/sample-01)
- 配置接入Apollo
  - [doc](./doc/读取远程配置-apollo.md)
  - [code](./msb-samples/sample-02)
- Apollo关联公共namespace
  - [doc](./doc/远程配置热更新.md)
  - [code](./msb-samples/sample-03)
- 控制台日志输出Banner
  - [doc](./doc/控制台输出Banner.md)
  - [code](./msb-samples/sample-04)
- 日志接入Cat
  - [doc](./doc/集成Cat.md)
  - [code](./msb-samples/sample-04)
