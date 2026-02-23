<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { usePortfolioStore } from '@/stores/portfolio'
import api from '@/services/api'
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
  AlertCircle
} from 'lucide-vue-next'

const router = useRouter()
const authStore = useAuthStore()
const portfolioStore = usePortfolioStore()

const showModal = ref(false)
const modalMode = ref('create') // 'create' | 'edit'
const isSaving = ref(false)
const isDeleting = ref(false)
const deleteConfirmId = ref(null)
const saveError = ref('')

const form = reactive({
  id: null,
  title: '',
  clientName: '',
  industry: '',
  description: '',
  thumbnailUrl: '',
  visible: true
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
  portfolioStore.fetchAdminPortfolios()
})

const openCreateModal = () => {
  modalMode.value = 'create'
  resetForm()
  saveError.value = ''
  showModal.value = true
}

const openEditModal = (portfolio) => {
  modalMode.value = 'edit'
  form.id = portfolio.id
  form.title = portfolio.title
  form.clientName = portfolio.clientName
  form.industry = portfolio.industry
  form.description = portfolio.description
  form.thumbnailUrl = portfolio.thumbnailUrl || ''
  form.visible = portfolio.visible
  saveError.value = ''
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
  resetForm()
}

const resetForm = () => {
  form.id = null
  form.title = ''
  form.clientName = ''
  form.industry = ''
  form.description = ''
  form.thumbnailUrl = ''
  form.visible = true
}

const savePortfolio = async () => {
  isSaving.value = true
  saveError.value = ''

  const payload = {
    title: form.title,
    clientName: form.clientName,
    industry: form.industry,
    description: form.description,
    thumbnailUrl: form.thumbnailUrl,
    visible: form.visible
  }

  try {
    if (modalMode.value === 'create') {
      await api.post('/api/admin/portfolios', payload)
    } else {
      await api.put(`/api/admin/portfolios/${form.id}`, payload)
    }
    await portfolioStore.fetchAdminPortfolios()
    closeModal()
  } catch (err) {
    saveError.value = err.response?.data?.message || '저장 중 오류가 발생했습니다.'
  } finally {
    isSaving.value = false
  }
}

const confirmDelete = (id) => {
  deleteConfirmId.value = id
}

const cancelDelete = () => {
  deleteConfirmId.value = null
}

const deletePortfolio = async (id) => {
  isDeleting.value = true
  try {
    await api.delete(`/api/admin/portfolios/${id}`)
    await portfolioStore.fetchAdminPortfolios()
  } catch (err) {
    alert('삭제 중 오류가 발생했습니다.')
  } finally {
    isDeleting.value = false
    deleteConfirmId.value = null
  }
}

const toggleVisibility = async (portfolio) => {
  try {
    await api.patch(`/api/admin/portfolios/${portfolio.id}/visibility`, { visible: !portfolio.visible })
    await portfolioStore.fetchAdminPortfolios()
  } catch (err) {
    alert('노출 상태 변경 중 오류가 발생했습니다.')
  }
}

const logout = async () => {
  await authStore.logout()
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

      <!-- Loading -->
      <div v-if="portfolioStore.loading" class="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div v-for="i in 6" :key="i" class="animate-pulse bg-white rounded-xl h-64"></div>
      </div>

      <!-- Portfolio Grid -->
      <div v-else class="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div
          v-for="portfolio in portfolioStore.portfolios"
          :key="portfolio.id"
          class="bg-white rounded-xl shadow-sm overflow-hidden"
          :class="{ 'opacity-60': !portfolio.visible }"
        >
          <!-- Thumbnail -->
          <div class="relative h-40 bg-gray-100">
            <img
              v-if="portfolio.thumbnailUrl"
              :src="portfolio.thumbnailUrl"
              :alt="portfolio.title"
              class="w-full h-full object-cover"
            />
            <div v-else class="w-full h-full flex items-center justify-center">
              <Image class="w-10 h-10 text-gray-300" />
            </div>
            <div class="absolute top-2 right-2">
              <span
                class="px-2 py-1 rounded text-xs font-medium"
                :class="portfolio.visible ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-600'"
              >
                {{ portfolio.visible ? '노출' : '숨김' }}
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
            <p class="text-sm text-gray-500 mb-1">{{ portfolio.clientName }}</p>
            <p class="text-sm text-gray-400 mb-4 line-clamp-2">{{ portfolio.description }}</p>

            <!-- Actions -->
            <div class="flex items-center justify-between pt-3 border-t">
              <button
                @click="toggleVisibility(portfolio)"
                class="p-2 hover:bg-gray-100 rounded-lg transition-colors cursor-pointer"
                :title="portfolio.visible ? '숨기기' : '노출하기'"
              >
                <Eye v-if="portfolio.visible" class="w-4 h-4 text-gray-500" />
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
                <div v-else class="flex items-center space-x-1">
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
          <button @click="openCreateModal" class="btn-primary">
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
          <div class="absolute inset-0 bg-black/50" @click="closeModal"></div>

          <div class="relative bg-white rounded-2xl shadow-xl max-w-lg w-full max-h-[90vh] overflow-hidden">
            <div class="flex items-center justify-between p-6 border-b">
              <h2 class="text-lg font-semibold text-gray-900">
                {{ modalMode === 'create' ? '새 포트폴리오' : '포트폴리오 수정' }}
              </h2>
              <button @click="closeModal" class="p-2 hover:bg-gray-100 rounded-lg cursor-pointer" aria-label="닫기">
                <X class="w-5 h-5" />
              </button>
            </div>

            <form @submit.prevent="savePortfolio" class="p-6 space-y-4 overflow-y-auto max-h-[60vh]">
              <div v-if="saveError" class="p-3 bg-red-50 border border-red-200 rounded-lg flex items-start">
                <AlertCircle class="w-4 h-4 text-red-500 mr-2 mt-0.5 flex-shrink-0" />
                <span class="text-red-700 text-sm">{{ saveError }}</span>
              </div>

              <div>
                <label class="label-text">제목 <span class="text-red-500">*</span></label>
                <input v-model="form.title" type="text" class="input-field" placeholder="포트폴리오 제목" required />
              </div>

              <div class="grid grid-cols-2 gap-4">
                <div>
                  <label class="label-text">고객사 <span class="text-red-500">*</span></label>
                  <input v-model="form.clientName" type="text" class="input-field" placeholder="ABC Company" required />
                </div>
                <div>
                  <label class="label-text">업종 <span class="text-red-500">*</span></label>
                  <select v-model="form.industry" class="input-field" required>
                    <option value="" disabled>선택</option>
                    <option v-for="ind in industries" :key="ind" :value="ind">{{ ind }}</option>
                  </select>
                </div>
              </div>

              <div>
                <label class="label-text">설명 <span class="text-red-500">*</span></label>
                <textarea v-model="form.description" rows="3" class="input-field resize-none" placeholder="프로젝트 설명..." required></textarea>
              </div>

              <div>
                <label class="label-text">썸네일 URL</label>
                <input v-model="form.thumbnailUrl" type="url" class="input-field" placeholder="https://example.com/image.jpg" />
              </div>

              <div class="flex items-center">
                <input
                  id="visible"
                  v-model="form.visible"
                  type="checkbox"
                  class="w-4 h-4 text-primary-600 border-gray-300 rounded focus:ring-primary-500"
                />
                <label for="visible" class="ml-2 text-sm text-gray-700">사이트에 노출</label>
              </div>
            </form>

            <div class="flex items-center justify-end gap-3 p-6 border-t bg-gray-50">
              <button type="button" @click="closeModal" class="px-4 py-2 text-gray-700 hover:bg-gray-200 rounded-lg transition-colors cursor-pointer">
                취소
              </button>
              <button
                @click="savePortfolio"
                :disabled="isSaving || !form.title || !form.clientName || !form.industry || !form.description"
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
