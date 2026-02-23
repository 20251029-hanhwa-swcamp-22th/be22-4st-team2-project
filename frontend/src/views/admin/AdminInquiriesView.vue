<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useInquiryStore } from '@/stores/inquiry'
import {
  Search,
  Filter,
  ChevronLeft,
  ChevronRight,
  LogOut,
  Mail,
  Phone,
  Building2,
  User,
  Calendar,
  MessageSquare,
  X,
  Save,
  Loader2,
  FileText,
  LayoutDashboard
} from 'lucide-vue-next'

const router = useRouter()
const authStore = useAuthStore()
const inquiryStore = useInquiryStore()

const searchQuery = ref('')
const statusFilter = ref('')
const selectedInquiry = ref(null)
const adminMemo = ref('')
const isSavingMemo = ref(false)
const isDetailLoading = ref(false)
let searchTimeout = null

const statusOptions = [
  { value: '', label: '전체' },
  { value: 'PENDING', label: '대기' },
  { value: 'IN_PROGRESS', label: '진행' },
  { value: 'DONE', label: '완료' }
]

const statusLabels = {
  PENDING: { label: '대기', color: 'bg-yellow-100 text-yellow-700' },
  IN_PROGRESS: { label: '진행', color: 'bg-blue-100 text-blue-700' },
  DONE: { label: '완료', color: 'bg-green-100 text-green-700' }
}

const inquiryTypeLabels = {
  PARTNERSHIP: '제휴 문의',
  GENERAL: '일반 문의',
  QUOTE: '견적 문의',
  ETC: '기타 문의'
}

const fetchInquiries = (page = 1) => {
  inquiryStore.fetchInquiries({
    status: statusFilter.value || undefined,
    keyword: searchQuery.value || undefined,
    page,
    size: 10
  })
}

onMounted(() => {
  fetchInquiries()
})

watch(statusFilter, () => {
  fetchInquiries(1)
})

watch(searchQuery, () => {
  clearTimeout(searchTimeout)
  searchTimeout = setTimeout(() => {
    fetchInquiries(1)
  }, 300)
})

const goToPage = (page) => {
  if (page < 1 || page > inquiryStore.pagination.totalPages) return
  fetchInquiries(page)
}

const openDetail = async (inquiry) => {
  selectedInquiry.value = inquiry
  adminMemo.value = ''
  isDetailLoading.value = true
  await inquiryStore.fetchInquiryById(inquiry.id)
  isDetailLoading.value = false
  if (inquiryStore.currentInquiry) {
    selectedInquiry.value = inquiryStore.currentInquiry
    adminMemo.value = inquiryStore.currentInquiry.adminMemo || ''
  }
}

const closeDetail = () => {
  selectedInquiry.value = null
  adminMemo.value = ''
}

const updateStatus = async (status) => {
  if (!selectedInquiry.value) return

  const result = await inquiryStore.updateInquiryStatus(selectedInquiry.value.id, status)
  if (result.success) {
    selectedInquiry.value.status = status
  }
}

const saveMemo = async () => {
  if (!selectedInquiry.value) return

  isSavingMemo.value = true
  const result = await inquiryStore.updateInquiryMemo(selectedInquiry.value.id, adminMemo.value)
  isSavingMemo.value = false

  if (result.success) {
    selectedInquiry.value.adminMemo = adminMemo.value
  }
}

const formatDate = (dateString) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
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
      <div class="mb-8">
        <h1 class="text-2xl font-bold text-gray-900">제휴문의 관리</h1>
        <p class="text-gray-600">접수된 제휴문의를 관리합니다.</p>
      </div>

      <!-- Filters -->
      <div class="bg-white rounded-xl shadow-sm p-4 mb-6">
        <div class="flex flex-col sm:flex-row gap-4">
          <!-- Search -->
          <div class="flex-1 relative">
            <Search class="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
            <input
              v-model="searchQuery"
              type="text"
              placeholder="기업명, 담당자, 이메일로 검색"
              class="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
            />
          </div>

          <!-- Status Filter -->
          <div class="flex items-center space-x-2">
            <Filter class="w-5 h-5 text-gray-400" />
            <select
              v-model="statusFilter"
              class="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
            >
              <option
                v-for="option in statusOptions"
                :key="option.value"
                :value="option.value"
              >
                {{ option.label }}
              </option>
            </select>
          </div>
        </div>
      </div>

      <!-- Inquiries Table -->
      <div class="bg-white rounded-xl shadow-sm overflow-hidden">
        <div class="overflow-x-auto">
          <table class="w-full">
            <thead class="bg-gray-50">
              <tr>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">기업/담당자</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">연락처</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">문의유형</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">상태</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">접수일</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-200">
              <tr
                v-for="inquiry in inquiryStore.inquiries"
                :key="inquiry.id"
                class="hover:bg-gray-50 cursor-pointer transition-colors"
                @click="openDetail(inquiry)"
              >
                <td class="px-6 py-4">
                  <div class="flex items-center">
                    <div class="w-10 h-10 bg-primary-100 rounded-full flex items-center justify-center mr-3">
                      <Building2 class="w-5 h-5 text-primary-600" />
                    </div>
                    <div>
                      <div class="font-medium text-gray-900">{{ inquiry.companyName }}</div>
                      <div class="text-sm text-gray-500">{{ inquiry.contactName }}</div>
                    </div>
                  </div>
                </td>
                <td class="px-6 py-4">
                  <div class="text-sm text-gray-900">{{ inquiry.email }}</div>
                  <div class="text-sm text-gray-500">{{ inquiry.phone }}</div>
                </td>
                <td class="px-6 py-4">
                  <span class="text-sm text-gray-700">
                    {{ inquiryTypeLabels[inquiry.inquiryType] || inquiry.inquiryType }}
                  </span>
                </td>
                <td class="px-6 py-4">
                  <span
                    class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium"
                    :class="statusLabels[inquiry.status]?.color"
                  >
                    {{ statusLabels[inquiry.status]?.label }}
                  </span>
                </td>
                <td class="px-6 py-4 text-sm text-gray-500">
                  {{ formatDate(inquiry.createdAt) }}
                </td>
              </tr>
              <tr v-if="inquiryStore.inquiries.length === 0 && !inquiryStore.loading">
                <td colspan="5" class="px-6 py-12 text-center text-gray-500">
                  조회된 문의가 없습니다.
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Pagination -->
        <div class="bg-gray-50 px-6 py-3 flex items-center justify-between border-t">
          <div class="text-sm text-gray-500">
            총 {{ inquiryStore.pagination.total }}건
          </div>
          <div class="flex items-center space-x-2">
            <button
              @click="goToPage(inquiryStore.pagination.page - 1)"
              :disabled="inquiryStore.pagination.page <= 1 || inquiryStore.loading"
              class="p-2 rounded-lg hover:bg-gray-200 disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer"
            >
              <ChevronLeft class="w-5 h-5" />
            </button>
            <span class="px-3 py-1 bg-primary-600 text-white rounded-lg text-sm">
              {{ inquiryStore.pagination.page }}
            </span>
            <span class="text-sm text-gray-500">/ {{ inquiryStore.pagination.totalPages || 1 }}</span>
            <button
              @click="goToPage(inquiryStore.pagination.page + 1)"
              :disabled="inquiryStore.pagination.page >= inquiryStore.pagination.totalPages || inquiryStore.loading"
              class="p-2 rounded-lg hover:bg-gray-200 disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer"
            >
              <ChevronRight class="w-5 h-5" />
            </button>
          </div>
        </div>
      </div>
    </main>

    <!-- Detail Sidebar -->
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
          v-if="selectedInquiry"
          class="fixed inset-0 z-50"
        >
          <!-- Backdrop -->
          <div
            class="absolute inset-0 bg-black/50"
            @click="closeDetail"
          ></div>

          <!-- Sidebar -->
          <div class="absolute right-0 top-0 bottom-0 w-full max-w-lg bg-white shadow-xl">
            <div class="flex flex-col h-full">
              <!-- Header -->
              <div class="flex items-center justify-between p-6 border-b">
                <h2 class="text-lg font-semibold text-gray-900">문의 상세</h2>
                <button
                  @click="closeDetail"
                  class="p-2 hover:bg-gray-100 rounded-lg cursor-pointer"
                  aria-label="닫기"
                >
                  <X class="w-5 h-5" />
                </button>
              </div>

              <!-- Loading -->
              <div v-if="isDetailLoading" class="flex-1 flex items-center justify-center">
                <Loader2 class="w-8 h-8 animate-spin text-primary-600" />
              </div>

              <!-- Content -->
              <div v-else class="flex-1 overflow-y-auto p-6 space-y-6">
                <!-- Status -->
                <div>
                  <label class="text-sm font-medium text-gray-700 mb-2 block">처리 상태</label>
                  <div class="flex space-x-2">
                    <button
                      v-for="status in ['PENDING', 'IN_PROGRESS', 'DONE']"
                      :key="status"
                      @click="updateStatus(status)"
                      class="px-4 py-2 rounded-lg text-sm font-medium transition-colors cursor-pointer"
                      :class="selectedInquiry.status === status
                        ? statusLabels[status].color
                        : 'bg-gray-100 text-gray-600 hover:bg-gray-200'"
                    >
                      {{ statusLabels[status].label }}
                    </button>
                  </div>
                </div>

                <!-- Company Info -->
                <div class="bg-gray-50 rounded-lg p-4 space-y-3">
                  <div class="flex items-center">
                    <Building2 class="w-5 h-5 text-gray-400 mr-3" />
                    <div>
                      <div class="text-sm text-gray-500">기업명</div>
                      <div class="font-medium text-gray-900">{{ selectedInquiry.companyName }}</div>
                    </div>
                  </div>
                  <div class="flex items-center">
                    <User class="w-5 h-5 text-gray-400 mr-3" />
                    <div>
                      <div class="text-sm text-gray-500">담당자</div>
                      <div class="font-medium text-gray-900">{{ selectedInquiry.contactName }}</div>
                    </div>
                  </div>
                  <div class="flex items-center">
                    <Mail class="w-5 h-5 text-gray-400 mr-3" />
                    <div>
                      <div class="text-sm text-gray-500">이메일</div>
                      <a :href="'mailto:' + selectedInquiry.email" class="font-medium text-primary-600 hover:underline">
                        {{ selectedInquiry.email }}
                      </a>
                    </div>
                  </div>
                  <div class="flex items-center">
                    <Phone class="w-5 h-5 text-gray-400 mr-3" />
                    <div>
                      <div class="text-sm text-gray-500">연락처</div>
                      <a :href="'tel:' + selectedInquiry.phone" class="font-medium text-primary-600 hover:underline">
                        {{ selectedInquiry.phone }}
                      </a>
                    </div>
                  </div>
                  <div class="flex items-center">
                    <Calendar class="w-5 h-5 text-gray-400 mr-3" />
                    <div>
                      <div class="text-sm text-gray-500">접수일</div>
                      <div class="font-medium text-gray-900">{{ formatDate(selectedInquiry.createdAt) }}</div>
                    </div>
                  </div>
                </div>

                <!-- Inquiry Content -->
                <div>
                  <div class="flex items-center mb-2">
                    <MessageSquare class="w-5 h-5 text-gray-400 mr-2" />
                    <label class="text-sm font-medium text-gray-700">문의 내용</label>
                  </div>
                  <div class="bg-gray-50 rounded-lg p-4 text-gray-700 whitespace-pre-wrap">
                    {{ selectedInquiry.content }}
                  </div>
                </div>

                <!-- Admin Memo -->
                <div>
                  <div class="flex items-center justify-between mb-2">
                    <div class="flex items-center">
                      <FileText class="w-5 h-5 text-gray-400 mr-2" />
                      <label class="text-sm font-medium text-gray-700">관리자 메모</label>
                    </div>
                    <button
                      @click="saveMemo"
                      :disabled="isSavingMemo"
                      class="inline-flex items-center px-3 py-1.5 bg-primary-600 text-white text-sm rounded-lg hover:bg-primary-700 disabled:opacity-50 cursor-pointer"
                    >
                      <Loader2 v-if="isSavingMemo" class="w-4 h-4 mr-1 animate-spin" />
                      <Save v-else class="w-4 h-4 mr-1" />
                      저장
                    </button>
                  </div>
                  <textarea
                    v-model="adminMemo"
                    rows="4"
                    class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 resize-none"
                    placeholder="내부 메모를 입력하세요..."
                  ></textarea>
                </div>
              </div>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>
