<script setup>
import { reactive, ref } from "vue";
import { login } from "./api/auth";

const form = reactive({
  username: "admin",
  password: "123456"
});

const loading = ref(false);
const errorMessage = ref("");
const loginResult = ref(null);

async function handleLogin() {
  errorMessage.value = "";
  loginResult.value = null;

  if (!form.username || !form.password) {
    errorMessage.value = "请输入用户名和密码";
    return;
  }

  loading.value = true;
  try {
    const { data } = await login({
      username: form.username,
      password: form.password
    });

    if (data.code !== 200) {
      errorMessage.value = data.message || "登录失败";
      return;
    }

    loginResult.value = data.data;
  } catch (error) {
    errorMessage.value =
      error.response?.data?.message || "请求失败，请检查后端服务和数据库配置";
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <main class="page-shell">
    <section class="hero-panel">
      <p class="eyebrow">Spring Boot + Vue</p>
      <h1>后台管理登录演示</h1>
      <p class="hero-copy">
        这是一个最小可扩展的前后端分离登录页示例，后端走数据库查询，前端通过接口完成登录。
      </p>
      <div class="hero-tags">
        <span>Vue 3</span>
        <span>Vite</span>
        <span>Spring Boot</span>
        <span>MySQL</span>
      </div>
    </section>

    <section class="login-card">
      <div class="card-header">
        <h2>欢迎登录</h2>
        <p>默认测试账号已帮你填好，你也可以自行修改</p>
      </div>

      <form class="login-form" @submit.prevent="handleLogin">
        <label>
          <span>用户名</span>
          <input v-model.trim="form.username" type="text" placeholder="请输入用户名" />
        </label>

        <label>
          <span>密码</span>
          <input v-model="form.password" type="password" placeholder="请输入密码" />
        </label>

        <button class="submit-btn" type="submit" :disabled="loading">
          {{ loading ? "登录中..." : "登 录" }}
        </button>
      </form>

      <p v-if="errorMessage" class="message error">{{ errorMessage }}</p>

      <div v-if="loginResult" class="result-box">
        <h3>登录成功</h3>
        <p>用户ID：{{ loginResult.userId }}</p>
        <p>用户名：{{ loginResult.username }}</p>
        <p>昵称：{{ loginResult.nickname }}</p>
        <p class="token-text">Token：{{ loginResult.token }}</p>
      </div>
    </section>
  </main>
</template>

