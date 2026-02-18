<script setup>
import { Mail, Phone, MapPin, Github, Linkedin } from 'lucide-vue-next'

const teamMembers = [
  { name: '박찬진', role: 'Backend / 팀장' },
  { name: '강성훈', role: 'Backend' },
  { name: '정진호', role: 'Frontend' },
  { name: '최현지', role: 'Infra/Full' }
]

const quickLinks = [
  { name: '서비스 소개', path: '/service' },
  { name: '포트폴리오', path: '/portfolio' },
  { name: '제휴 문의', path: '/inquiry' }
]
</script>

<template>
  <footer class="bg-gray-900 text-gray-300">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
        <!-- Company Info -->
        <div class="lg:col-span-2">
          <div class="flex items-center space-x-2 mb-4">
            <div class="w-10 h-10 bg-primary-600 rounded-xl flex items-center justify-center">
              <span class="text-white font-bold text-xl">S</span>
            </div>
            <span class="text-xl font-bold text-white">SalesBoost</span>
          </div>
          <p class="text-gray-400 mb-6 max-w-md">
            해외 B2B 영업관리의 새로운 기준. SalesBoost는 무역서류 자동화와 인수인계 패키지를 통해
            영업 효율을 극대화하는 솔루션입니다.
          </p>
          <div class="space-y-2">
            <div class="flex items-center space-x-3">
              <Mail class="w-5 h-5 text-primary-400" />
              <span>contact@salesboost.kr</span>
            </div>
            <div class="flex items-center space-x-3">
              <Phone class="w-5 h-5 text-primary-400" />
              <span>02-1234-5678</span>
            </div>
            <div class="flex items-center space-x-3">
              <MapPin class="w-5 h-5 text-primary-400" />
              <span>서울특별시 강남구 테헤란로 123</span>
            </div>
          </div>
        </div>

        <!-- Quick Links -->
        <div>
          <h3 class="text-white font-semibold mb-4">바로가기</h3>
          <ul class="space-y-2">
            <li v-for="link in quickLinks" :key="link.path">
              <router-link
                :to="link.path"
                class="hover:text-primary-400 transition-colors cursor-pointer"
              >
                {{ link.name }}
              </router-link>
            </li>
            <li>
              <router-link
                to="/admin/login"
                class="hover:text-primary-400 transition-colors cursor-pointer"
              >
                관리자
              </router-link>
            </li>
          </ul>
        </div>

        <!-- Team -->
        <div>
          <h3 class="text-white font-semibold mb-4">팀원 소개</h3>
          <ul class="space-y-2">
            <li
              v-for="member in teamMembers"
              :key="member.name"
              class="text-sm"
            >
              <span class="text-white">{{ member.name }}</span>
              <span class="text-gray-500"> · {{ member.role }}</span>
            </li>
          </ul>
        </div>
      </div>

      <!-- Bottom Bar -->
      <div class="border-t border-gray-800 mt-10 pt-8 flex flex-col md:flex-row justify-between items-center">
        <p class="text-gray-500 text-sm">
          &copy; 2025 SalesBoost. All rights reserved.
        </p>
        <div class="flex items-center space-x-4 mt-4 md:mt-0">
          <a
            href="https://github.com"
            target="_blank"
            rel="noopener noreferrer"
            class="p-2 hover:bg-gray-800 rounded-lg transition-colors cursor-pointer"
            aria-label="GitHub"
          >
            <Github class="w-5 h-5" />
          </a>
          <a
            href="https://linkedin.com"
            target="_blank"
            rel="noopener noreferrer"
            class="p-2 hover:bg-gray-800 rounded-lg transition-colors cursor-pointer"
            aria-label="LinkedIn"
          >
            <Linkedin class="w-5 h-5" />
          </a>
        </div>
      </div>
    </div>
  </footer>
</template>
