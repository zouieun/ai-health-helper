<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-logo">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
          <path d="M11 20A7 7 0 0 1 4 13c0-4 3-8 8-9 3.5-.7 7-.5 7-.5s.5 3.5-.5 7c-1 5-5 8-7.5 8.5z" />
          <path d="M4 21c4-4 6-6 10-10" />
        </svg>
      </div>
      <h1 class="login-title">健康饮食助手</h1>
      <p class="login-subtitle">{{ isRegister ? '创建账号，开启健康饮食之旅' : '登录后开始您的健康饮食咨询' }}</p>

      <p v-if="notice" class="login-notice" role="alert">{{ notice }}</p>
      <p v-if="localNotice" class="login-notice" role="alert">{{ localNotice }}</p>

      <form class="login-form" @submit.prevent="handleSubmit">
        <div class="field">
          <label for="username">用户名</label>
          <div class="input-wrap">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
              <circle cx="12" cy="7" r="4" />
            </svg>
            <input
              id="username"
              v-model.trim="username"
              type="text"
              placeholder="请输入用户名"
              autocomplete="username"
              :disabled="loading"
            />
          </div>
        </div>

        <div class="field">
          <label for="password">密码</label>
          <div class="input-wrap">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="11" width="18" height="11" rx="2" />
              <path d="M7 11V7a5 5 0 0 1 10 0v4" />
            </svg>
            <input
              id="password"
              v-model="password"
              :type="showPassword ? 'text' : 'password'"
              placeholder="请输入密码"
              :autocomplete="isRegister ? 'new-password' : 'current-password'"
              :disabled="loading"
            />
            <button
              type="button"
              class="toggle-password"
              :aria-label="showPassword ? '隐藏密码' : '显示密码'"
              @click="showPassword = !showPassword"
            >
              <svg v-if="showPassword" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94" />
                <path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19" />
                <path d="M14.12 14.12a3 3 0 1 1-4.24-4.24" />
                <path d="M1 1l22 22" />
              </svg>
              <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                <circle cx="12" cy="12" r="3" />
              </svg>
            </button>
          </div>
        </div>

        <div v-if="isRegister" class="field">
          <label for="confirmPassword">确认密码</label>
          <div class="input-wrap">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="11" width="18" height="11" rx="2" />
              <path d="M7 11V7a5 5 0 0 1 10 0v4" />
            </svg>
            <input
              id="confirmPassword"
              v-model="confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              autocomplete="new-password"
              :disabled="loading"
            />
          </div>
        </div>

        <p v-if="errorText" class="login-error" role="alert">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="10" />
            <path d="M12 8v4M12 16h.01" />
          </svg>
          {{ errorText }}
        </p>

        <button type="submit" class="login-button" :disabled="loading || !canSubmit">
          <span v-if="loading" class="spinner"></span>
          {{ loading ? (isRegister ? '注册中…' : '登录中…') : (isRegister ? '注 册' : '登 录') }}
        </button>
      </form>

      <button type="button" class="switch-mode" @click="switchMode">
        {{ isRegister ? '已有账号？直接登录' : '没有账号？立即注册' }}
      </button>

      <p class="login-tip">均衡饮食 · 健康生活</p>
    </div>
  </div>
</template>

<script>
import { login, register } from '../api/chatApi.js'
import { setAuth } from '../utils/auth.js'

export default {
  name: 'LoginView',
  emits: ['login-success'],
  props: {
    notice: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      mode: 'login',
      username: '',
      password: '',
      confirmPassword: '',
      showPassword: false,
      loading: false,
      errorText: '',
      localNotice: ''
    }
  },
  computed: {
    isRegister() {
      return this.mode === 'register'
    },
    canSubmit() {
      if (!this.username.length || !this.password.length) return false
      return this.isRegister ? this.confirmPassword.length > 0 : true
    }
  },
  methods: {
    switchMode() {
      this.mode = this.isRegister ? 'login' : 'register'
      this.errorText = ''
      this.localNotice = ''
      this.confirmPassword = ''
    },

    authErrorText(error, fallback) {
      // 业务失败（HTTP 200 + code=0）已由chatApi标记isBizError并携带msg
      if (error.isBizError) {
        return error.message
      }
      if (error.code === 'ECONNABORTED') {
        return '请求超时，请稍后重试'
      }
      return error.response?.data?.msg || fallback
    },

    async handleSubmit() {
      if (!this.canSubmit || this.loading) return
      if (this.isRegister && this.password !== this.confirmPassword) {
        this.errorText = '两次输入的密码不一致'
        return
      }
      this.loading = true
      this.errorText = ''
      this.localNotice = ''
      let registered = false
      try {
        if (this.isRegister) {
          try {
            await register(this.username, this.password)
            registered = true
          } catch (error) {
            this.errorText = this.authErrorText(error, '注册失败，请稍后重试')
            return
          }
        }
        // 登录（注册成功后自动登录）
        const loginVO = await login(this.username, this.password)
        setAuth(loginVO)
        this.$emit('login-success', loginVO)
      } catch (error) {
        if (registered) {
          // 注册已成功但自动登录失败：回登录模式手动登录
          this.mode = 'login'
          this.confirmPassword = ''
          this.localNotice = '注册成功，请登录'
        } else {
          this.errorText = this.authErrorText(error, '用户名或密码错误')
        }
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background:
    radial-gradient(600px 320px at 12% -4%, rgba(52, 211, 153, 0.16), transparent 70%),
    radial-gradient(700px 380px at 95% 8%, rgba(163, 230, 53, 0.14), transparent 70%),
    linear-gradient(180deg, #f2faf5 0%, #f6fbf3 100%);
}

.login-card {
  width: 100%;
  max-width: 400px;
  background: var(--card);
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  padding: 36px 32px 28px;
  text-align: center;
  animation: login-in 0.4s ease-out;
}

.login-logo {
  width: 64px;
  height: 64px;
  margin: 0 auto 16px;
  border-radius: 20px;
  background: var(--gradient-primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 24px rgba(22, 163, 74, 0.3);
}

.login-logo svg {
  width: 34px;
  height: 34px;
}

.login-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--ink-900);
  letter-spacing: 0.5px;
}

.login-subtitle {
  font-size: 13px;
  color: var(--ink-400);
  margin: 6px 0 26px;
}

.login-form {
  text-align: left;
}

.field {
  margin-bottom: 16px;
}

.field label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: var(--ink-700);
  margin-bottom: 6px;
}

.input-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
  border: 1px solid var(--line);
  border-radius: var(--radius-md);
  background: var(--mint-50);
  padding: 0 12px;
  transition: border-color 0.2s, box-shadow 0.2s, background 0.2s;
}

.input-wrap:focus-within {
  border-color: var(--green-500);
  background: #fff;
  box-shadow: 0 0 0 3px rgba(34, 197, 94, 0.15);
}

.input-wrap > svg {
  width: 17px;
  height: 17px;
  color: var(--ink-400);
  flex-shrink: 0;
}

.input-wrap input {
  flex: 1;
  min-width: 0;
  border: none;
  outline: none;
  background: transparent;
  padding: 12px 0;
  font-size: 14px;
  color: var(--ink-900);
}

.input-wrap input::placeholder {
  color: var(--ink-400);
}

.toggle-password {
  display: flex;
  align-items: center;
  padding: 4px;
  color: var(--ink-400);
  border-radius: 6px;
}

.toggle-password:hover {
  color: var(--green-600);
}

.toggle-password svg {
  width: 17px;
  height: 17px;
}

.login-notice {
  font-size: 13px;
  color: var(--amber-600);
  background: var(--amber-100);
  border: 1px solid #fde68a;
  border-radius: 10px;
  padding: 8px 12px;
  margin-bottom: 16px;
}

.login-error {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--danger-500);
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 10px;
  padding: 8px 12px;
  margin-bottom: 16px;
}

.login-error svg {
  width: 15px;
  height: 15px;
  flex-shrink: 0;
}

.login-button {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 13px;
  border-radius: var(--radius-md);
  background: var(--gradient-primary);
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 2px;
  box-shadow: 0 4px 14px rgba(22, 163, 74, 0.3);
  transition: transform 0.15s, box-shadow 0.15s, opacity 0.15s;
}

.login-button:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 6px 18px rgba(22, 163, 74, 0.36);
}

.login-button:disabled {
  opacity: 0.55;
  cursor: not-allowed;
  box-shadow: none;
}

.spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.4);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

.switch-mode {
  margin-top: 16px;
  font-size: 13px;
  color: var(--green-600);
  background: none;
  border: none;
  cursor: pointer;
}

.switch-mode:hover {
  color: var(--green-700);
  text-decoration: underline;
}

.login-tip {
  margin-top: 22px;
  font-size: 12px;
  color: var(--ink-400);
  letter-spacing: 1px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@keyframes login-in {
  from {
    opacity: 0;
    transform: translateY(14px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 480px) {
  .login-card {
    padding: 28px 22px 22px;
  }

  .input-wrap input {
    font-size: 16px;
  }
}
</style>