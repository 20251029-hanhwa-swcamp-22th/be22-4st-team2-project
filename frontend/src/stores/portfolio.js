import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '@/services/api'

export const usePortfolioStore = defineStore('portfolio', () => {
  const portfolios = ref([])
  const currentPortfolio = ref(null)
  const loading = ref(false)
  const error = ref(null)

  async function fetchPortfolios() {
    loading.value = true
    error.value = null
    try {
      const response = await api.get('/api/portfolios')
      portfolios.value = response.data.data
    } catch (err) {
      error.value = '포트폴리오를 불러오는데 실패했습니다.'
      portfolios.value = []
    } finally {
      loading.value = false
    }
  }

  async function fetchAdminPortfolios() {
    loading.value = true
    error.value = null
    try {
      const response = await api.get('/api/admin/portfolios')
      portfolios.value = response.data.data
    } catch (err) {
      error.value = '포트폴리오를 불러오는데 실패했습니다.'
      portfolios.value = []
    } finally {
      loading.value = false
    }
  }

  async function fetchPortfolioById(id) {
    loading.value = true
    error.value = null
    try {
      const response = await api.get(`/api/portfolios/${id}`)
      currentPortfolio.value = response.data.data
    } catch (err) {
      error.value = '포트폴리오 상세 정보를 불러오는데 실패했습니다.'
      currentPortfolio.value = null
    } finally {
      loading.value = false
    }
  }

  return {
    portfolios,
    currentPortfolio,
    loading,
    error,
    fetchPortfolios,
    fetchAdminPortfolios,
    fetchPortfolioById
  }
})
