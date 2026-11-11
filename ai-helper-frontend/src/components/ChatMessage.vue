<template>
  <div class="chat-message" :class="isUser ? 'user-message' : 'ai-message'">
    <div v-if="!isUser" class="message-avatar">
      <div class="avatar ai-avatar" aria-label="AI 助手">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
          <path d="M12 3l1.9 5.1L19 10l-5.1 1.9L12 17l-1.9-5.1L5 10l5.1-1.9L12 3z" />
          <path d="M19 15l.9 2.1L22 18l-2.1.9L19 21l-.9-2.1L16 18l2.1-.9L19 15z" />
        </svg>
      </div>
    </div>
    <div class="message-content">
      <div class="message-bubble" :class="isUser ? 'user-bubble' : 'ai-bubble'">
        <div v-if="isUser" class="user-text">{{ message }}</div>
        <div v-else class="message-markdown" v-html="renderedContent"></div>
      </div>
      <div class="message-time">{{ formattedTime }}</div>
    </div>
    <div v-if="isUser" class="message-avatar">
      <div class="avatar user-avatar" aria-label="我">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
          <path d="M20 21a8 8 0 0 0-16 0" />
          <circle cx="12" cy="8" r="4" />
        </svg>
      </div>
    </div>
  </div>
</template>

<script>
import { renderMarkdown, formatTime } from '../utils/index.js'

export default {
  name: 'ChatMessage',
  props: {
    message: {
      type: String,
      required: true
    },
    isUser: {
      type: Boolean,
      default: false
    },
    timestamp: {
      type: [Date, String, Number],
      default: null
    }
  },
  computed: {
    renderedContent() {
      return renderMarkdown(this.message)
    },
    formattedTime() {
      return formatTime(this.timestamp)
    }
  }
}
</script>

<style scoped>
.chat-message {
  display: flex;
  margin-bottom: 22px;
  animation: message-in 0.3s ease-out;
}

.ai-message {
  justify-content: flex-start;
}

.user-message {
  justify-content: flex-end;
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

.user-avatar {
  background: var(--card);
  border: 1.5px solid var(--line);
  color: var(--green-600);
}

.message-content {
  max-width: 72%;
  min-width: 80px;
}

.user-message .message-content {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.message-bubble {
  padding: 12px 16px;
  border-radius: 18px;
  word-wrap: break-word;
  word-break: break-word;
}

.ai-bubble {
  background: var(--card);
  border: 1px solid var(--line);
  border-bottom-left-radius: 6px;
  box-shadow: var(--shadow-sm);
}

.user-bubble {
  background: var(--gradient-primary);
  border-bottom-right-radius: 6px;
  box-shadow: 0 4px 14px rgba(22, 163, 74, 0.25);
}

.user-text {
  font-size: 14px;
  line-height: 1.7;
  color: #fff;
  white-space: pre-wrap;
}

.message-time {
  font-size: 11px;
  color: var(--ink-400);
  margin-top: 5px;
  padding: 0 4px;
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

@media (max-width: 768px) {
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