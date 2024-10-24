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

- 整合需要的功能

- 注解式场景启动(@EnableXXX)

- 封装实现，隐藏细节

### 2 quick start

- [启用框架](./doc/启用框架.md)

- [读取远程配置](./doc/读取远程配置-nacos)

- [关联公共namespace](./doc/远程配置热更新.md)

### 3 samples

- [框架启用](./msb-samples/sample-01)

- [读取远程配置](./msb-samples/sample-02)

- [关联公共namespace](./msb-samples/sample-03)
