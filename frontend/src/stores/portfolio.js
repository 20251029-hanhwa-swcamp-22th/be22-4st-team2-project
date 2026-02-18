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
      portfolios.value = response.data
    } catch (err) {
      error.value = '포트폴리오를 불러오는데 실패했습니다.'
      // Use mock data if API fails
      portfolios.value = getMockPortfolios()
    } finally {
      loading.value = false
    }
  }

  async function fetchPortfolioById(id) {
    loading.value = true
    error.value = null
    try {
      const response = await api.get(`/api/portfolios/${id}`)
      currentPortfolio.value = response.data
    } catch (err) {
      error.value = '포트폴리오 상세 정보를 불러오는데 실패했습니다.'
      currentPortfolio.value = getMockPortfolios().find(p => p.id === parseInt(id))
    } finally {
      loading.value = false
    }
  }

  // Mock data for development
  function getMockPortfolios() {
    return [
      {
        id: 1,
        title: '글로벌 전자부품 제조사',
        client: 'ABC Electronics',
        industry: '전자/반도체',
        description: 'PI/PO 문서 자동화 및 출하관리 시스템 구축으로 업무 효율 40% 향상',
        thumbnail: 'https://images.unsplash.com/photo-1518770660439-4636190af475?w=400',
        images: [
          'https://images.unsplash.com/photo-1518770660439-4636190af475?w=800',
          'https://images.unsplash.com/photo-1581091226825-a6a2a5aee158?w=800'
        ],
        features: ['PI 자동 생성', 'PO 접수 관리', '출하현황 대시보드'],
        createdAt: '2024-12-01'
      },
      {
        id: 2,
        title: '자동차 부품 수출기업',
        client: 'AutoParts Korea',
        industry: '자동차/기계',
        description: '복잡한 CI/PL 문서 작업을 자동화하여 연간 2,000시간 절감',
        thumbnail: 'https://images.unsplash.com/photo-1565043666747-69f6646db940?w=400',
        images: [
          'https://images.unsplash.com/photo-1565043666747-69f6646db940?w=800'
        ],
        features: ['CI/PL 자동 생성', '인수인계 패키지', '거래처 관리'],
        createdAt: '2024-11-15'
      },
      {
        id: 3,
        title: '화학제품 무역회사',
        client: 'ChemTrade Co.',
        industry: '화학/소재',
        description: '다국어 무역서류 지원 및 실시간 출하현황 모니터링 시스템 구축',
        thumbnail: 'https://images.unsplash.com/photo-1532187863486-abf9dbad1b69?w=400',
        images: [
          'https://images.unsplash.com/photo-1532187863486-abf9dbad1b69?w=800'
        ],
        features: ['다국어 지원', '실시간 출하현황', '판매현황 분석'],
        createdAt: '2024-10-20'
      },
      {
        id: 4,
        title: '섬유/의류 수출업체',
        client: 'Fashion Export',
        industry: '섬유/의류',
        description: '시즌별 대량 주문 관리 및 생산지시 자동화로 리드타임 30% 단축',
        thumbnail: 'https://images.unsplash.com/photo-1558171813-4c088753af8f?w=400',
        images: [
          'https://images.unsplash.com/photo-1558171813-4c088753af8f?w=800'
        ],
        features: ['생산지시 관리', '대량 주문 처리', '배송 추적'],
        createdAt: '2024-09-10'
      }
    ]
  }

  return {
    portfolios,
    currentPortfolio,
    loading,
    error,
    fetchPortfolios,
    fetchPortfolioById
  }
})
