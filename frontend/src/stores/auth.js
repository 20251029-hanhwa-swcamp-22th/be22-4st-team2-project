import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/services/api'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('admin_token') || null)
  const user = ref(null)

  const isAuthenticated = computed(() => !!token.value)

  async function login(credentials) {
    try {
      const response = await api.post('/api/admin/login', credentials)
      const accessToken = response.data.data.accessToken
      token.value = accessToken
      localStorage.setItem('admin_token', accessToken)
      return { success: true }
    } catch (error) {
      return { success: false, message: error.response?.data?.message || '로그인에 실패했습니다.' }
    }
  }

  function logout() {
    token.value = null
    user.value = null
    localStorage.removeItem('admin_token')
  }

  return {
    token,
    user,
    isAuthenticated,
    login,
    logout
  }
})
