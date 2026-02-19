import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '@/services/api'

export const useInquiryStore = defineStore('inquiry', () => {
  const inquiries = ref([])
  const currentInquiry = ref(null)
  const loading = ref(false)
  const error = ref(null)
  const pagination = ref({
    page: 1,
    size: 10,
    total: 0,
    totalPages: 0
  })

  async function submitInquiry(inquiryData) {
    loading.value = true
    error.value = null
    try {
      const response = await api.post('/api/inquiries', inquiryData)
      return { success: true, data: response.data }
    } catch (err) {
      error.value = '문의 등록에 실패했습니다.'
      return { success: false, message: err.response?.data?.message || '문의 등록 중 오류가 발생했습니다.' }
    } finally {
      loading.value = false
    }
  }

  // Admin functions
  async function fetchInquiries(params = {}) {
    loading.value = true
    error.value = null
    try {
      const response = await api.get('/api/admin/inquiries', { params })
      const data = response.data.data
      inquiries.value = data.items
      pagination.value = {
        page: data.page,
        size: data.size,
        total: data.totalCount,
        totalPages: Math.ceil(data.totalCount / data.size)
      }
    } catch (err) {
      error.value = '문의 목록을 불러오는데 실패했습니다.'
      inquiries.value = []
      pagination.value = { page: 1, size: 10, total: 0, totalPages: 0 }
    } finally {
      loading.value = false
    }
  }

  async function fetchInquiryById(id) {
    loading.value = true
    error.value = null
    try {
      const response = await api.get(`/api/admin/inquiries/${id}`)
      currentInquiry.value = response.data.data
    } catch (err) {
      error.value = '문의 상세 정보를 불러오는데 실패했습니다.'
      currentInquiry.value = null
    } finally {
      loading.value = false
    }
  }

  async function updateInquiryStatus(id, status) {
    try {
      await api.patch(`/api/admin/inquiries/${id}/status`, { status })
      const inquiry = inquiries.value.find(i => i.id === id)
      if (inquiry) inquiry.status = status
      return { success: true }
    } catch (err) {
      return { success: false, message: '상태 변경에 실패했습니다.' }
    }
  }

  async function updateInquiryMemo(id, memo) {
    try {
      await api.patch(`/api/admin/inquiries/${id}/memo`, { memo })
      if (currentInquiry.value?.id === id) {
        currentInquiry.value.adminMemo = memo
      }
      return { success: true }
    } catch (err) {
      return { success: false, message: '메모 저장에 실패했습니다.' }
    }
  }

  return {
    inquiries,
    currentInquiry,
    loading,
    error,
    pagination,
    submitInquiry,
    fetchInquiries,
    fetchInquiryById,
    updateInquiryStatus,
    updateInquiryMemo
  }
})
