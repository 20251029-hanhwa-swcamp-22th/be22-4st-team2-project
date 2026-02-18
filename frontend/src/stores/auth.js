import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/services/api'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('admin_token') || null)
  const user = ref(JSON.parse(localStorage.getItem('admin_user') || 'null'))

  const isAuthenticated = computed(() => !!token.value)

  async function login(credentials) {
    try {
      const response = await api.post('/api/admin/login', credentials)
      token.value = response.data.token
      user.value = response.data.user
      localStorage.setItem('admin_token', response.data.token)
      localStorage.setItem('admin_user', JSON.stringify(response.data.user))
      return { success: true }
    } catch (error) {
      return { success: false, message: error.response?.data?.message || '로그인에 실패했습니다.' }
    }
  }

  function logout() {
    token.value = null
    user.value = null
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_user')
  }

  return {
    token,
    user,
    isAuthenticated,
    login,
    logout
  }
})
