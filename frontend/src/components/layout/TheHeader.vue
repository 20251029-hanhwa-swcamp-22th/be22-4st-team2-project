<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { Menu, X } from 'lucide-vue-next'

const route = useRoute()
const isScrolled = ref(false)
const isMobileMenuOpen = ref(false)

const navItems = [
  { name: '서비스 소개', path: '/service' },
  { name: '포트폴리오', path: '/portfolio' },
  { name: '제휴 문의', path: '/inquiry' }
]

const handleScroll = () => {
  isScrolled.value = window.scrollY > 20
}

const toggleMobileMenu = () => {
  isMobileMenuOpen.value = !isMobileMenuOpen.value
}

const closeMobileMenu = () => {
  isMobileMenuOpen.value = false
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<template>
  <header
    class="fixed top-0 left-0 right-0 z-50 transition-all duration-300"
    :class="[
      isScrolled
        ? 'bg-white/95 backdrop-blur-md shadow-sm'
        : 'bg-transparent'
    ]"
  >
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex items-center justify-between h-20">
        <!-- Logo -->
        <router-link
          to="/"
          class="flex items-center space-x-2 cursor-pointer"
          @click="closeMobileMenu"
        >
          <div class="w-10 h-10 bg-primary-600 rounded-xl flex items-center justify-center">
            <span class="text-white font-bold text-xl">S</span>
          </div>
          <span
            class="text-xl font-bold transition-colors duration-300"
            :class="isScrolled ? 'text-gray-900' : 'text-gray-900'"
          >
            SalesBoost
          </span>
        </router-link>

        <!-- Desktop Navigation -->
        <nav class="hidden md:flex items-center space-x-8">
          <router-link
            v-for="item in navItems"
            :key="item.path"
            :to="item.path"
            class="text-base font-medium transition-colors duration-200 cursor-pointer"
            :class="[
              route.path === item.path
                ? 'text-primary-600'
                : isScrolled
                  ? 'text-gray-700 hover:text-primary-600'
                  : 'text-gray-700 hover:text-primary-600'
            ]"
          >
            {{ item.name }}
          </router-link>
          <router-link
            to="/inquiry"
            class="btn-primary"
          >
            무료 상담 신청
          </router-link>
        </nav>

        <!-- Mobile Menu Button -->
        <button
          class="md:hidden p-2 rounded-lg hover:bg-gray-100 transition-colors cursor-pointer"
          @click="toggleMobileMenu"
          aria-label="메뉴 열기"
        >
          <Menu v-if="!isMobileMenuOpen" class="w-6 h-6 text-gray-700" />
          <X v-else class="w-6 h-6 text-gray-700" />
        </button>
      </div>
    </div>

    <!-- Mobile Menu -->
    <Transition
      enter-active-class="transition duration-200 ease-out"
      enter-from-class="opacity-0 -translate-y-2"
      enter-to-class="opacity-100 translate-y-0"
      leave-active-class="transition duration-150 ease-in"
      leave-from-class="opacity-100 translate-y-0"
      leave-to-class="opacity-0 -translate-y-2"
    >
      <div
        v-if="isMobileMenuOpen"
        class="md:hidden absolute top-full left-0 right-0 bg-white shadow-lg border-t"
      >
        <nav class="flex flex-col py-4 px-4 space-y-2">
          <router-link
            v-for="item in navItems"
            :key="item.path"
            :to="item.path"
            class="py-3 px-4 rounded-lg text-base font-medium transition-colors cursor-pointer"
            :class="[
              route.path === item.path
                ? 'bg-primary-50 text-primary-600'
                : 'text-gray-700 hover:bg-gray-50'
            ]"
            @click="closeMobileMenu"
          >
            {{ item.name }}
          </router-link>
          <router-link
            to="/inquiry"
            class="btn-primary mt-2 text-center"
            @click="closeMobileMenu"
          >
            무료 상담 신청
          </router-link>
        </nav>
      </div>
    </Transition>
  </header>
</template>
