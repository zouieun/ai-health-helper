<template>
  <div class="app">
    <LoginView v-if="!loggedIn" :notice="authNotice" @login-success="handleLoginSuccess" />
    <template v-else>
    <!-- 头部标题 -->
    <header class="app-header">
      <div class="header-inner">
        <div class="brand">
          <div class="brand-logo">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <path d="M11 20A7 7 0 0 1 4 13c0-4 3-8 8-9 3.5-.7 7-.5 7-.5s.5 3.5-.5 7c-1 5-5 8-7.5 8.5z" />
              <path d="M4 21c4-4 6-6 10-10" />
            </svg>
          </div>
          <div class="brand-text">
            <h1 class="app-title">健康饮食助手</h1>
            <div class="app-subtitle">咨询健康饮食相关知识与问题</div>
          </div>
        </div>
        <div class="mode-switch" role="tablist" aria-label="模式切换">
          <button
            class="mode-option"
            :class="{ active: !reportMode }"
            role="tab"
            :aria-selected="!reportMode"
            @click="setMode(false)"
          >
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <path d="M21 11.5a8.5 8.5 0 0 1-8.5 8.5c-1.5 0-3-.4-4.2-1.1L3 20l1.1-5.3A8.5 8.5 0 1 1 21 11.5z" />
            </svg>
            聊天咨询
          </button>
          <button
            class="mode-option"
            :class="{ active: reportMode }"
            role="tab"
            :aria-selected="reportMode"
            @click="setMode(true)"
          >
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <path d="M9 3h6" />
              <path d="M10 3v6L4.5 18a2 2 0 0 0 1.8 3h11.4a2 2 0 0 0 1.8-3L14 9V3" />
              <path d="M7.5 14h9" />
            </svg>
            研究报告
          </button>
        </div>
        <div class="user-area">
          <span class="user-chip">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
              <circle cx="12" cy="7" r="4" />
            </svg>
            {{ userName }}
          </span>
          <button class="logout-button" title="退出登录" aria-label="退出登录" @click="handleLogout">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
              <path d="M16 17l5-5-5-5" />
              <path d="M21 12H9" />
            </svg>
          </button>
        </div>
      </div>
    </header>

    <!-- 聊天区域 -->
    <main class="chat-container">
      <!-- 消息列表 -->
      <div class="messages-container" ref="messagesContainer">
        <div class="messages-inner">
          <div v-if="messages.length === 0" class="welcome-message">
            <div class="welcome-content">
              <div class="welcome-badge">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M11 20A7 7 0 0 1 4 13c0-4 3-8 8-9 3.5-.7 7-.5 7-.5s.5 3.5-.5 7c-1 5-5 8-7.5 8.5z" />
                  <path d="M4 21c4-4 6-6 10-10" />
                </svg>
              </div>
              <h2>欢迎使用健康饮食助手</h2>
              <p class="welcome-desc">
                我是您的专属健康饮食顾问，可以帮你了解饮食原则、解答营养问题、
                提供实用建议。点击下方话题快速开始，或直接输入您的问题。
              </p>
              <div class="suggestion-grid">
                <button
                  v-for="item in suggestions"
                  :key="item.question"
                  class="suggestion-card"
                  @click="handleSend(item.question)"
                >
                  <span class="suggestion-icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                      <path v-for="(path, i) in item.icon" :key="i" :d="path" />
                    </svg>
                  </span>
                  <span class="suggestion-text">
                    <span class="suggestion-label">{{ item.label }}</span>
                    <span class="suggestion-question">{{ item.question }}</span>
                  </span>
                </button>
              </div>
            </div>
          </div>

          <!-- 历史消息 -->
          <ChatMessage
            v-for="message in messages"
            :key="message.id"
            :message="message.content"
            :is-user="message.isUser"
            :timestamp="message.timestamp"
          />

          <!-- 研究闭环报告 -->
          <ResearchReportPanel v-if="currentReport" :report="currentReport" />

          <!-- 研究闭环加载中 -->
          <div v-if="isReportLoading" class="chat-message ai-message">
            <div class="message-avatar">
              <div class="avatar ai-avatar">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M12 3l1.9 5.1L19 10l-5.1 1.9L12 17l-1.9-5.1L5 10l5.1-1.9L12 3z" />
                  <path d="M19 15l.9 2.1L22 18l-2.1.9L19 21l-.9-2.1L16 18l2.1-.9L19 15z" />
                </svg>
              </div>
            </div>
            <div class="message-content">
              <div class="message-bubble loading-bubble">
                <LoadingDots />
                <span class="loading-text">正在生成研究报告，请稍候…</span>
              </div>
            </div>
          </div>

          <!-- AI 正在回复的消息 -->
          <div v-if="isAiTyping" class="chat-message ai-message">
            <div class="message-avatar">
              <div class="avatar ai-avatar">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M12 3l1.9 5.1L19 10l-5.1 1.9L12 17l-1.9-5.1L5 10l5.1-1.9L12 3z" />
                  <path d="M19 15l.9 2.1L22 18l-2.1.9L19 21l-.9-2.1L16 18l2.1-.9L19 15z" />
                </svg>
              </div>
            </div>
            <div class="message-content">
              <div class="message-bubble">
                <div class="ai-typing-content">
                  <div class="ai-response-text message-markdown" v-html="currentAiResponseRendered"></div>
                  <LoadingDots v-if="isStreaming" />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入框 -->
      <ChatInput
        :disabled="isAiTyping || isReportLoading"
        @send-message="handleSend"
        :placeholder="inputPlaceholder"
      />
    </main>

    <!-- 连接状态提示 -->
    <div v-if="connectionError" class="connection-error" role="alert">
      <div class="error-content">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
          <path d="M10.3 3.9 1.8 18a2 2 0 0 0 1.7 3h17a2 2 0 0 0 1.7-3L13.7 3.9a2 2 0 0 0-3.4 0z" />
          <path d="M12 9v4M12 17h.01" />
        </svg>
        <span>连接服务器失败，请检查后端服务是否启动</span>
      </div>
    </div>
    </template>
  </div>
</template>

<script>
import ChatMessage from './components/ChatMessage.vue'
import ChatInput from './components/ChatInput.vue'
import LoadingDots from './components/LoadingDots.vue'
import ResearchReportPanel from './components/ResearchReportPanel.vue'
import LoginView from './components/LoginView.vue'
import { chatWithSSE, createResearchReport, getHistory, saveChatLog } from './api/chatApi.js'
import { renderMarkdown } from './utils/index.js'
import { isLoggedIn, getUser, clearAuth } from './utils/auth.js'

export default {
  name: 'App',
  components: {
    ChatMessage,
    ChatInput,
    LoadingDots,
    ResearchReportPanel,
    LoginView
  },
  data() {
    return {
      messages: [],
      loggedIn: isLoggedIn(),
      userName: getUser()?.userName || '',
      authNotice: '',
      isAiTyping: false,
      isStreaming: false,
      currentAiResponse: '',
      lastUserMessage: '',
      currentEventSource: null,
      connectionError: false,
      reportMode: false,
      isReportLoading: false,
      currentReport: null,
      suggestions: [
        {
          label: '饮食原则',
          question: '健康饮食的基本原则是什么？',
          icon: [
            'M11 20A7 7 0 0 1 4 13c0-4 3-8 8-9 3.5-.7 7-.5 7-.5s.5 3.5-.5 7c-1 5-5 8-7.5 8.5z',
            'M4 21c4-4 6-6 10-10'
          ]
        },
        {
          label: '三餐搭配',
          question: '如何安排一日三餐更健康？',
          icon: [
            'M12 21a9 9 0 1 0 0-18 9 9 0 0 0 0 18z',
            'M12 7v5l3 3'
          ]
        },
        {
          label: '饮食益处',
          question: '多吃蔬菜水果有哪些益处？',
          icon: [
            'M12 20s-7-4.5-9-9c-1.5-3.5 1-7 4.5-7 2 0 3.5 1 4.5 2.5C13 5 14.5 4 16.5 4c3.5 0 6 3.5 4.5 7-2 4.5-9 9-9 9z'
          ]
        },
        {
          label: '实用建议',
          question: '控糖减盐有什么实用建议？',
          icon: [
            'M9 18h6M10 21h4',
            'M12 3a6 6 0 0 0-4 10.5c.8.7 1 1.5 1 2.5h6c0-1 .2-1.8 1-2.5A6 6 0 0 0 12 3z'
          ]
        }
      ]
    }
  },
  computed: {
    inputPlaceholder() {
      return this.reportMode
        ? '输入研究主题，例如：健康饮食的基本原则...'
        : '请输入您关于健康饮食的咨询问题...'
    },
    currentAiResponseRendered() {
      return renderMarkdown(this.currentAiResponse)
    }
  },
  methods: {
    handleLoginSuccess(loginVO) {
      this.loggedIn = true
      this.userName = loginVO.userName
      this.authNotice = ''
      this.resetChat()
      this.loadHistory()
    },

    handleLogout() {
      if (this.currentEventSource) {
        this.currentEventSource.close()
        this.currentEventSource = null
      }
      clearAuth()
      this.loggedIn = false
      this.userName = ''
      this.resetChat()
    },

    handleAuthExpired() {
      this.authNotice = '登录已过期，请重新登录'
      this.handleLogout()
    },

    resetChat() {
      this.messages = []
      this.currentReport = null
      this.currentAiResponse = ''
      this.lastUserMessage = ''
      this.isAiTyping = false
      this.isStreaming = false
      this.isReportLoading = false
      this.connectionError = false
    },

    setMode(reportMode) {
      this.reportMode = reportMode
    },

    toggleReportMode() {
      this.reportMode = !this.reportMode
    },

    handleSend(message) {
      if (this.reportMode) {
        this.sendReport(message)
      } else {
        this.sendMessage(message)
      }
    },

    async sendReport(topic) {
      this.addMessage(topic, true)
      this.isReportLoading = true
      this.currentReport = null
      this.connectionError = false
      try {
        this.currentReport = await createResearchReport(topic)
        if (!this.loggedIn) return
      } catch (error) {
        console.error('研究闭环生成失败:', error)
        this.connectionError = true
        const detail = error.isBizError ? error.message : (error.response?.data?.msg || error.message || '请求失败')
        this.addMessage('研究闭环生成失败：' + detail, false)
        setTimeout(() => {
          this.connectionError = false
        }, 5000)
      } finally {
        this.isReportLoading = false
        this.scrollToBottom()
      }
    },

    sendMessage(message) {
      this.addMessage(message, true)
      this.startAiResponse(message)
    },

    addMessage(content, isUser = false) {
      const message = {
        id: Date.now() + Math.random(),
        content,
        isUser,
        timestamp: new Date()
      }
      this.messages.push(message)
      this.scrollToBottom()
    },

    startAiResponse(userMessage) {
      this.isAiTyping = true
      this.isStreaming = true
      this.currentAiResponse = ''
      this.connectionError = false
      this.lastUserMessage = userMessage

      if (this.currentEventSource) {
        this.currentEventSource.close()
      }

      this.currentEventSource = chatWithSSE(
        userMessage,
        this.handleAiMessage,
        this.handleAiError,
        this.handleAiClose
      )
    },

    handleAiMessage(data) {
      this.currentAiResponse += data
      this.scrollToBottom()
    },

    handleAiError(error) {
      console.error('AI 回复出错:', error)
      this.connectionError = true
      this.finishAiResponse()

      setTimeout(() => {
        this.connectionError = false
      }, 5000)
    },

    handleAiClose() {
      this.finishAiResponse()
    },

    finishAiResponse() {
      this.isStreaming = false

      const content = this.currentAiResponse.trim()
      if (content) {
        this.addMessage(content, false)
        saveChatLog(this.lastUserMessage, content).catch(error => {
          console.error('保存聊天记录失败:', error)
        })
      }

      this.isAiTyping = false
      this.currentAiResponse = ''
      this.connectionError = false

      if (this.currentEventSource) {
        this.currentEventSource.close()
        this.currentEventSource = null
      }
    },

    scrollToBottom() {
      this.$nextTick(() => {
        const container = this.$refs.messagesContainer
        if (container) {
          container.scrollTop = container.scrollHeight
        }
      })
    },

    async loadHistory() {
      try {
        const data = await getHistory()
        // 等待期间用户可能已退出或已开始新会话，不覆盖
        if (!this.loggedIn || this.messages.length > 0) return
        const rows = (data || []).slice().reverse()
        const history = []
        rows.forEach(row => {
          const timestamp = row.createdAt ? new Date(row.createdAt) : new Date()
          if (row.requestContent) {
            history.push({ id: `h${row.id}-u`, content: row.requestContent, isUser: true, timestamp })
          }
          if (row.responseContent) {
            history.push({ id: `h${row.id}-a`, content: row.responseContent, isUser: false, timestamp })
          }
        })
        this.messages = history
        this.scrollToBottom()
      } catch (error) {
        console.error('加载历史记录失败:', error)
      }
    }
  },

  mounted() {
    if (this.loggedIn) {
      this.loadHistory()
    }
    window.addEventListener('auth-expired', this.handleAuthExpired)
  },

  beforeUnmount() {
    window.removeEventListener('auth-expired', this.handleAuthExpired)
    if (this.currentEventSource) {
      this.currentEventSource.close()
    }
  }
}
</script>

<style scoped>
.app {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background:
    radial-gradient(600px 320px at 12% -4%, rgba(52, 211, 153, 0.14), transparent 70%),
    radial-gradient(700px 380px at 95% 8%, rgba(163, 230, 53, 0.12), transparent 70%),
    linear-gradient(180deg, #f2faf5 0%, #f6fbf3 100%);
}

/* ===== 头部 ===== */
.app-header {
  background: rgba(255, 255, 255, 0.82);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--line);
  z-index: 10;
}

.header-inner {
  max-width: 880px;
  margin: 0 auto;
  padding: 14px 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
}

.brand-logo {
  width: 44px;
  height: 44px;
  border-radius: 15px;
  background: var(--gradient-primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 14px rgba(22, 163, 74, 0.3);
}

.brand-logo svg {
  width: 24px;
  height: 24px;
}

.app-title {
  font-size: 19px;
  font-weight: 700;
  color: var(--ink-900);
  letter-spacing: 0.5px;
}

.app-subtitle {
  font-size: 12px;
  color: var(--ink-400);
  margin-top: 2px;
}

.mode-switch {
  display: flex;
  gap: 4px;
  background: var(--mint-50);
  border: 1px solid var(--line);
  border-radius: 999px;
  padding: 4px;
}

.mode-option {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 7px 16px;
  border-radius: 999px;
  font-size: 13px;
  color: var(--ink-500);
  transition: background 0.2s, color 0.2s, box-shadow 0.2s;
}

.mode-option svg {
  width: 15px;
  height: 15px;
}

.mode-option:hover:not(.active) {
  color: var(--green-700);
}

.mode-option.active {
  background: var(--gradient-primary);
  color: #fff;
  box-shadow: 0 3px 10px rgba(22, 163, 74, 0.3);
}

/* ===== 用户区 ===== */
.user-area {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-chip {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 7px 14px;
  border-radius: 999px;
  background: var(--mint-100);
  border: 1px solid var(--mint-200);
  color: var(--green-700);
  font-size: 13px;
  font-weight: 600;
  max-width: 140px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-chip svg {
  width: 15px;
  height: 15px;
  flex-shrink: 0;
}

.logout-button {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: 50%;
  border: 1px solid var(--line);
  background: var(--card);
  color: var(--ink-400);
  transition: color 0.2s, border-color 0.2s, background 0.2s;
}

.logout-button:hover {
  color: var(--danger-500);
  border-color: #fecaca;
  background: #fef2f2;
}

.logout-button svg {
  width: 16px;
  height: 16px;
}

/* ===== 聊天区域 ===== */
.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  scroll-behavior: smooth;
}

.messages-inner {
  max-width: 880px;
  margin: 0 auto;
  padding: 24px 20px 8px;
}

/* ===== 欢迎页 ===== */
.welcome-message {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100% - 8px);
  padding: 24px 0;
}

.welcome-content {
  text-align: center;
  max-width: 620px;
  animation: welcome-in 0.5s ease-out;
}

.welcome-badge {
  width: 72px;
  height: 72px;
  margin: 0 auto 20px;
  border-radius: 24px;
  background: var(--gradient-primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 10px 30px rgba(22, 163, 74, 0.3);
}

.welcome-badge svg {
  width: 38px;
  height: 38px;
}

.welcome-content h2 {
  font-size: 24px;
  font-weight: 700;
  color: var(--ink-900);
  margin-bottom: 12px;
}

.welcome-desc {
  font-size: 14px;
  line-height: 1.8;
  color: var(--ink-500);
  max-width: 460px;
  margin: 0 auto 28px;
}

.suggestion-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.suggestion-card {
  display: flex;
  align-items: center;
  gap: 12px;
  text-align: left;
  background: var(--card);
  border: 1px solid var(--line);
  border-radius: var(--radius-md);
  padding: 14px 16px;
  box-shadow: var(--shadow-sm);
  transition: transform 0.2s, border-color 0.2s, box-shadow 0.2s;
}

.suggestion-card:hover {
  transform: translateY(-2px);
  border-color: var(--mint-200);
  box-shadow: var(--shadow-md);
}

.suggestion-icon {
  flex-shrink: 0;
  width: 38px;
  height: 38px;
  border-radius: 12px;
  background: var(--mint-100);
  color: var(--green-600);
  display: flex;
  align-items: center;
  justify-content: center;
}

.suggestion-icon svg {
  width: 20px;
  height: 20px;
}

.suggestion-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.suggestion-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--ink-900);
}

.suggestion-question {
  font-size: 12px;
  color: var(--ink-400);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* ===== 流式回复 / 加载中消息 ===== */
.chat-message {
  display: flex;
  margin-bottom: 22px;
  animation: message-in 0.3s ease-out;
}

.ai-message {
  justify-content: flex-start;
}

.message-avatar {
  flex-shrink: 0;
  margin: 0 10px;
}

.avatar {
  width: 38px;
  height: 38px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar svg {
  width: 20px;
  height: 20px;
}

.ai-avatar {
  background: var(--gradient-primary);
  color: #fff;
  box-shadow: 0 3px 10px rgba(22, 163, 74, 0.28);
}

.message-content {
  max-width: 72%;
  min-width: 80px;
}

.message-bubble {
  padding: 12px 16px;
  border-radius: 18px;
  border-bottom-left-radius: 6px;
  background: var(--card);
  border: 1px solid var(--line);
  box-shadow: var(--shadow-sm);
  word-wrap: break-word;
  word-break: break-word;
}

.loading-bubble {
  display: flex;
  align-items: center;
  gap: 10px;
}

.loading-text {
  font-size: 13px;
  color: var(--ink-400);
}

.ai-typing-content {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.ai-response-text {
  min-height: 4px;
}

/* ===== 连接错误提示 ===== */
.connection-error {
  position: fixed;
  top: 18px;
  left: 50%;
  transform: translateX(-50%);
  background: var(--card);
  color: var(--danger-500);
  border: 1px solid #fecaca;
  border-left: 4px solid var(--danger-500);
  padding: 12px 18px;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(229, 72, 77, 0.18);
  z-index: 1000;
  animation: slide-down 0.3s ease-out;
}

.error-content {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}

.error-content svg {
  width: 17px;
  height: 17px;
  flex-shrink: 0;
}

@keyframes slide-down {
  from {
    transform: translateX(-50%) translateY(-120%);
    opacity: 0;
  }
  to {
    transform: translateX(-50%) translateY(0);
    opacity: 1;
  }
}

@keyframes message-in {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes welcome-in {
  from {
    opacity: 0;
    transform: translateY(14px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* ===== 滚动条 ===== */
.messages-container::-webkit-scrollbar {
  width: 6px;
}

.messages-container::-webkit-scrollbar-track {
  background: transparent;
}

.messages-container::-webkit-scrollbar-thumb {
  background: var(--mint-200);
  border-radius: 3px;
}

.messages-container::-webkit-scrollbar-thumb:hover {
  background: var(--green-500);
}

@media (max-width: 768px) {
  .header-inner {
    padding: 12px 14px;
    justify-content: center;
  }

  .brand-logo {
    width: 38px;
    height: 38px;
    border-radius: 13px;
  }

  .app-title {
    font-size: 17px;
  }

  .messages-inner {
    padding: 16px 12px 8px;
  }

  .welcome-content h2 {
    font-size: 20px;
  }

  .suggestion-grid {
    grid-template-columns: 1fr;
  }

  .message-content {
    max-width: 82%;
  }

  .message-avatar {
    margin: 0 6px;
  }

  .avatar {
    width: 32px;
    height: 32px;
    border-radius: 11px;
  }

  .avatar svg {
    width: 17px;
    height: 17px;
  }
}
</style>