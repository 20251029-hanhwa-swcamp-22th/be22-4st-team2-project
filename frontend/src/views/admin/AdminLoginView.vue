<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { Lock, User, Loader2, AlertCircle, Eye, EyeOff } from 'lucide-vue-next'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const form = reactive({
  username: '',
  password: ''
})

const showPassword = ref(false)
const isLoading = ref(false)
const error = ref('')

const handleSubmit = async () => {
  if (!form.username || !form.password) {
    error.value = '아이디와 비밀번호를 입력해주세요.'
    return
  }

  isLoading.value = true
  error.value = ''

  const result = await authStore.login(form)

  isLoading.value = false

  if (result.success) {
    const redirectPath = route.query.redirect || '/admin/inquiries'
    router.push(redirectPath)
  } else {
    error.value = result.message || '로그인에 실패했습니다.'
  }
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-primary-50 to-secondary-50 p-4">
    <div class="w-full max-w-md">
      <!-- Logo -->
      <div class="text-center mb-8">
        <router-link to="/" class="inline-flex items-center space-x-2 cursor-pointer">
          <div class="w-12 h-12 bg-primary-600 rounded-xl flex items-center justify-center">
            <span class="text-white font-bold text-2xl">S</span>
          </div>
          <span class="text-2xl font-bold text-gray-900">SalesBoost</span>
        </router-link>
        <p class="mt-2 text-gray-600">관리자 로그인</p>
      </div>

      <!-- Login Form -->
      <div class="bg-white rounded-2xl shadow-xl p-8">
        <!-- Error Alert -->
        <div
          v-if="error"
          class="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg flex items-start"
        >
          <AlertCircle class="w-5 h-5 text-red-500 mr-3 flex-shrink-0 mt-0.5" />
          <span class="text-red-700 text-sm">{{ error }}</span>
        </div>

        <form @submit.prevent="handleSubmit" class="space-y-6">
          <!-- Username -->
          <div>
            <label for="username" class="label-text">
              아이디
            </label>
            <div class="relative">
              <div class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                <User class="w-5 h-5 text-gray-400" />
              </div>
              <input
                id="username"
                v-model="form.username"
                type="text"
                autocomplete="username"
                class="input-field pl-12"
                placeholder="아이디를 입력하세요"
              />
            </div>
          </div>

          <!-- Password -->
          <div>
            <label for="password" class="label-text">
              비밀번호
            </label>
            <div class="relative">
              <div class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                <Lock class="w-5 h-5 text-gray-400" />
              </div>
              <input
                id="password"
                v-model="form.password"
                :type="showPassword ? 'text' : 'password'"
                autocomplete="current-password"
                class="input-field pl-12 pr-12"
                placeholder="비밀번호를 입력하세요"
              />
              <button
                type="button"
                @click="showPassword = !showPassword"
                class="absolute inset-y-0 right-0 pr-4 flex items-center cursor-pointer"
                aria-label="비밀번호 표시 토글"
              >
                <Eye v-if="!showPassword" class="w-5 h-5 text-gray-400 hover:text-gray-600" />
                <EyeOff v-else class="w-5 h-5 text-gray-400 hover:text-gray-600" />
              </button>
            </div>
          </div>

          <!-- Submit Button -->
          <button
            type="submit"
            class="btn-primary w-full py-4"
            :disabled="isLoading"
          >
            <Loader2 v-if="isLoading" class="w-5 h-5 mr-2 animate-spin" />
            {{ isLoading ? '로그인 중...' : '로그인' }}
          </button>
        </form>

      </div>

      <!-- Back to Home -->
      <div class="mt-6 text-center">
        <router-link to="/" class="text-sm text-gray-600 hover:text-primary-600 transition-colors cursor-pointer">
          ← 메인 페이지로 돌아가기
        </router-link>
      </div>
    </div>
  </div>
</template>
