<script setup>
import { ref, onMounted } from 'vue'
import { usePortfolioStore } from '@/stores/portfolio'
import { X, Building2, Tag, Calendar, ChevronLeft, ChevronRight } from 'lucide-vue-next'

const portfolioStore = usePortfolioStore()

const selectedPortfolio = ref(null)
const currentImageIndex = ref(0)

onMounted(() => {
  portfolioStore.fetchPortfolios()
})

const openModal = (portfolio) => {
  selectedPortfolio.value = portfolio
  currentImageIndex.value = 0
  document.body.style.overflow = 'hidden'
}

const closeModal = () => {
  selectedPortfolio.value = null
  document.body.style.overflow = ''
}

const nextImage = () => {
  if (selectedPortfolio.value && currentImageIndex.value < selectedPortfolio.value.imageUrls.length - 1) {
    currentImageIndex.value++
  }
}

const prevImage = () => {
  if (currentImageIndex.value > 0) {
    currentImageIndex.value--
  }
}
</script>

<template>
  <div class="pt-20">
    <!-- Hero Section -->
    <section class="py-16 bg-gradient-to-br from-primary-50 to-white">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="text-center max-w-3xl mx-auto">
          <h1 class="text-4xl md:text-5xl font-bold text-gray-900 mb-6">
            포트폴리오
          </h1>
          <p class="text-lg md:text-xl text-gray-600">
            SalesBoost를 도입한 다양한 기업들의 성공 사례를 확인하세요.
          </p>
        </div>
      </div>
    </section>

    <!-- Portfolio Grid -->
    <section class="py-16 bg-white">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <!-- Loading State -->
        <div v-if="portfolioStore.loading" class="grid md:grid-cols-2 lg:grid-cols-3 gap-8">
          <div
            v-for="i in 6"
            :key="i"
            class="animate-pulse"
          >
            <div class="bg-gray-200 h-48 rounded-t-2xl"></div>
            <div class="bg-gray-100 p-6 rounded-b-2xl">
              <div class="h-6 bg-gray-200 rounded mb-4 w-3/4"></div>
              <div class="h-4 bg-gray-200 rounded mb-2"></div>
              <div class="h-4 bg-gray-200 rounded w-2/3"></div>
            </div>
          </div>
        </div>

        <!-- Portfolio Cards -->
        <div v-else class="grid md:grid-cols-2 lg:grid-cols-3 gap-8">
          <div
            v-for="portfolio in portfolioStore.portfolios"
            :key="portfolio.id"
            class="card overflow-hidden cursor-pointer group"
            @click="openModal(portfolio)"
          >
            <!-- Thumbnail -->
            <div class="relative h-48 overflow-hidden bg-gray-100">
              <img
                v-if="portfolio.thumbnailUrl"
                :src="portfolio.thumbnailUrl"
                :alt="portfolio.title"
                class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110"
              />
              <div class="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300"></div>
              <div class="absolute bottom-4 left-4 right-4 text-white opacity-0 group-hover:opacity-100 transition-opacity duration-300">
                <span class="text-sm font-medium">자세히 보기</span>
              </div>
            </div>

            <!-- Content -->
            <div class="p-6">
              <div class="flex items-center gap-2 mb-3">
                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-primary-100 text-primary-700">
                  {{ portfolio.industry }}
                </span>
              </div>
              <h3 class="text-lg font-semibold text-gray-900 mb-2 group-hover:text-primary-600 transition-colors">
                {{ portfolio.title }}
              </h3>
              <p class="text-gray-600 text-sm line-clamp-2">
                {{ portfolio.description }}
              </p>
              <p class="text-gray-500 text-xs mt-3">
                <Building2 class="w-3 h-3 inline mr-1" />
                {{ portfolio.clientName }}
              </p>
            </div>
          </div>
        </div>

        <!-- Empty State -->
        <div
          v-if="!portfolioStore.loading && portfolioStore.portfolios.length === 0"
          class="text-center py-16"
        >
          <div class="w-24 h-24 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-6">
            <Building2 class="w-12 h-12 text-gray-400" />
          </div>
          <h3 class="text-xl font-semibold text-gray-900 mb-2">포트폴리오가 없습니다</h3>
          <p class="text-gray-600">등록된 포트폴리오가 없습니다.</p>
        </div>
      </div>
    </section>

    <!-- Modal -->
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
          v-if="selectedPortfolio"
          class="fixed inset-0 z-50 flex items-center justify-center p-4"
        >
          <!-- Backdrop -->
          <div
            class="absolute inset-0 bg-black/70"
            @click="closeModal"
          ></div>

          <!-- Modal Content -->
          <div class="relative bg-white rounded-2xl shadow-2xl max-w-4xl w-full max-h-[90vh] overflow-hidden">
            <!-- Close Button -->
            <button
              @click="closeModal"
              class="absolute top-4 right-4 z-10 p-2 bg-white/90 rounded-full hover:bg-white transition-colors cursor-pointer"
              aria-label="닫기"
            >
              <X class="w-5 h-5 text-gray-700" />
            </button>

            <div class="grid lg:grid-cols-2">
              <!-- Image Gallery -->
              <div class="relative bg-gray-900 h-64 lg:h-auto">
                <img
                  v-if="selectedPortfolio.imageUrls && selectedPortfolio.imageUrls.length > 0"
                  :src="selectedPortfolio.imageUrls[currentImageIndex]"
                  :alt="selectedPortfolio.title"
                  class="w-full h-full object-cover"
                />
                <div v-else-if="selectedPortfolio.thumbnailUrl" class="w-full h-full">
                  <img :src="selectedPortfolio.thumbnailUrl" :alt="selectedPortfolio.title" class="w-full h-full object-cover" />
                </div>

                <!-- Image Navigation -->
                <div
                  v-if="selectedPortfolio.imageUrls && selectedPortfolio.imageUrls.length > 1"
                  class="absolute inset-x-0 bottom-4 flex items-center justify-center space-x-2"
                >
                  <button
                    @click.stop="prevImage"
                    :disabled="currentImageIndex === 0"
                    class="p-2 bg-white/90 rounded-full hover:bg-white disabled:opacity-50 disabled:cursor-not-allowed transition-colors cursor-pointer"
                    aria-label="이전 이미지"
                  >
                    <ChevronLeft class="w-4 h-4" />
                  </button>
                  <span class="px-3 py-1 bg-white/90 rounded-full text-sm">
                    {{ currentImageIndex + 1 }} / {{ selectedPortfolio.imageUrls.length }}
                  </span>
                  <button
                    @click.stop="nextImage"
                    :disabled="currentImageIndex === selectedPortfolio.imageUrls.length - 1"
                    class="p-2 bg-white/90 rounded-full hover:bg-white disabled:opacity-50 disabled:cursor-not-allowed transition-colors cursor-pointer"
                    aria-label="다음 이미지"
                  >
                    <ChevronRight class="w-4 h-4" />
                  </button>
                </div>
              </div>

              <!-- Details -->
              <div class="p-8 overflow-y-auto max-h-[60vh] lg:max-h-[80vh]">
                <div class="flex items-center gap-2 mb-4">
                  <span class="inline-flex items-center px-3 py-1 rounded-full text-sm font-medium bg-primary-100 text-primary-700">
                    <Tag class="w-4 h-4 mr-1" />
                    {{ selectedPortfolio.industry }}
                  </span>
                </div>

                <h2 class="text-2xl font-bold text-gray-900 mb-2">
                  {{ selectedPortfolio.title }}
                </h2>

                <div class="flex items-center text-gray-500 text-sm mb-6">
                  <Building2 class="w-4 h-4 mr-1" />
                  {{ selectedPortfolio.clientName }}
                  <span class="mx-2">·</span>
                  <Calendar class="w-4 h-4 mr-1" />
                  {{ selectedPortfolio.createdAt ? new Date(selectedPortfolio.createdAt).toLocaleDateString('ko-KR') : '' }}
                </div>

                <p class="text-gray-600">
                  {{ selectedPortfolio.description }}
                </p>
              </div>
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
