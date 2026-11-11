<template>
  <div class="chat-input-wrapper">
    <div class="input-bar" :class="{ focused, disabled }">
      <textarea
        ref="inputRef"
        v-model="inputMessage"
        :placeholder="placeholder"
        :disabled="disabled"
        rows="1"
        aria-label="输入消息"
        @focus="focused = true"
        @blur="focused = false"
        @input="adjustHeight"
        @keydown="handleKeyDown"
      ></textarea>
      <button
        class="send-btn"
        :disabled="disabled || !inputMessage.trim()"
        aria-label="发送"
        title="发送"
        @click="sendMessage"
      >
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M22 2 11 13" />
          <path d="M22 2l-7 20-4-9-9-4 20-7z" />
        </svg>
      </button>
    </div>
    <div class="input-hint">Enter 发送 · Shift + Enter 换行</div>
  </div>
</template>

<script>
export default {
  name: 'ChatInput',
  props: {
    disabled: {
      type: Boolean,
      default: false
    },
    placeholder: {
      type: String,
      default: '请输入您的问题...'
    }
  },
  emits: ['send-message'],
  data() {
    return {
      inputMessage: '',
      focused: false
    }
  },
  methods: {
    sendMessage() {
      if (this.inputMessage.trim() && !this.disabled) {
        this.$emit('send-message', this.inputMessage.trim())
        this.inputMessage = ''
        this.adjustHeight()
      }
    },
    handleKeyDown(event) {
      // isComposing：中文输入法候选词确认时不触发发送
      if (event.key === 'Enter' && !event.shiftKey && !event.isComposing) {
        event.preventDefault()
        this.sendMessage()
      }
    },
    adjustHeight() {
      this.$nextTick(() => {
        const textarea = this.$refs.inputRef
        if (!textarea) return
        textarea.style.height = 'auto'
        textarea.style.height = Math.min(textarea.scrollHeight, 140) + 'px'
      })
    },
    focus() {
      this.$refs.inputRef.focus()
    }
  },
  mounted() {
    this.adjustHeight()
  }
}
</script>

<style scoped>
.chat-input-wrapper {
  width: 100%;
  max-width: 880px;
  margin: 0 auto;
  padding: 12px 20px 14px;
}

.input-bar {
  display: flex;
  align-items: flex-end;
  gap: 10px;
  background: var(--card);
  border: 1.5px solid var(--line);
  border-radius: var(--radius-lg);
  padding: 10px 10px 10px 18px;
  box-shadow: var(--shadow-sm);
  transition: border-color 0.2s, box-shadow 0.2s;
}

.input-bar.focused {
  border-color: var(--green-500);
  box-shadow: 0 0 0 4px rgba(34, 197, 94, 0.12), var(--shadow-sm);
}

.input-bar.disabled {
  background: #fafdfb;
  opacity: 0.75;
}

textarea {
  flex: 1;
  border: none;
  outline: none;
  resize: none;
  font-family: inherit;
  font-size: 14px;
  line-height: 1.6;
  color: var(--ink-900);
  background: transparent;
  max-height: 140px;
  padding: 6px 0;
  overflow-y: auto;
}

textarea::placeholder {
  color: var(--ink-400);
}

.send-btn {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--gradient-primary);
  color: #fff;
  box-shadow: 0 4px 12px rgba(22, 163, 74, 0.3);
  transition: transform 0.15s, box-shadow 0.15s, opacity 0.2s;
}

.send-btn svg {
  width: 18px;
  height: 18px;
  margin-left: -2px;
}

.send-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(22, 163, 74, 0.4);
}

.send-btn:active:not(:disabled) {
  transform: translateY(0);
}

.send-btn:disabled {
  background: var(--mint-100);
  color: var(--ink-400);
  box-shadow: none;
}

.input-hint {
  text-align: center;
  font-size: 11px;
  color: var(--ink-400);
  margin-top: 8px;
}

@media (max-width: 768px) {
  .chat-input-wrapper {
    padding: 8px 12px 10px;
  }

  textarea {
    font-size: 16px; /* 防止 iOS 聚焦自动缩放 */
  }

  .input-hint {
    display: none;
  }
}
</style>