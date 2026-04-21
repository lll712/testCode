# Spring Boot + Vue Login Demo

一个简单的前后端分离示例项目：

- `backend`：Spring Boot 登录接口
- `frontend`：Vue 登录页面
- `scripts`：数据库初始化脚本和启动脚本

## 目录结构

```text
.
|-- backend
|-- frontend
|-- scripts
`-- README.md
```

## 功能说明

- 用户名密码登录
- 使用 MySQL 数据库读取用户数据
- Vue 页面调用 Spring Boot 接口
- 登录成功后显示欢迎信息和模拟 token

## 你需要修改的配置

后端数据库配置文件：

- `backend/src/main/resources/application.yml`

数据库初始化脚本：

- `scripts/sql/init_mysql.sql`

前端默认通过 Vite 代理访问后端：

- `frontend/vite.config.js`

## 启动步骤

1. 先创建数据库并执行 `scripts/sql/init_mysql.sql`
2. 修改 `backend/src/main/resources/application.yml` 中的数据库连接
3. 启动后端：执行 `scripts/backend-dev.bat`
4. 启动前端：执行 `scripts/frontend-dev.bat`

## 默认测试账号

- 用户名：`admin`
- 密码：`123456`

这个示例里的密码校验是明文对比，只适合演示。后续如果你要继续做正式项目，建议改成加密存储。

