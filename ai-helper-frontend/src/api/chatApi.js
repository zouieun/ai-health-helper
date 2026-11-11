import axios from 'axios'
import { getToken, clearAuth } from '../utils/auth.js'

// 配置axios基础URL
// 代理后端接口
const API_BASE_URL = '/api'

// 带令牌拦截的axios实例：请求自动携带token，401时清除登录态并通知App回到登录页
const http = axios.create()

http.interceptors.request.use(config => {
    const token = getToken()
    if (token) {
        config.headers['token'] = token
    }
    return config
})

http.interceptors.response.use(
    response => response,
    error => {
        if (error.response?.status === 401) {
            clearAuth()
            window.dispatchEvent(new CustomEvent('auth-expired'))
        }
        return Promise.reject(error)
    }
)

/**
 * 解包后端统一响应 Result{code,msg,data}：code=1 成功返回 data；
 * 业务失败（HTTP 200 + code=0）抛出携带 msg 的 Error，交给调用方展示
 */
function unwrapResult(response) {
    const result = response.data
    if (result?.code !== 1) {
        const error = new Error(result?.msg || '请求失败')
        error.isBizError = true
        throw error
    }
    return result.data
}

/**
 * 登录：POST /ai/login，成功返回 { id, userName, token }
 * 不走http实例，避免登录失败的401被当成登录态过期
 */
export async function login(username, password) {
    const response = await axios.post(`${API_BASE_URL}/ai/login`, { username, password }, {
        timeout: 10000
    })
    return unwrapResult(response)
}

/**
 * 注册：POST /ai/register，code=1 即成功；与login一样不走http实例
 */
export async function register(username, password) {
    const response = await axios.post(`${API_BASE_URL}/ai/register`, { username, password }, {
        timeout: 10000
    })
    unwrapResult(response)
}

/**
 * 使用 SSE 方式调用聊天接口
 * @param {string} message 用户消息
 * @param {Function} onMessage 接收消息的回调函数
 * @param {Function} onError 错误处理回调函数
 * @param {Function} onClose 连接关闭回调函数
 * @returns {EventSource} 返回 EventSource 对象，用于手动关闭连接
 */
export function chatWithSSE(message, onMessage, onError, onClose) {
    // 构建URL参数
    const params = new URLSearchParams({
        message: message
    })

    // EventSource无法自定义请求头，令牌通过query参数传递（后端拦截器已支持）
    const token = getToken()
    if (token) {
        params.append('token', token)
    }

    // 创建 EventSource 连接
    const eventSource = new EventSource(`${API_BASE_URL}/ai/chat?${params}`)

    // EventSource 没有 close 事件：一次性 SSE 流结束后浏览器会触发
    // onerror 并自动重连。收到过数据后的断流视为正常结束，否则才是连接错误。
    let receivedData = false
    let settled = false

    const settle = (callback, arg) => {
        if (settled) return
        settled = true
        eventSource.close()
        callback && callback(arg)
    }

    // 处理接收到的消息
    eventSource.onmessage = function(event) {
        try {
            const data = event.data
            if (data && data.trim() !== '') {
                receivedData = true
                onMessage(data)
            }
        } catch (error) {
            console.error('解析消息失败:', error)
            settle(onError, error)
        }
    }

    // 处理流结束或连接错误
    eventSource.onerror = function() {
        if (receivedData) {
            settle(onClose)
        } else {
            settle(onError, new Error('SSE 连接失败，请检查后端服务是否启动'))
        }
    }

    return eventSource
}

/**
 * 调用研究闭环接口：本地检索 -> MCP 联网检索 -> 生成带引用报告
 * @param {string} topic 研究主题
 * @returns {Promise} 报告数据，包含 status/topic/answer/sources/toolTrace/elapsedMs
 */
export async function createResearchReport(topic) {
    const response = await http.post(`${API_BASE_URL}/ai/report`, { topic }, {
        timeout: 90000
    })
    return unwrapResult(response)
}

/**
 * 拉取当前用户最近聊天记录（新→旧，最多50轮）
 * @returns {Promise} [{ id, requestContent, responseContent, createdAt }]
 */
export async function getHistory() {
    const response = await http.get(`${API_BASE_URL}/ai/messages`, {
        params: { limit: 50 },
        timeout: 10000
    })
    return unwrapResult(response)
}

/**
 * 保存一轮聊天记录（用户消息 + 完整AI回复），user_id由后端从token解析
 */
export async function saveChatLog(requestContent, responseContent) {
    const response = await http.post(`${API_BASE_URL}/ai`, { requestContent, responseContent }, {
        timeout: 10000
    })
    unwrapResult(response)
}