<template>
  <div class="report-panel">
    <header class="report-header">
      <div class="report-icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
          <path d="M9 3h6" />
          <path d="M10 3v6L4.5 18a2 2 0 0 0 1.8 3h11.4a2 2 0 0 0 1.8-3L14 9V3" />
          <path d="M7.5 14h9" />
        </svg>
      </div>
      <div class="report-heading">
        <div class="report-label">研究闭环报告</div>
        <h3 class="report-title">{{ report.topic }}</h3>
      </div>
      <span class="report-badge" :class="statusClass">{{ statusText }}</span>
    </header>

    <div class="report-body">
      <div class="report-answer message-markdown" v-html="renderedAnswer"></div>

      <section class="report-section">
        <div class="section-heading">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M10 13a5 5 0 0 0 7.5.5l3-3a5 5 0 0 0-7-7l-1.7 1.7" />
            <path d="M14 11a5 5 0 0 0-7.5-.5l-3 3a5 5 0 0 0 7 7l1.7-1.7" />
          </svg>
          <h4>引用来源</h4>
          <span class="section-count">{{ sources.length }}</span>
        </div>
        <p v-if="sources.length === 0" class="empty-text">未命中可用来源</p>
        <div v-for="source in sources" :key="source.id" class="source-item">
          <div class="source-title">
            <span class="source-id">{{ source.id }}</span>
            <span class="source-name">{{ source.title }}</span>
            <span class="source-type" :class="'type-' + source.type">{{ source.type === 'local' ? '本地' : '联网' }}</span>
          </div>
          <a v-if="source.url" class="source-url" :href="source.url" target="_blank" rel="noopener noreferrer">{{ source.url }}</a>
          <p class="source-snippet">{{ source.snippet }}</p>
        </div>
      </section>

      <section v-if="toolTrace.length" class="report-section">
        <div class="section-heading">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M22 12h-4l-3 9L9 3l-3 9H2" />
          </svg>
          <h4>工具轨迹</h4>
        </div>
        <div class="trace-list">
          <div v-for="(trace, index) in toolTrace" :key="index" class="trace-item" :class="'trace-' + trace.status">
            <div class="trace-row">
              <span class="trace-step">{{ trace.step }}</span>
              <span class="trace-tool">{{ trace.tool }}</span>
              <span class="trace-status">{{ trace.status }}</span>
              <span class="trace-time">{{ trace.elapsedMs }} ms</span>
            </div>
            <p class="trace-detail">{{ trace.detail }}</p>
          </div>
        </div>
      </section>
    </div>

    <footer class="report-footer">
      <span v-if="report.elapsedMs != null">总耗时 {{ report.elapsedMs }} ms · </span>内容由 AI 生成，仅供参考
    </footer>
  </div>
</template>

<script>
import { renderMarkdown } from '../utils/index.js'

export default {
  name: 'ResearchReportPanel',
  props: {
    report: {
      type: Object,
      required: true
    }
  },
  computed: {
    renderedAnswer() {
      return renderMarkdown(this.report.answer || '')
    },
    sources() {
      return this.report.sources || []
    },
    toolTrace() {
      return this.report.toolTrace || []
    },
    statusText() {
      const statusMap = {
        SUCCESS: '闭环完成',
        DEGRADED: '降级完成',
        FAILED: '生成失败'
      }
      return statusMap[this.report.status] || this.report.status
    },
    statusClass() {
      const classMap = {
        SUCCESS: 'status-success',
        DEGRADED: 'status-degraded',
        FAILED: 'status-failed'
      }
      return classMap[this.report.status] || 'status-failed'
    }
  }
}
</script>

<style scoped>
.report-panel {
  background: var(--card);
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  overflow: hidden;
  margin: 0 48px 22px;
  animation: report-in 0.35s ease-out;
}

.report-header {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 22px;
  background: linear-gradient(135deg, var(--mint-50), var(--lime-50));
  border-bottom: 1px solid var(--line);
}

.report-icon {
  flex-shrink: 0;
  width: 44px;
  height: 44px;
  border-radius: 14px;
  background: var(--gradient-primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(22, 163, 74, 0.28);
}

.report-icon svg {
  width: 22px;
  height: 22px;
}

.report-heading {
  flex: 1;
  min-width: 0;
}

.report-label {
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 2px;
  color: var(--green-600);
  margin-bottom: 3px;
}

.report-title {
  font-size: 17px;
  font-weight: 600;
  color: var(--ink-900);
  line-height: 1.4;
}

.report-badge {
  flex-shrink: 0;
  font-size: 11px;
  font-weight: 600;
  padding: 4px 12px;
  border-radius: 999px;
}

.status-success {
  background: var(--mint-100);
  color: var(--green-700);
  border: 1px solid var(--mint-200);
}

.status-degraded {
  background: var(--amber-100);
  color: var(--amber-600);
  border: 1px solid #fde68a;
}

.status-failed {
  background: var(--danger-50);
  color: var(--danger-500);
  border: 1px solid #fecaca;
}

.report-body {
  padding: 20px 22px;
}

.report-answer {
  background: var(--mint-50);
  border: 1px solid var(--mint-100);
  border-radius: var(--radius-md);
  padding: 14px 18px;
}

.report-section {
  margin-top: 20px;
}

.section-heading {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.section-heading svg {
  width: 16px;
  height: 16px;
  color: var(--green-600);
}

.section-heading h4 {
  font-size: 14px;
  font-weight: 600;
  color: var(--ink-900);
}

.section-count {
  font-size: 11px;
  font-weight: 600;
  color: var(--green-700);
  background: var(--mint-100);
  border-radius: 999px;
  padding: 1px 8px;
}

.empty-text {
  font-size: 13px;
  color: var(--ink-400);
}

.source-item {
  padding: 12px 14px;
  border: 1px solid var(--line);
  border-radius: var(--radius-md);
  margin-bottom: 8px;
  background: #fafdfb;
  transition: border-color 0.2s;
}

.source-item:hover {
  border-color: var(--mint-200);
}

.source-title {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  font-size: 13px;
  font-weight: 600;
  color: var(--ink-900);
}

.source-id {
  flex-shrink: 0;
  min-width: 22px;
  height: 22px;
  padding: 0 6px;
  border-radius: 7px;
  background: var(--gradient-primary);
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.source-name {
  flex: 1;
  min-width: 0;
}

.source-type {
  flex-shrink: 0;
  padding: 1px 8px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 500;
}

.type-local {
  background: var(--mint-100);
  color: var(--green-700);
}

.type-web {
  background: #e0f2fe;
  color: #0369a1;
}

.source-url {
  display: inline-block;
  margin-top: 6px;
  font-size: 12px;
  color: var(--green-600);
  text-decoration: none;
  word-break: break-all;
}

.source-url:hover {
  text-decoration: underline;
}

.source-snippet {
  margin: 6px 0 0;
  font-size: 12px;
  line-height: 1.6;
  color: var(--ink-500);
  white-space: pre-wrap;
  word-break: break-word;
}

.trace-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.trace-item {
  padding: 10px 14px;
  border-radius: var(--radius-md);
  background: #fafdfb;
  border: 1px solid var(--line);
  border-left: 3px solid var(--ink-400);
}

.trace-success {
  border-left-color: var(--green-500);
}

.trace-empty,
.trace-skipped {
  border-left-color: var(--ink-400);
}

.trace-timeout,
.trace-failed {
  border-left-color: var(--danger-500);
}

.trace-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  font-size: 12px;
}

.trace-step {
  font-weight: 700;
  color: var(--ink-900);
}

.trace-tool {
  color: var(--ink-500);
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
}

.trace-status {
  padding: 1px 8px;
  border-radius: 999px;
  background: var(--mint-100);
  color: var(--green-700);
  font-size: 11px;
}

.trace-timeout .trace-status,
.trace-failed .trace-status {
  background: var(--danger-50);
  color: var(--danger-500);
}

.trace-empty .trace-status,
.trace-skipped .trace-status {
  background: #f1f5f3;
  color: var(--ink-500);
}

.trace-time {
  margin-left: auto;
  color: var(--ink-400);
  font-size: 11px;
}

.trace-detail {
  margin: 5px 0 0;
  font-size: 12px;
  line-height: 1.6;
  color: var(--ink-500);
  word-break: break-word;
}

.report-footer {
  padding: 12px 22px;
  border-top: 1px solid var(--line);
  font-size: 11px;
  color: var(--ink-400);
  background: #fafdfb;
}

@keyframes report-in {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 768px) {
  .report-panel {
    margin: 0 0 22px;
  }

  .report-header {
    padding: 14px 16px;
  }

  .report-body {
    padding: 16px;
  }
}
</style>