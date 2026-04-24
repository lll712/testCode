# 台球厅管理系统模块说明

## 技术栈

- 前端：Vue 3 + Vite
- 后端：Spring Boot 3 + Spring Data JPA
- 数据库：MySQL
- 接口返回格式：`{ code, message, data }`

## 数据库脚本

- 完整建表与初始化数据：`backend/src/main/resources/sql/billiards_schema.sql`

## 已新增后端接口

### 1. 台球桌管理

- `GET /api/tables`
- `POST /api/tables`
- `PUT /api/tables/{id}`
- `DELETE /api/tables/{id}`

请求示例：

```json
{
  "tableNo": "A08",
  "tableName": "8号台",
  "areaName": "大厅A区",
  "status": "IDLE",
  "hourlyPrice": 58,
  "remark": "靠窗位置"
}
```

返回示例：

```json
{
  "code": 200,
  "message": "新增成功",
  "data": {
    "id": 4,
    "tableNo": "A08",
    "tableName": "8号台",
    "areaName": "大厅A区",
    "status": "IDLE",
    "hourlyPrice": 58.0,
    "remark": "靠窗位置"
  }
}
```

### 2. 预约管理

- `GET /api/reservations`
- `POST /api/reservations`
- `PUT /api/reservations/{id}`
- `PUT /api/reservations/{id}/cancel`

请求示例：

```json
{
  "userId": 2,
  "tableId": 1,
  "startTime": "2026-04-24T14:00:00",
  "endTime": "2026-04-24T16:00:00",
  "contactPhone": "13800000001",
  "remark": "提前10分钟到店"
}
```

### 3. 订单管理

- `GET /api/orders`
- `POST /api/orders`
- `PUT /api/orders/{id}/pay`

请求示例：

```json
{
  "userId": 2,
  "tableId": 1,
  "reservationId": 1,
  "startTime": "2026-04-24T14:00:00",
  "endTime": "2026-04-24T16:30:00",
  "paymentStatus": "UNPAID",
  "remark": "含饮品套餐"
}
```

说明：

- 后端自动计算 `durationMinutes`
- 后端自动计算 `amount`
- 支持支付状态 `UNPAID / PAID`

### 4. 用户管理

- `GET /api/users`
- `PUT /api/users/{id}`
- `PUT /api/users/{id}/disable`

请求示例：

```json
{
  "nickname": "张三-VIP",
  "phone": "13800000009",
  "email": "vip-zhangsan@example.com",
  "status": 1
}
```

### 5. 报表统计

- `GET /api/reports/summary?type=day`
- `GET /api/reports/summary?type=week`
- `GET /api/reports/summary?type=month`

返回字段说明：

- `label`：统计区间
- `totalMinutes`：总使用分钟数
- `usageRate`：使用率，百分比
- `income`：收入
- `orderCount`：订单数量

## 已新增前端文件

- `frontend/src/App.vue`
- `frontend/src/style.css`
- `frontend/src/api/request.js`
- `frontend/src/api/table.js`
- `frontend/src/api/reservation.js`
- `frontend/src/api/order.js`
- `frontend/src/api/user.js`
- `frontend/src/api/report.js`

## 页面功能说明

- 登录成功后切换到管理后台
- 单页包含 5 个业务标签页
- 表单和列表直接联动
- 样式延续原登录 demo 的渐变背景、玻璃卡片、圆角按钮风格

## 本地运行

1. 先执行 SQL：`backend/src/main/resources/sql/billiards_schema.sql`
2. 启动后端 Spring Boot
3. 启动前端 Vite
4. 使用默认账号 `admin / 123456` 登录

## 当前验证结果

- 前端生产构建已通过
- 当前环境未安装 `Java` 和 `Maven`，因此未能在本机完成后端编译验证
