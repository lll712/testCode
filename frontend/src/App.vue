<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from "vue";
import { login } from "./api/auth";
import {
  closeTable,
  createTable,
  deleteTable,
  fetchTables,
  openTable,
  updateTable
} from "./api/table";
import {
  fetchReservations,
  createReservation,
  updateReservation,
  cancelReservation
} from "./api/reservation";
import { fetchOrders, createOrder, payOrder } from "./api/order";
import { fetchUsers, updateUser, disableUser } from "./api/user";
import { fetchReportSummary } from "./api/report";

const tabs = [
  { key: "tables", label: "台球桌管理" },
  { key: "reservations", label: "预约管理" },
  { key: "orders", label: "订单管理" },
  { key: "users", label: "用户管理" },
  { key: "reports", label: "报表统计" }
];

const tableStatusOptions = [
  { label: "空闲", value: "IDLE" },
  { label: "使用中", value: "IN_USE" },
  { label: "维修", value: "MAINTENANCE" }
];

const reservationStatusMap = {
  BOOKED: "已预约",
  CANCELLED: "已取消"
};

const paymentStatusMap = {
  UNPAID: "未支付",
  PAID: "已支付"
};

const loginForm = reactive({
  username: "admin",
  password: "123456"
});

const tableForm = reactive(createTableForm());
const reservationForm = reactive(createReservationForm());
const orderForm = reactive(createOrderForm());
const userForm = reactive(createUserForm());
const closeDeskForm = reactive({
  tableId: null,
  tableName: "",
  memberCardLastFour: ""
});

const loginLoading = ref(false);
const submitting = ref(false);
const currentUser = ref(null);
const activeTab = ref("tables");
const errorMessage = ref("");
const successMessage = ref("");

const tables = ref([]);
const reservations = ref([]);
const orders = ref([]);
const users = ref([]);
const reports = ref([]);
const reportType = ref("day");
const tableEditingId = ref(null);
const reservationEditingId = ref(null);
const userEditingId = ref(null);
const closeDeskVisible = ref(false);
const nowTick = ref(Date.now());

let timerId = null;

const overviewCards = computed(() => {
  const paidIncome = orders.value
    .filter((item) => item.paymentStatus === "PAID")
    .reduce((sum, item) => sum + Number(item.amount || 0), 0);

  return [
    {
      label: "台球桌总数",
      value: tables.value.length,
      tip: `${tables.value.filter((item) => item.status === "IDLE").length} 张空闲`
    },
    {
      label: "当前开桌",
      value: tables.value.filter((item) => item.status === "IN_USE").length,
      tip: "再次点击可关桌结算"
    },
    {
      label: "已结算收入",
      value: `￥${paidIncome.toFixed(2)}`,
      tip: `${orders.value.filter((item) => item.paymentStatus === "UNPAID").length} 笔待支付`
    },
    {
      label: "会员人数",
      value: users.value.filter((item) => item.status === 1).length,
      tip: "支持会员卡余额扣费"
    }
  ];
});

const tableCards = computed(() =>
  [...tables.value].sort((a, b) => String(a.tableNo).localeCompare(String(b.tableNo)))
);

const availableUsers = computed(() => users.value.filter((item) => item.status === 1));

const closeDeskPreview = computed(() => {
  const table = tables.value.find((item) => item.id === closeDeskForm.tableId);
  if (!table?.currentUseStartTime) {
    return { durationMinutes: 0, amount: "0.00" };
  }

  const durationMinutes = Math.max(
    Math.floor((nowTick.value - new Date(table.currentUseStartTime).getTime()) / 60000),
    1
  );
  const amount = ((Number(table.hourlyPrice || 0) * durationMinutes) / 60).toFixed(2);
  return { durationMinutes, amount };
});

function createTableForm() {
  return {
    tableNo: "",
    tableName: "",
    areaName: "",
    status: "IDLE",
    hourlyPrice: 48,
    imageUrl:
      "https://images.unsplash.com/photo-1511884642898-4c92249e20b6?auto=format&fit=crop&w=900&q=80",
    remark: ""
  };
}

function createReservationForm() {
  return {
    userId: "",
    tableId: "",
    startTime: "",
    endTime: "",
    contactPhone: "",
    remark: ""
  };
}

function createOrderForm() {
  return {
    userId: "",
    tableId: "",
    reservationId: "",
    startTime: "",
    endTime: "",
    paymentStatus: "UNPAID",
    remark: ""
  };
}

function createUserForm() {
  return {
    nickname: "",
    phone: "",
    email: "",
    memberCardNo: "",
    balance: 0,
    status: 1
  };
}

function resetMessage() {
  errorMessage.value = "";
  successMessage.value = "";
}

function showSuccess(message) {
  errorMessage.value = "";
  successMessage.value = message;
}

function showError(error, fallback = "请求失败，请检查后端服务") {
  successMessage.value = "";
  errorMessage.value = error?.response?.data?.message || fallback;
}

function resetTableForm() {
  Object.assign(tableForm, createTableForm());
  tableEditingId.value = null;
}

function resetReservationForm() {
  Object.assign(reservationForm, createReservationForm());
  reservationEditingId.value = null;
}

function resetOrderForm() {
  Object.assign(orderForm, createOrderForm());
}

function resetUserForm() {
  Object.assign(userForm, createUserForm());
  userEditingId.value = null;
}

function getStatusText(value) {
  return tableStatusOptions.find((item) => item.value === value)?.label || value;
}

function formatMoney(value) {
  return Number(value || 0).toFixed(2);
}

function formatDateTime(value) {
  if (!value) {
    return "-";
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }

  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  const hour = String(date.getHours()).padStart(2, "0");
  const minute = String(date.getMinutes()).padStart(2, "0");
  return `${year}-${month}-${day} ${hour}:${minute}`;
}

function toInputDateTime(value) {
  if (!value) {
    return "";
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }

  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  const hour = String(date.getHours()).padStart(2, "0");
  const minute = String(date.getMinutes()).padStart(2, "0");
  return `${year}-${month}-${day}T${hour}:${minute}`;
}

function formatDuration(startTime) {
  if (!startTime) {
    return "00:00:00";
  }

  const seconds = Math.max(Math.floor((nowTick.value - new Date(startTime).getTime()) / 1000), 0);
  const hours = String(Math.floor(seconds / 3600)).padStart(2, "0");
  const minutes = String(Math.floor((seconds % 3600) / 60)).padStart(2, "0");
  const secs = String(seconds % 60).padStart(2, "0");
  return `${hours}:${minutes}:${secs}`;
}

async function requestWrapper(action, successText) {
  resetMessage();
  submitting.value = true;
  try {
    const { data } = await action();
    if (data.code !== 200) {
      errorMessage.value = data.message || "操作失败";
      return null;
    }
    if (successText) {
      showSuccess(successText);
    }
    return data.data;
  } catch (error) {
    showError(error);
    return null;
  } finally {
    submitting.value = false;
  }
}

async function loadTables() {
  const data = await requestWrapper(() => fetchTables());
  if (data) {
    tables.value = data;
  }
}

async function loadReservations() {
  const data = await requestWrapper(() => fetchReservations());
  if (data) {
    reservations.value = data;
  }
}

async function loadOrders() {
  const data = await requestWrapper(() => fetchOrders());
  if (data) {
    orders.value = data;
  }
}

async function loadUsers() {
  const data = await requestWrapper(() => fetchUsers());
  if (data) {
    users.value = data;
  }
}

async function loadReports() {
  const data = await requestWrapper(() => fetchReportSummary(reportType.value));
  if (data) {
    reports.value = data;
  }
}

async function loadDashboardData() {
  await Promise.all([loadTables(), loadReservations(), loadOrders(), loadUsers(), loadReports()]);
}

async function handleLogin() {
  resetMessage();
  if (!loginForm.username || !loginForm.password) {
    errorMessage.value = "请输入用户名和密码";
    return;
  }

  loginLoading.value = true;
  try {
    const { data } = await login(loginForm);
    if (data.code !== 200) {
      errorMessage.value = data.message || "登录失败";
      return;
    }

    currentUser.value = data.data;
    showSuccess(`欢迎回来，${data.data.nickname}`);
    await loadDashboardData();
  } catch (error) {
    showError(error);
  } finally {
    loginLoading.value = false;
  }
}

function editTable(row) {
  tableEditingId.value = row.id;
  Object.assign(tableForm, {
    tableNo: row.tableNo,
    tableName: row.tableName,
    areaName: row.areaName || "",
    status: row.status,
    hourlyPrice: row.hourlyPrice,
    imageUrl: row.imageUrl || createTableForm().imageUrl,
    remark: row.remark || ""
  });
}

async function submitTable() {
  const payload = {
    ...tableForm,
    hourlyPrice: Number(tableForm.hourlyPrice)
  };
  const data = await requestWrapper(
    () => (tableEditingId.value ? updateTable(tableEditingId.value, payload) : createTable(payload)),
    tableEditingId.value ? "球桌信息更新成功" : "球桌新增成功"
  );
  if (data) {
    resetTableForm();
    await loadTables();
  }
}

async function removeTable(id) {
  const data = await requestWrapper(() => deleteTable(id), "球桌删除成功");
  if (data !== null) {
    await loadTables();
  }
}

async function handleToggleDesk(table) {
  if (table.status === "IDLE") {
    const data = await requestWrapper(() => openTable(table.id), `${table.tableName} 开桌成功`);
    if (data) {
      await loadTables();
    }
    return;
  }

  if (table.status === "IN_USE") {
    closeDeskForm.tableId = table.id;
    closeDeskForm.tableName = table.tableName;
    closeDeskForm.memberCardLastFour = "";
    closeDeskVisible.value = true;
  }
}

async function submitCloseDesk() {
  const data = await requestWrapper(
    () =>
      closeTable(closeDeskForm.tableId, {
        memberCardLastFour: closeDeskForm.memberCardLastFour
      }),
    `${closeDeskForm.tableName} 关桌结算成功`
  );
  if (data) {
    closeDeskVisible.value = false;
    await Promise.all([loadTables(), loadOrders(), loadUsers(), loadReports()]);
  }
}

function editReservation(row) {
  reservationEditingId.value = row.id;
  Object.assign(reservationForm, {
    userId: row.userId,
    tableId: row.tableId,
    startTime: toInputDateTime(row.startTime),
    endTime: toInputDateTime(row.endTime),
    contactPhone: row.contactPhone || "",
    remark: row.remark || ""
  });
}

async function submitReservation() {
  const payload = {
    ...reservationForm,
    userId: Number(reservationForm.userId),
    tableId: Number(reservationForm.tableId)
  };
  const data = await requestWrapper(
    () =>
      reservationEditingId.value
        ? updateReservation(reservationEditingId.value, payload)
        : createReservation(payload),
    reservationEditingId.value ? "预约修改成功" : "预约创建成功"
  );
  if (data) {
    resetReservationForm();
    await Promise.all([loadReservations(), loadTables()]);
  }
}

async function handleCancelReservation(id) {
  const data = await requestWrapper(() => cancelReservation(id), "预约已取消");
  if (data) {
    await loadReservations();
  }
}

async function submitOrder() {
  const payload = {
    ...orderForm,
    userId: Number(orderForm.userId),
    tableId: Number(orderForm.tableId),
    reservationId: orderForm.reservationId ? Number(orderForm.reservationId) : null
  };
  const data = await requestWrapper(() => createOrder(payload), "订单创建成功");
  if (data) {
    resetOrderForm();
    await Promise.all([loadOrders(), loadTables(), loadReports()]);
  }
}

async function handlePayOrder(id) {
  const data = await requestWrapper(() => payOrder(id), "订单已标记为已支付");
  if (data) {
    await Promise.all([loadOrders(), loadReports()]);
  }
}

function editUser(row) {
  userEditingId.value = row.id;
  Object.assign(userForm, {
    nickname: row.nickname || "",
    phone: row.phone || "",
    email: row.email || "",
    memberCardNo: row.memberCardNo || "",
    balance: Number(row.balance || 0),
    status: row.status
  });
}

async function submitUser() {
  if (!userEditingId.value) {
    errorMessage.value = "请先从用户列表选择要编辑的会员";
    return;
  }

  const data = await requestWrapper(
    () =>
      updateUser(userEditingId.value, {
        ...userForm,
        status: Number(userForm.status),
        balance: Number(userForm.balance)
      }),
    "会员信息更新成功"
  );
  if (data) {
    resetUserForm();
    await loadUsers();
  }
}

async function handleDisableUser(id) {
  const data = await requestWrapper(() => disableUser(id), "用户已禁用");
  if (data) {
    await loadUsers();
  }
}

onMounted(() => {
  timerId = window.setInterval(() => {
    nowTick.value = Date.now();
  }, 1000);
});

onBeforeUnmount(() => {
  if (timerId) {
    window.clearInterval(timerId);
  }
});
</script>

<template>
  <main class="page-shell">
    <section class="hero-panel">
      <p class="eyebrow">Billiards Hall Admin</p>
      <h1>台球厅管理系统</h1>
      <p class="hero-copy">
        在原有登录示例的风格上，新增开桌、关桌、图片墙展示、实时计时和会员余额扣费能力，方便前台快速操作。
      </p>
      <div class="hero-tags">
        <span>开桌 / 关桌</span>
        <span>会员扣费</span>
        <span>图片卡片墙</span>
        <span>实时计时</span>
      </div>

      <div v-if="currentUser" class="overview-grid">
        <article v-for="item in overviewCards" :key="item.label" class="overview-card">
          <p>{{ item.label }}</p>
          <strong>{{ item.value }}</strong>
          <span>{{ item.tip }}</span>
        </article>
      </div>
    </section>

    <section class="login-card admin-card">
      <div class="card-header">
        <template v-if="!currentUser">
          <h2>欢迎登录</h2>
          <p>使用默认账号进入台球厅前台管理界面</p>
        </template>
        <template v-else>
          <div class="header-row">
            <div>
              <h2>营业控制台</h2>
              <p>当前登录：{{ currentUser.nickname }}（{{ currentUser.username }}）</p>
            </div>
            <button class="ghost-btn" type="button" @click="currentUser = null">退出展示</button>
          </div>
        </template>
      </div>

      <form v-if="!currentUser" class="login-form" @submit.prevent="handleLogin">
        <label>
          <span>用户名</span>
          <input v-model.trim="loginForm.username" type="text" placeholder="请输入用户名" />
        </label>
        <label>
          <span>密码</span>
          <input v-model="loginForm.password" type="password" placeholder="请输入密码" />
        </label>
        <button class="submit-btn" type="submit" :disabled="loginLoading">
          {{ loginLoading ? "登录中..." : "登录系统" }}
        </button>
      </form>

      <template v-else>
        <div class="toolbar tabs">
          <button
            v-for="tab in tabs"
            :key="tab.key"
            type="button"
            :class="['tab-btn', { active: activeTab === tab.key }]"
            @click="activeTab = tab.key"
          >
            {{ tab.label }}
          </button>
        </div>

        <section v-if="activeTab === 'tables'" class="panel-block">
          <div class="section-title">
            <h3>台球桌图片墙</h3>
            <p>点击空闲球桌可开桌，使用中的球桌再次点击即可关桌并输入会员卡后四位完成扣费。</p>
          </div>

          <div class="desk-grid">
            <article
              v-for="table in tableCards"
              :key="table.id"
              :class="['desk-card', table.status.toLowerCase()]"
            >
              <div class="desk-image-wrap">
                <img :src="table.imageUrl || createTableForm().imageUrl" :alt="table.tableName" class="desk-image" />
                <div class="desk-overlay">
                  <span class="status-pill" :class="table.status.toLowerCase()">
                    {{ getStatusText(table.status) }}
                  </span>
                </div>
              </div>

              <div class="desk-content">
                <div class="desk-heading">
                  <div>
                    <h4>{{ table.tableName }}</h4>
                    <p>{{ table.tableNo }} · {{ table.areaName || "未分区" }}</p>
                  </div>
                  <strong>￥{{ formatMoney(table.hourlyPrice) }}/小时</strong>
                </div>

                <div class="desk-meta">
                  <span>开始时间：{{ formatDateTime(table.currentUseStartTime) }}</span>
                  <span v-if="table.status === 'IN_USE'">已使用：{{ formatDuration(table.currentUseStartTime) }}</span>
                  <span v-else>备注：{{ table.remark || "暂无" }}</span>
                </div>

                <div class="desk-actions">
                  <button
                    class="submit-btn"
                    type="button"
                    :disabled="submitting || table.status === 'MAINTENANCE'"
                    @click="handleToggleDesk(table)"
                  >
                    {{ table.status === "IN_USE" ? "关桌结算" : "开桌" }}
                  </button>
                  <button class="ghost-btn" type="button" @click="editTable(table)">编辑球桌</button>
                </div>
              </div>
            </article>
          </div>

          <div class="section-title compact">
            <h3>球桌信息维护</h3>
            <p>可维护单价、图片、区域和状态。</p>
          </div>
          <div class="form-grid">
            <label>
              <span>台球桌编号</span>
              <input v-model.trim="tableForm.tableNo" type="text" placeholder="如 A01" />
            </label>
            <label>
              <span>台球桌名称</span>
              <input v-model.trim="tableForm.tableName" type="text" placeholder="如 1号台" />
            </label>
            <label>
              <span>区域</span>
              <input v-model.trim="tableForm.areaName" type="text" placeholder="如 大厅A区" />
            </label>
            <label>
              <span>状态</span>
              <select v-model="tableForm.status">
                <option v-for="item in tableStatusOptions" :key="item.value" :value="item.value">
                  {{ item.label }}
                </option>
              </select>
            </label>
            <label>
              <span>每小时单价</span>
              <input v-model="tableForm.hourlyPrice" type="number" min="1" step="0.01" />
            </label>
            <label>
              <span>图片地址</span>
              <input v-model.trim="tableForm.imageUrl" type="text" placeholder="请输入球桌图片 URL" />
            </label>
            <label class="full-span">
              <span>备注</span>
              <input v-model.trim="tableForm.remark" type="text" placeholder="如 靠窗、包间、灯光说明" />
            </label>
          </div>
          <div class="toolbar">
            <button class="submit-btn" type="button" :disabled="submitting" @click="submitTable">
              {{ tableEditingId ? "保存球桌" : "新增球桌" }}
            </button>
            <button class="ghost-btn" type="button" @click="resetTableForm">重置</button>
          </div>

          <div class="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>编号</th>
                  <th>名称</th>
                  <th>状态</th>
                  <th>单价</th>
                  <th>图片</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in tableCards" :key="row.id">
                  <td>{{ row.tableNo }}</td>
                  <td>{{ row.tableName }}</td>
                  <td>{{ getStatusText(row.status) }}</td>
                  <td>￥{{ formatMoney(row.hourlyPrice) }}</td>
                  <td>{{ row.imageUrl ? "已配置" : "未配置" }}</td>
                  <td class="actions">
                    <button type="button" class="link-btn" @click="editTable(row)">编辑</button>
                    <button type="button" class="link-btn danger" @click="removeTable(row.id)">删除</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>

        <section v-if="activeTab === 'reservations'" class="panel-block">
          <div class="section-title">
            <h3>预约管理</h3>
            <p>支持新增预约、修改预约和取消预约。</p>
          </div>
          <div class="form-grid">
            <label>
              <span>预约用户</span>
              <select v-model="reservationForm.userId">
                <option value="">请选择用户</option>
                <option v-for="item in availableUsers" :key="item.id" :value="item.id">
                  {{ item.nickname || item.username }}
                </option>
              </select>
            </label>
            <label>
              <span>台球桌</span>
              <select v-model="reservationForm.tableId">
                <option value="">请选择台球桌</option>
                <option v-for="item in tableCards.filter((table) => table.status === 'IDLE')" :key="item.id" :value="item.id">
                  {{ item.tableName }}（{{ item.tableNo }}）
                </option>
              </select>
            </label>
            <label>
              <span>开始时间</span>
              <input v-model="reservationForm.startTime" type="datetime-local" />
            </label>
            <label>
              <span>结束时间</span>
              <input v-model="reservationForm.endTime" type="datetime-local" />
            </label>
            <label>
              <span>联系电话</span>
              <input v-model.trim="reservationForm.contactPhone" type="text" placeholder="请输入联系电话" />
            </label>
            <label class="full-span">
              <span>备注</span>
              <input v-model.trim="reservationForm.remark" type="text" placeholder="如 到店时间说明" />
            </label>
          </div>
          <div class="toolbar">
            <button class="submit-btn" type="button" :disabled="submitting" @click="submitReservation">
              {{ reservationEditingId ? "保存预约" : "新增预约" }}
            </button>
            <button class="ghost-btn" type="button" @click="resetReservationForm">重置</button>
          </div>

          <div class="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>用户</th>
                  <th>台球桌</th>
                  <th>开始时间</th>
                  <th>结束时间</th>
                  <th>电话</th>
                  <th>状态</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in reservations" :key="row.id">
                  <td>{{ row.nickname || row.username }}</td>
                  <td>{{ row.tableName }}（{{ row.tableNo }}）</td>
                  <td>{{ formatDateTime(row.startTime) }}</td>
                  <td>{{ formatDateTime(row.endTime) }}</td>
                  <td>{{ row.contactPhone || "-" }}</td>
                  <td>{{ reservationStatusMap[row.status] || row.status }}</td>
                  <td class="actions">
                    <button type="button" class="link-btn" :disabled="row.status !== 'BOOKED'" @click="editReservation(row)">修改</button>
                    <button type="button" class="link-btn danger" :disabled="row.status !== 'BOOKED'" @click="handleCancelReservation(row.id)">取消</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>

        <section v-if="activeTab === 'orders'" class="panel-block">
          <div class="section-title">
            <h3>订单管理</h3>
            <p>支持人工订单和开桌关桌自动结算订单。</p>
          </div>
          <div class="form-grid">
            <label>
              <span>用户</span>
              <select v-model="orderForm.userId">
                <option value="">请选择用户</option>
                <option v-for="item in availableUsers" :key="item.id" :value="item.id">
                  {{ item.nickname || item.username }}
                </option>
              </select>
            </label>
            <label>
              <span>台球桌</span>
              <select v-model="orderForm.tableId">
                <option value="">请选择台球桌</option>
                <option v-for="item in tableCards" :key="item.id" :value="item.id">
                  {{ item.tableName }}（{{ item.tableNo }}）
                </option>
              </select>
            </label>
            <label>
              <span>关联预约</span>
              <select v-model="orderForm.reservationId">
                <option value="">无</option>
                <option v-for="item in reservations.filter((reservation) => reservation.status === 'BOOKED')" :key="item.id" :value="item.id">
                  {{ item.nickname || item.username }} - {{ item.tableName }}
                </option>
              </select>
            </label>
            <label>
              <span>支付状态</span>
              <select v-model="orderForm.paymentStatus">
                <option value="UNPAID">未支付</option>
                <option value="PAID">已支付</option>
              </select>
            </label>
            <label>
              <span>开始时间</span>
              <input v-model="orderForm.startTime" type="datetime-local" />
            </label>
            <label>
              <span>结束时间</span>
              <input v-model="orderForm.endTime" type="datetime-local" />
            </label>
            <label class="full-span">
              <span>备注</span>
              <input v-model.trim="orderForm.remark" type="text" placeholder="可填写套餐或说明" />
            </label>
          </div>
          <div class="toolbar">
            <button class="submit-btn" type="button" :disabled="submitting" @click="submitOrder">生成订单</button>
            <button class="ghost-btn" type="button" @click="resetOrderForm">重置</button>
          </div>

          <div class="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>订单号</th>
                  <th>用户</th>
                  <th>台球桌</th>
                  <th>时长</th>
                  <th>金额</th>
                  <th>卡号后四位</th>
                  <th>支付状态</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in orders" :key="row.id">
                  <td>{{ row.orderNo }}</td>
                  <td>{{ row.nickname || row.username || "-" }}</td>
                  <td>{{ row.tableName }}（{{ row.tableNo }}）</td>
                  <td>{{ row.durationMinutes }} 分钟</td>
                  <td>￥{{ formatMoney(row.amount) }}</td>
                  <td>{{ row.memberCardLastFour || "-" }}</td>
                  <td>{{ paymentStatusMap[row.paymentStatus] || row.paymentStatus }}</td>
                  <td class="actions">
                    <button type="button" class="link-btn" :disabled="row.paymentStatus === 'PAID'" @click="handlePayOrder(row.id)">标记已支付</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>

        <section v-if="activeTab === 'users'" class="panel-block">
          <div class="section-title">
            <h3>会员管理</h3>
            <p>维护会员卡号和余额，关桌时按会员卡后四位自动匹配并扣费。</p>
          </div>
          <div class="form-grid">
            <label>
              <span>昵称</span>
              <input v-model.trim="userForm.nickname" type="text" placeholder="请输入昵称" />
            </label>
            <label>
              <span>手机号</span>
              <input v-model.trim="userForm.phone" type="text" placeholder="请输入手机号" />
            </label>
            <label>
              <span>邮箱</span>
              <input v-model.trim="userForm.email" type="text" placeholder="请输入邮箱" />
            </label>
            <label>
              <span>会员卡号</span>
              <input v-model.trim="userForm.memberCardNo" type="text" placeholder="如 888800001234" />
            </label>
            <label>
              <span>余额</span>
              <input v-model="userForm.balance" type="number" min="0" step="0.01" />
            </label>
            <label>
              <span>状态</span>
              <select v-model="userForm.status">
                <option :value="1">正常</option>
                <option :value="0">禁用</option>
              </select>
            </label>
          </div>
          <div class="toolbar">
            <button class="submit-btn" type="button" :disabled="submitting" @click="submitUser">保存会员信息</button>
            <button class="ghost-btn" type="button" @click="resetUserForm">重置</button>
          </div>

          <div class="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>用户名</th>
                  <th>昵称</th>
                  <th>会员卡号</th>
                  <th>余额</th>
                  <th>状态</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in users" :key="row.id">
                  <td>{{ row.id }}</td>
                  <td>{{ row.username }}</td>
                  <td>{{ row.nickname || "-" }}</td>
                  <td>{{ row.memberCardNo || "-" }}</td>
                  <td>￥{{ formatMoney(row.balance) }}</td>
                  <td>{{ row.status === 1 ? "正常" : "禁用" }}</td>
                  <td class="actions">
                    <button type="button" class="link-btn" @click="editUser(row)">编辑</button>
                    <button type="button" class="link-btn danger" :disabled="row.status !== 1" @click="handleDisableUser(row.id)">禁用</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>

        <section v-if="activeTab === 'reports'" class="panel-block">
          <div class="section-title">
            <h3>报表统计</h3>
            <p>支持按天、按周、按月统计台球桌使用率和收入。</p>
          </div>
          <div class="toolbar">
            <div class="segmented-control">
              <button type="button" :class="{ active: reportType === 'day' }" @click="reportType = 'day'">按天</button>
              <button type="button" :class="{ active: reportType === 'week' }" @click="reportType = 'week'">按周</button>
              <button type="button" :class="{ active: reportType === 'month' }" @click="reportType = 'month'">按月</button>
            </div>
            <button class="submit-btn" type="button" :disabled="submitting" @click="loadReports">刷新报表</button>
          </div>

          <div class="report-grid">
            <article v-for="row in reports" :key="row.label" class="report-card">
              <p class="report-label">{{ row.label }}</p>
              <strong>{{ row.usageRate }}%</strong>
              <span>使用时长：{{ row.totalMinutes }} 分钟</span>
              <span>订单数：{{ row.orderCount }}</span>
              <span>收入：￥{{ formatMoney(row.income) }}</span>
            </article>
          </div>
        </section>
      </template>

      <p v-if="errorMessage" class="message error">{{ errorMessage }}</p>
      <p v-if="successMessage" class="message success">{{ successMessage }}</p>
    </section>

    <div v-if="closeDeskVisible" class="modal-mask" @click.self="closeDeskVisible = false">
      <section class="modal-card">
        <h3>关桌结算</h3>
        <p>{{ closeDeskForm.tableName }} 正在使用中，请输入会员卡后四位完成扣费。</p>
        <div class="modal-stats">
          <span>预计时长：{{ closeDeskPreview.durationMinutes }} 分钟</span>
          <span>预计金额：￥{{ closeDeskPreview.amount }}</span>
        </div>
        <label>
          <span>会员卡后四位</span>
          <input v-model.trim="closeDeskForm.memberCardLastFour" type="text" maxlength="4" placeholder="如 1234" />
        </label>
        <div class="toolbar">
          <button class="submit-btn" type="button" :disabled="submitting" @click="submitCloseDesk">确认关桌</button>
          <button class="ghost-btn" type="button" @click="closeDeskVisible = false">取消</button>
        </div>
      </section>
    </div>
  </main>
</template>
