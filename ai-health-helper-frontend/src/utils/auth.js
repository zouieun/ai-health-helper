const TOKEN_KEY = 'ai_helper_token'
const USER_KEY = 'ai_helper_user'

export function getToken() {
    return localStorage.getItem(TOKEN_KEY) || ''
}

export function getUser() {
    try {
        return JSON.parse(localStorage.getItem(USER_KEY)) || null
    } catch {
        return null
    }
}

export function setAuth(loginVO) {
    localStorage.setItem(TOKEN_KEY, loginVO.token)
    localStorage.setItem(USER_KEY, JSON.stringify({ id: loginVO.id, userName: loginVO.userName }))
}

export function clearAuth() {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
}

export function isLoggedIn() {
    return !!getToken()
}