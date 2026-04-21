# Scripts

这个目录专门放脚本和初始化文件，方便你集中修改。

## 文件说明

- `sql/init_mysql.sql`：初始化数据库表和演示账号
- `backend-dev.bat`：启动 Spring Boot 后端
- `frontend-dev.bat`：启动 Vue 前端

## 需要先改的地方

如果你的数据库名、账号、密码不是当前示例值，请先修改：

- `backend/src/main/resources/application.yml`
- `sql/init_mysql.sql`

