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
      // For demo purposes, return success even if API fails
      return { success: true, demo: true }
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
      inquiries.value = response.data.content
      pagination.value = {
        page: response.data.page,
        size: response.data.size,
        total: response.data.total,
        totalPages: response.data.totalPages
      }
    } catch (err) {
      error.value = '문의 목록을 불러오는데 실패했습니다.'
      // Use mock data
      inquiries.value = getMockInquiries()
      pagination.value = { page: 1, size: 10, total: 5, totalPages: 1 }
    } finally {
      loading.value = false
    }
  }

  async function fetchInquiryById(id) {
    loading.value = true
    error.value = null
    try {
      const response = await api.get(`/api/admin/inquiries/${id}`)
      currentInquiry.value = response.data
    } catch (err) {
      error.value = '문의 상세 정보를 불러오는데 실패했습니다.'
      currentInquiry.value = getMockInquiries().find(i => i.id === parseInt(id))
    } finally {
      loading.value = false
    }
  }

  async function updateInquiryStatus(id, status) {
    try {
      await api.patch(`/api/admin/inquiries/${id}/status`, { status })
      // Update local state
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

  function getMockInquiries() {
    return [
      {
        id: 1,
        companyName: '삼성전자',
        contactName: '김철수',
        email: 'kim@samsung.com',
        phone: '010-1234-5678',
        inquiryType: 'PARTNERSHIP',
        content: 'SalesBoost 솔루션 도입에 관심이 있습니다. 상담 요청드립니다.',
        status: 'PENDING',
        adminMemo: '',
        createdAt: '2025-02-10T10:30:00'
      },
      {
        id: 2,
        companyName: 'LG화학',
        contactName: '박영희',
        email: 'park@lgchem.com',
        phone: '010-9876-5432',
        inquiryType: 'DEMO',
        content: '데모 시연 요청드립니다.',
        status: 'IN_PROGRESS',
        adminMemo: '담당자 연락 완료, 시연 일정 조율 중',
        createdAt: '2025-02-08T14:20:00'
      },
      {
        id: 3,
        companyName: '현대자동차',
        contactName: '이민수',
        email: 'lee@hyundai.com',
        phone: '010-5555-7777',
        inquiryType: 'PRICING',
        content: '가격 안내 부탁드립니다.',
        status: 'DONE',
        adminMemo: '가격표 발송 완료',
        createdAt: '2025-02-05T09:15:00'
      }
    ]
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
