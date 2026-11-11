import { marked } from 'marked'
import DOMPurify from 'dompurify'

/**
 * 渲染 Markdown 并过滤 XSS（AI 回复与研究报告通用）
 * @param {string} text Markdown 原文
 * @returns {string} 安全的 HTML
 */
export function renderMarkdown(text) {
    if (!text) return ''
    const html = marked.parse(text, { breaks: true, gfm: true })
    return DOMPurify.sanitize(html)
}

/**
 * 格式化时间
 * @param {Date} date 日期对象
 * @returns {string} 格式化后的时间字符串
 */
export function formatTime(date) {
    const now = new Date()
    const diff = now - date
    
    if (diff < 60000) { // 1分钟内
        return '刚刚'
    } else if (diff < 3600000) { // 1小时内
        return `${Math.floor(diff / 60000)}分钟前`
    } else if (diff < 86400000) { // 1天内
        return `${Math.floor(diff / 3600000)}小时前`
    } else {
        return date.toLocaleDateString()
    }
}

/**
 * 防抖函数
 * @param {Function} func 要防抖的函数
 * @param {number} wait 等待时间
 * @returns {Function} 防抖后的函数
 */
export function debounce(func, wait) {
    let timeout
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout)
            func(...args)
        }
        clearTimeout(timeout)
        timeout = setTimeout(later, wait)
    }
} 