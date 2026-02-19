<script setup>
import { ref, reactive } from 'vue'
import { useInquiryStore } from '@/stores/inquiry'
import {
  Send,
  CheckCircle,
  AlertCircle,
  Building2,
  User,
  Mail,
  Phone,
  MessageSquare,
  Loader2
} from 'lucide-vue-next'

const inquiryStore = useInquiryStore()

const form = reactive({
  companyName: '',
  contactName: '',
  email: '',
  phone: '',
  inquiryType: '',
  content: ''
})

const errors = reactive({
  companyName: '',
  contactName: '',
  email: '',
  phone: '',
  inquiryType: '',
  content: ''
})

const isSubmitting = ref(false)
const isSubmitted = ref(false)
const submitError = ref('')

const inquiryTypes = [
  { value: 'GENERAL', label: '일반 문의' },
  { value: 'PARTNERSHIP', label: '제휴 문의' },
  { value: 'QUOTE', label: '견적 문의' },
  { value: 'ETC', label: '기타 문의' }
]

const validateForm = () => {
  let isValid = true

  // Reset errors
  Object.keys(errors).forEach(key => errors[key] = '')

  // Company name
  if (!form.companyName.trim()) {
    errors.companyName = '기업명을 입력해주세요.'
    isValid = false
  }

  // Contact name
  if (!form.contactName.trim()) {
    errors.contactName = '담당자명을 입력해주세요.'
    isValid = false
  }

  // Email
  if (!form.email.trim()) {
    errors.email = '이메일을 입력해주세요.'
    isValid = false
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
    errors.email = '올바른 이메일 형식을 입력해주세요.'
    isValid = false
  }

  // Phone
  if (!form.phone.trim()) {
    errors.phone = '연락처를 입력해주세요.'
    isValid = false
  }

  // Inquiry type
  if (!form.inquiryType) {
    errors.inquiryType = '문의 유형을 선택해주세요.'
    isValid = false
  }

  // Content
  if (!form.content.trim()) {
    errors.content = '문의 내용을 입력해주세요.'
    isValid = false
  } else if (form.content.trim().length < 10) {
    errors.content = '문의 내용을 10자 이상 입력해주세요.'
    isValid = false
  }

  return isValid
}

const handleSubmit = async () => {
  if (!validateForm()) return

  isSubmitting.value = true
  submitError.value = ''

  const result = await inquiryStore.submitInquiry({
    companyName: form.companyName,
    contactName: form.contactName,
    email: form.email,
    phone: form.phone,
    inquiryType: form.inquiryType,
    content: form.content
  })

  isSubmitting.value = false

  if (result.success) {
    isSubmitted.value = true
  } else {
    submitError.value = result.message || '문의 등록에 실패했습니다. 다시 시도해주세요.'
  }
}

const resetForm = () => {
  Object.keys(form).forEach(key => form[key] = '')
  Object.keys(errors).forEach(key => errors[key] = '')
  isSubmitted.value = false
  submitError.value = ''
}
</script>

<template>
  <div class="pt-20">
    <!-- Hero Section -->
    <section class="py-16 bg-gradient-to-br from-primary-50 to-white">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="text-center max-w-3xl mx-auto">
          <h1 class="text-4xl md:text-5xl font-bold text-gray-900 mb-6">
            제휴 문의
          </h1>
          <p class="text-lg md:text-xl text-gray-600">
            SalesBoost 도입에 관심이 있으신가요?<br />
            아래 양식을 작성해주시면 담당자가 빠르게 연락드리겠습니다.
          </p>
        </div>
      </div>
    </section>

    <!-- Form Section -->
    <section class="py-16 bg-white">
      <div class="max-w-2xl mx-auto px-4 sm:px-6 lg:px-8">
        <!-- Success State -->
        <Transition
          enter-active-class="transition duration-300 ease-out"
          enter-from-class="opacity-0 scale-95"
          enter-to-class="opacity-100 scale-100"
        >
          <div
            v-if="isSubmitted"
            class="text-center py-12"
          >
            <div class="w-20 h-20 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-6">
              <CheckCircle class="w-10 h-10 text-green-600" />
            </div>
            <h2 class="text-2xl font-bold text-gray-900 mb-4">
              문의가 접수되었습니다
            </h2>
            <p class="text-gray-600 mb-8">
              담당자가 확인 후 빠른 시일 내에 연락드리겠습니다.<br />
              감사합니다.
            </p>
            <button
              @click="resetForm"
              class="btn-primary"
            >
              새 문의 작성
            </button>
          </div>
        </Transition>

        <!-- Form -->
        <form
          v-if="!isSubmitted"
          @submit.prevent="handleSubmit"
          class="bg-white rounded-2xl shadow-lg p-8"
        >
          <!-- Error Alert -->
          <div
            v-if="submitError"
            class="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg flex items-start"
          >
            <AlertCircle class="w-5 h-5 text-red-500 mr-3 flex-shrink-0 mt-0.5" />
            <span class="text-red-700">{{ submitError }}</span>
          </div>

          <div class="space-y-6">
            <!-- Company Name -->
            <div>
              <label for="companyName" class="label-text">
                <Building2 class="w-4 h-4 inline mr-1" />
                기업명 <span class="text-red-500">*</span>
              </label>
              <input
                id="companyName"
                v-model="form.companyName"
                type="text"
                class="input-field"
                :class="{ 'border-red-500 focus:ring-red-500 focus:border-red-500': errors.companyName }"
                placeholder="(주)회사명"
              />
              <p v-if="errors.companyName" class="mt-1 text-sm text-red-500">
                {{ errors.companyName }}
              </p>
            </div>

            <!-- Contact Name -->
            <div>
              <label for="contactName" class="label-text">
                <User class="w-4 h-4 inline mr-1" />
                담당자명 <span class="text-red-500">*</span>
              </label>
              <input
                id="contactName"
                v-model="form.contactName"
                type="text"
                class="input-field"
                :class="{ 'border-red-500 focus:ring-red-500 focus:border-red-500': errors.contactName }"
                placeholder="홍길동"
              />
              <p v-if="errors.contactName" class="mt-1 text-sm text-red-500">
                {{ errors.contactName }}
              </p>
            </div>

            <!-- Email & Phone -->
            <div class="grid md:grid-cols-2 gap-6">
              <div>
                <label for="email" class="label-text">
                  <Mail class="w-4 h-4 inline mr-1" />
                  이메일 <span class="text-red-500">*</span>
                </label>
                <input
                  id="email"
                  v-model="form.email"
                  type="email"
                  class="input-field"
                  :class="{ 'border-red-500 focus:ring-red-500 focus:border-red-500': errors.email }"
                  placeholder="example@company.com"
                />
                <p v-if="errors.email" class="mt-1 text-sm text-red-500">
                  {{ errors.email }}
                </p>
              </div>

              <div>
                <label for="phone" class="label-text">
                  <Phone class="w-4 h-4 inline mr-1" />
                  연락처 <span class="text-red-500">*</span>
                </label>
                <input
                  id="phone"
                  v-model="form.phone"
                  type="tel"
                  class="input-field"
                  :class="{ 'border-red-500 focus:ring-red-500 focus:border-red-500': errors.phone }"
                  placeholder="010-0000-0000"
                />
                <p v-if="errors.phone" class="mt-1 text-sm text-red-500">
                  {{ errors.phone }}
                </p>
              </div>
            </div>

            <!-- Inquiry Type -->
            <div>
              <label for="inquiryType" class="label-text">
                문의 유형 <span class="text-red-500">*</span>
              </label>
              <select
                id="inquiryType"
                v-model="form.inquiryType"
                class="input-field"
                :class="{ 'border-red-500 focus:ring-red-500 focus:border-red-500': errors.inquiryType }"
              >
                <option value="" disabled>문의 유형을 선택해주세요</option>
                <option
                  v-for="type in inquiryTypes"
                  :key="type.value"
                  :value="type.value"
                >
                  {{ type.label }}
                </option>
              </select>
              <p v-if="errors.inquiryType" class="mt-1 text-sm text-red-500">
                {{ errors.inquiryType }}
              </p>
            </div>

            <!-- Content -->
            <div>
              <label for="content" class="label-text">
                <MessageSquare class="w-4 h-4 inline mr-1" />
                문의 내용 <span class="text-red-500">*</span>
              </label>
              <textarea
                id="content"
                v-model="form.content"
                rows="5"
                class="input-field resize-none"
                :class="{ 'border-red-500 focus:ring-red-500 focus:border-red-500': errors.content }"
                placeholder="문의하실 내용을 자세히 작성해주세요."
              ></textarea>
              <p v-if="errors.content" class="mt-1 text-sm text-red-500">
                {{ errors.content }}
              </p>
            </div>

            <!-- Submit Button -->
            <button
              type="submit"
              class="btn-primary w-full py-4"
              :disabled="isSubmitting"
            >
              <Loader2 v-if="isSubmitting" class="w-5 h-5 mr-2 animate-spin" />
              <Send v-else class="w-5 h-5 mr-2" />
              {{ isSubmitting ? '제출 중...' : '문의 제출' }}
            </button>
          </div>
        </form>

        <!-- Contact Info -->
        <div class="mt-12 text-center">
          <p class="text-gray-600 mb-4">
            급한 문의는 아래 연락처로 직접 연락주세요.
          </p>
          <div class="flex flex-col sm:flex-row items-center justify-center gap-4 text-gray-700">
            <a href="tel:02-1234-5678" class="flex items-center hover:text-primary-600 transition-colors cursor-pointer">
              <Phone class="w-5 h-5 mr-2" />
              02-1234-5678
            </a>
            <span class="hidden sm:inline text-gray-300">|</span>
            <a href="mailto:contact@salesboost.kr" class="flex items-center hover:text-primary-600 transition-colors cursor-pointer">
              <Mail class="w-5 h-5 mr-2" />
              contact@salesboost.kr
            </a>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>
