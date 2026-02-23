import axios from 'axios'

const api = axios.create({
  // 로컬 개발: VITE_API_BASE_URL 환경변수로 백엔드 직접 접속 (예: http://localhost:8080)
  // Docker/운영: 빈 문자열(상대경로) → nginx가 /api/ 요청을 backend:8080으로 프록시
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Request interceptor for auth token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('admin_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Refresh Token 갱신 상태 관리
let isRefreshing = false
let failedQueue = []

const processQueue = (error, token = null) => {
  failedQueue.forEach(({ resolve, reject }) => {
    if (error) {
      reject(error)
    } else {
      resolve(token)
    }
  })
  failedQueue = []
}

const clearAuthAndRedirect = () => {
  localStorage.removeItem('admin_token')
  localStorage.removeItem('admin_refresh_token')
  if (window.location.pathname.startsWith('/admin') &&
      window.location.pathname !== '/admin/login') {
    window.location.href = '/admin/login'
  }
}

// Response interceptor: 401 발생 시 Refresh Token으로 자동 갱신 시도
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config

    if (error.response?.status !== 401 || originalRequest._retry) {
      return Promise.reject(error)
    }

    // 로그인/토큰 갱신 요청 자체가 401이면 바로 로그아웃
    if (originalRequest.url?.includes('/api/admin/login') ||
        originalRequest.url?.includes('/api/admin/token/refresh')) {
      clearAuthAndRedirect()
      return Promise.reject(error)
    }

    // 이미 갱신 중이면 큐에 대기
    if (isRefreshing) {
      return new Promise((resolve, reject) => {
        failedQueue.push({ resolve, reject })
      }).then((token) => {
        originalRequest.headers.Authorization = `Bearer ${token}`
        return api(originalRequest)
      })
    }

    originalRequest._retry = true
    isRefreshing = true

    const refreshToken = localStorage.getItem('admin_refresh_token')
    if (!refreshToken) {
      processQueue(error, null)
      isRefreshing = false
      clearAuthAndRedirect()
      return Promise.reject(error)
    }

    try {
      const response = await axios.post(
        (import.meta.env.VITE_API_BASE_URL || '') + '/api/admin/token/refresh',
        { refreshToken }
      )
      const { accessToken, refreshToken: newRefreshToken } = response.data.data
      localStorage.setItem('admin_token', accessToken)
      localStorage.setItem('admin_refresh_token', newRefreshToken)

      processQueue(null, accessToken)

      originalRequest.headers.Authorization = `Bearer ${accessToken}`
      return api(originalRequest)
    } catch (refreshError) {
      processQueue(refreshError, null)
      clearAuthAndRedirect()
      return Promise.reject(refreshError)
    } finally {
      isRefreshing = false
    }
  }
)

export default api
