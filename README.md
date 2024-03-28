my-springboot
---

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

### 2 TODO

- [ ] 基于SpringBoot

- [ ] 统一场景Bean注入容器

- [ ] web场景

  - web开发最重要的是什么 sso

  - 将sso的实现看作黑盒
 
  - 框架要关心的是应用开发层面的注解 拿到要访问的资源信息 去做鉴权和跳转

- [ ] http场景

  - 这个场景要从众多的http client中挑选一个轻量级的 并且高性能的

- [ ] 远程配置中心

  - 要将项目的唯一标识符硬编码到resources中，作为配置中心的namespace标识

  - 启动的时候可以梭哈读到所有配置项缓存到spring的environment中

  - 注册监听器接收变更回调