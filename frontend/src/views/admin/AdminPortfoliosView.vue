<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { usePortfolioStore } from '@/stores/portfolio'
import {
  Plus,
  Edit2,
  Trash2,
  Eye,
  EyeOff,
  LogOut,
  Mail,
  LayoutDashboard,
  X,
  Save,
  Loader2,
  Image,
  GripVertical
} from 'lucide-vue-next'

const router = useRouter()
const authStore = useAuthStore()
const portfolioStore = usePortfolioStore()

const showModal = ref(false)
const modalMode = ref('create') // 'create' | 'edit'
const isSaving = ref(false)
const isDeleting = ref(false)
const deleteConfirmId = ref(null)

const form = reactive({
  id: null,
  title: '',
  client: '',
  industry: '',
  description: '',
  thumbnail: '',
  features: '',
  isVisible: true
})

const industries = [
  '전자/반도체',
  '자동차/기계',
  '화학/소재',
  '섬유/의류',
  '식품/농산물',
  '의료/바이오',
  '철강/금속',
  '기타'
]

onMounted(() => {
  portfolioStore.fetchPortfolios()
})

const openCreateModal = () => {
  modalMode.value = 'create'
  resetForm()
  showModal.value = true
}

const openEditModal = (portfolio) => {
  modalMode.value = 'edit'
  form.id = portfolio.id
  form.title = portfolio.title
  form.client = portfolio.client
  form.industry = portfolio.industry
  form.description = portfolio.description
  form.thumbnail = portfolio.thumbnail
  form.features = portfolio.features.join(', ')
  form.isVisible = portfolio.isVisible !== false
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
  resetForm()
}

const resetForm = () => {
  form.id = null
  form.title = ''
  form.client = ''
  form.industry = ''
  form.description = ''
  form.thumbnail = ''
  form.features = ''
  form.isVisible = true
}

const savePortfolio = async () => {
  isSaving.value = true

  // Simulate API call
  await new Promise(resolve => setTimeout(resolve, 1000))

  const portfolioData = {
    id: form.id || Date.now(),
    title: form.title,
    client: form.client,
    industry: form.industry,
    description: form.description,
    thumbnail: form.thumbnail || 'https://images.unsplash.com/photo-1551288049-bebda4e38f71?w=400',
    features: form.features.split(',').map(f => f.trim()).filter(f => f),
    images: [form.thumbnail || 'https://images.unsplash.com/photo-1551288049-bebda4e38f71?w=800'],
    isVisible: form.isVisible,
    createdAt: new Date().toISOString().split('T')[0]
  }

  if (modalMode.value === 'create') {
    portfolioStore.portfolios.unshift(portfolioData)
  } else {
    const index = portfolioStore.portfolios.findIndex(p => p.id === form.id)
    if (index !== -1) {
      portfolioStore.portfolios[index] = portfolioData
    }
  }

  isSaving.value = false
  closeModal()
}

const confirmDelete = (id) => {
  deleteConfirmId.value = id
}

const cancelDelete = () => {
  deleteConfirmId.value = null
}

const deletePortfolio = async (id) => {
  isDeleting.value = true

  // Simulate API call
  await new Promise(resolve => setTimeout(resolve, 500))

  portfolioStore.portfolios = portfolioStore.portfolios.filter(p => p.id !== id)

  isDeleting.value = false
  deleteConfirmId.value = null
}

const toggleVisibility = async (portfolio) => {
  portfolio.isVisible = !portfolio.isVisible
  // In real app, call API to update visibility
}

const logout = () => {
  authStore.logout()
  router.push('/admin/login')
}
</script>

<template>
  <div class="min-h-screen bg-gray-100">
    <!-- Admin Header -->
    <header class="bg-white shadow-sm">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex items-center justify-between h-16">
          <div class="flex items-center space-x-4">
            <div class="flex items-center space-x-2">
              <div class="w-8 h-8 bg-primary-600 rounded-lg flex items-center justify-center">
                <span class="text-white font-bold">S</span>
              </div>
              <span class="text-lg font-bold text-gray-900">SalesBoost</span>
              <span class="text-sm text-gray-500">Admin</span>
            </div>
          </div>

          <nav class="flex items-center space-x-4">
            <router-link
              to="/admin/inquiries"
              class="flex items-center px-3 py-2 rounded-lg text-sm font-medium transition-colors cursor-pointer"
              :class="$route.name === 'admin-inquiries' ? 'bg-primary-100 text-primary-700' : 'text-gray-600 hover:bg-gray-100'"
            >
              <Mail class="w-4 h-4 mr-2" />
              제휴문의
            </router-link>
            <router-link
              to="/admin/portfolios"
              class="flex items-center px-3 py-2 rounded-lg text-sm font-medium transition-colors cursor-pointer"
              :class="$route.name === 'admin-portfolios' ? 'bg-primary-100 text-primary-700' : 'text-gray-600 hover:bg-gray-100'"
            >
              <LayoutDashboard class="w-4 h-4 mr-2" />
              포트폴리오
            </router-link>
            <button
              @click="logout"
              class="flex items-center px-3 py-2 rounded-lg text-sm font-medium text-gray-600 hover:bg-gray-100 transition-colors cursor-pointer"
            >
              <LogOut class="w-4 h-4 mr-2" />
              로그아웃
            </button>
          </nav>
        </div>
      </div>
    </header>

    <!-- Main Content -->
    <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div class="flex items-center justify-between mb-8">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">포트폴리오 관리</h1>
          <p class="text-gray-600">포트폴리오 항목을 등록하고 관리합니다.</p>
        </div>
        <button
          @click="openCreateModal"
          class="btn-primary"
        >
          <Plus class="w-5 h-5 mr-2" />
          새 포트폴리오
        </button>
      </div>

      <!-- Portfolio Grid -->
      <div class="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div
          v-for="portfolio in portfolioStore.portfolios"
          :key="portfolio.id"
          class="bg-white rounded-xl shadow-sm overflow-hidden"
          :class="{ 'opacity-60': portfolio.isVisible === false }"
        >
          <!-- Thumbnail -->
          <div class="relative h-40">
            <img
              :src="portfolio.thumbnail"
              :alt="portfolio.title"
              class="w-full h-full object-cover"
            />
            <div class="absolute top-2 right-2 flex space-x-1">
              <span
                class="px-2 py-1 rounded text-xs font-medium"
                :class="portfolio.isVisible !== false ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-600'"
              >
                {{ portfolio.isVisible !== false ? '노출' : '숨김' }}
              </span>
            </div>
          </div>

          <!-- Content -->
          <div class="p-4">
            <div class="flex items-center gap-2 mb-2">
              <span class="text-xs px-2 py-0.5 bg-primary-100 text-primary-700 rounded-full">
                {{ portfolio.industry }}
              </span>
            </div>
            <h3 class="font-semibold text-gray-900 mb-1">{{ portfolio.title }}</h3>
            <p class="text-sm text-gray-500 mb-4 line-clamp-2">{{ portfolio.description }}</p>

            <!-- Actions -->
            <div class="flex items-center justify-between pt-3 border-t">
              <button
                @click="toggleVisibility(portfolio)"
                class="p-2 hover:bg-gray-100 rounded-lg transition-colors cursor-pointer"
                :title="portfolio.isVisible !== false ? '숨기기' : '노출하기'"
              >
                <Eye v-if="portfolio.isVisible !== false" class="w-4 h-4 text-gray-500" />
                <EyeOff v-else class="w-4 h-4 text-gray-500" />
              </button>
              <div class="flex items-center space-x-1">
                <button
                  @click="openEditModal(portfolio)"
                  class="p-2 hover:bg-gray-100 rounded-lg transition-colors cursor-pointer"
                  title="수정"
                >
                  <Edit2 class="w-4 h-4 text-gray-500" />
                </button>
                <button
                  v-if="deleteConfirmId !== portfolio.id"
                  @click="confirmDelete(portfolio.id)"
                  class="p-2 hover:bg-red-50 rounded-lg transition-colors cursor-pointer"
                  title="삭제"
                >
                  <Trash2 class="w-4 h-4 text-red-500" />
                </button>
                <div
                  v-else
                  class="flex items-center space-x-1"
                >
                  <button
                    @click="deletePortfolio(portfolio.id)"
                    :disabled="isDeleting"
                    class="px-2 py-1 bg-red-600 text-white text-xs rounded hover:bg-red-700 disabled:opacity-50 cursor-pointer"
                  >
                    {{ isDeleting ? '삭제중...' : '확인' }}
                  </button>
                  <button
                    @click="cancelDelete"
                    class="px-2 py-1 bg-gray-200 text-gray-700 text-xs rounded hover:bg-gray-300 cursor-pointer"
                  >
                    취소
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Empty State -->
        <div
          v-if="portfolioStore.portfolios.length === 0"
          class="col-span-full text-center py-12"
        >
          <div class="w-20 h-20 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
            <Image class="w-10 h-10 text-gray-400" />
          </div>
          <h3 class="text-lg font-medium text-gray-900 mb-2">포트폴리오가 없습니다</h3>
          <p class="text-gray-500 mb-4">새 포트폴리오를 등록해보세요.</p>
          <button
            @click="openCreateModal"
            class="btn-primary"
          >
            <Plus class="w-5 h-5 mr-2" />
            새 포트폴리오
          </button>
        </div>
      </div>
    </main>

    <!-- Create/Edit Modal -->
    <Teleport to="body">
      <Transition
        enter-active-class="transition duration-200 ease-out"
        enter-from-class="opacity-0"
        enter-to-class="opacity-100"
        leave-active-class="transition duration-150 ease-in"
        leave-from-class="opacity-100"
        leave-to-class="opacity-0"
      >
        <div
          v-if="showModal"
          class="fixed inset-0 z-50 flex items-center justify-center p-4"
        >
          <!-- Backdrop -->
          <div
            class="absolute inset-0 bg-black/50"
            @click="closeModal"
          ></div>

          <!-- Modal -->
          <div class="relative bg-white rounded-2xl shadow-xl max-w-lg w-full max-h-[90vh] overflow-hidden">
            <!-- Header -->
            <div class="flex items-center justify-between p-6 border-b">
              <h2 class="text-lg font-semibold text-gray-900">
                {{ modalMode === 'create' ? '새 포트폴리오' : '포트폴리오 수정' }}
              </h2>
              <button
                @click="closeModal"
                class="p-2 hover:bg-gray-100 rounded-lg cursor-pointer"
                aria-label="닫기"
              >
                <X class="w-5 h-5" />
              </button>
            </div>

            <!-- Form -->
            <form @submit.prevent="savePortfolio" class="p-6 space-y-4 overflow-y-auto max-h-[60vh]">
              <div>
                <label class="label-text">제목 <span class="text-red-500">*</span></label>
                <input
                  v-model="form.title"
                  type="text"
                  class="input-field"
                  placeholder="포트폴리오 제목"
                  required
                />
              </div>

              <div class="grid grid-cols-2 gap-4">
                <div>
                  <label class="label-text">고객사 <span class="text-red-500">*</span></label>
                  <input
                    v-model="form.client"
                    type="text"
                    class="input-field"
                    placeholder="ABC Company"
                    required
                  />
                </div>
                <div>
                  <label class="label-text">업종 <span class="text-red-500">*</span></label>
                  <select
                    v-model="form.industry"
                    class="input-field"
                    required
                  >
                    <option value="" disabled>선택</option>
                    <option v-for="ind in industries" :key="ind" :value="ind">
                      {{ ind }}
                    </option>
                  </select>
                </div>
              </div>

              <div>
                <label class="label-text">설명 <span class="text-red-500">*</span></label>
                <textarea
                  v-model="form.description"
                  rows="3"
                  class="input-field resize-none"
                  placeholder="프로젝트 설명..."
                  required
                ></textarea>
              </div>

              <div>
                <label class="label-text">썸네일 URL</label>
                <input
                  v-model="form.thumbnail"
                  type="url"
                  class="input-field"
                  placeholder="https://example.com/image.jpg"
                />
              </div>

              <div>
                <label class="label-text">적용 기능 (쉼표로 구분)</label>
                <input
                  v-model="form.features"
                  type="text"
                  class="input-field"
                  placeholder="PI 자동 생성, PO 접수 관리, 출하현황 대시보드"
                />
              </div>

              <div class="flex items-center">
                <input
                  id="isVisible"
                  v-model="form.isVisible"
                  type="checkbox"
                  class="w-4 h-4 text-primary-600 border-gray-300 rounded focus:ring-primary-500"
                />
                <label for="isVisible" class="ml-2 text-sm text-gray-700">
                  사이트에 노출
                </label>
              </div>
            </form>

            <!-- Footer -->
            <div class="flex items-center justify-end gap-3 p-6 border-t bg-gray-50">
              <button
                type="button"
                @click="closeModal"
                class="px-4 py-2 text-gray-700 hover:bg-gray-200 rounded-lg transition-colors cursor-pointer"
              >
                취소
              </button>
              <button
                @click="savePortfolio"
                :disabled="isSaving || !form.title || !form.client || !form.industry || !form.description"
                class="btn-primary"
              >
                <Loader2 v-if="isSaving" class="w-4 h-4 mr-2 animate-spin" />
                <Save v-else class="w-4 h-4 mr-2" />
                {{ isSaving ? '저장 중...' : '저장' }}
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
