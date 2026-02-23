import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/services/api'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('admin_token') || null)
  const refreshToken = ref(localStorage.getItem('admin_refresh_token') || null)
  const user = ref(null)

  const isAuthenticated = computed(() => !!token.value)

  async function login(credentials) {
    try {
      const response = await api.post('/api/admin/login', credentials)
      const { accessToken, refreshToken: newRefreshToken } = response.data.data
      token.value = accessToken
      refreshToken.value = newRefreshToken
      localStorage.setItem('admin_token', accessToken)
      localStorage.setItem('admin_refresh_token', newRefreshToken)
      return { success: true }
    } catch (error) {
      return { success: false, message: error.response?.data?.message || '로그인에 실패했습니다.' }
    }
  }

  async function logout() {
    try {
      if (token.value) {
        await api.post('/api/admin/logout')
      }
    } catch {
      // 서버 로그아웃 실패해도 로컬 상태는 정리
    } finally {
      token.value = null
      refreshToken.value = null
      user.value = null
      localStorage.removeItem('admin_token')
      localStorage.removeItem('admin_refresh_token')
    }
  }

  return {
    token,
    refreshToken,
    user,
    isAuthenticated,
    login,
    logout
  }
})
