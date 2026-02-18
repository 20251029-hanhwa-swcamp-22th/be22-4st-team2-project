<script setup>
import {
  FileText,
  ClipboardCheck,
  Factory,
  Truck,
  FileSpreadsheet,
  BarChart3,
  TrendingUp,
  Package,
  ArrowRight,
  CheckCircle,
  ChevronRight
} from 'lucide-vue-next'

const features = [
  {
    id: 1,
    icon: FileText,
    title: 'PI 발행 (Proforma Invoice)',
    subtitle: '견적서 자동 생성',
    description: '제품 정보와 거래 조건을 입력하면 Proforma Invoice가 자동으로 생성됩니다. 기존 거래 내역을 기반으로 빠르게 견적서를 작성하고 고객에게 전송할 수 있습니다.',
    benefits: [
      '거래처/제품 정보 자동 불러오기',
      '다국어(한/영) 문서 지원',
      'PDF/Excel 다운로드 및 이메일 발송',
      '견적 이력 관리 및 버전 추적'
    ],
    color: 'primary'
  },
  {
    id: 2,
    icon: ClipboardCheck,
    title: 'PO 접수 (Purchase Order)',
    subtitle: '주문서 자동 연동',
    description: 'PI 발행 후 고객의 Purchase Order를 시스템에 등록하면 PI와 자동으로 연동됩니다. 수량, 단가, 납기 등의 변경사항을 쉽게 관리할 수 있습니다.',
    benefits: [
      'PI 데이터 자동 불러오기',
      '변경사항 비교 및 알림',
      '승인 워크플로우 지원',
      '주문 현황 대시보드'
    ],
    color: 'secondary'
  },
  {
    id: 3,
    icon: Factory,
    title: '생산/구매 지시',
    subtitle: '주문 기반 자동 지시',
    description: 'PO가 확정되면 생산 또는 구매 지시서가 자동으로 생성됩니다. 재고 현황을 파악하여 생산 계획을 수립하고 구매 요청을 발행할 수 있습니다.',
    benefits: [
      '재고 현황 자동 체크',
      '생산 리드타임 관리',
      '구매 요청 자동 생성',
      '공정별 진행 현황 추적'
    ],
    color: 'primary'
  },
  {
    id: 4,
    icon: Truck,
    title: '출하 지시',
    subtitle: '출하 일정 관리',
    description: '생산이 완료되면 출하 지시서를 발행합니다. 운송 스케줄, 포장 정보, 배송지 등을 관리하고 물류 파트너와 정보를 공유할 수 있습니다.',
    benefits: [
      '납기 기반 출하 스케줄링',
      '포장 사양 자동 계산',
      '운송사 정보 연동',
      '출하 라벨 자동 생성'
    ],
    color: 'secondary'
  },
  {
    id: 5,
    icon: FileSpreadsheet,
    title: 'CI/PL (Commercial Invoice & Packing List)',
    subtitle: '무역서류 자동 생성',
    description: '출하 정보를 기반으로 Commercial Invoice와 Packing List가 자동으로 생성됩니다. HS Code, 원산지, 무게 등의 무역 필수 정보가 자동 입력됩니다.',
    benefits: [
      'PI/PO 데이터 자동 반영',
      'HS Code 자동 매핑',
      '수출입 규정 준수 검증',
      '관세사 연동 지원'
    ],
    color: 'primary'
  },
  {
    id: 6,
    icon: BarChart3,
    title: '출하 현황',
    subtitle: '실시간 모니터링',
    description: '모든 출하 건의 진행 현황을 실시간으로 확인할 수 있습니다. 대시보드를 통해 지연 건, 완료 건, 진행 중인 건을 한눈에 파악할 수 있습니다.',
    benefits: [
      '거래처별 출하 현황',
      '납기 준수율 분석',
      '지연 건 알림',
      '커스텀 리포트 생성'
    ],
    color: 'secondary'
  },
  {
    id: 7,
    icon: TrendingUp,
    title: '판매 현황',
    subtitle: '매출 분석 리포트',
    description: '거래처별, 제품별, 기간별 판매 현황을 분석합니다. 매출 추이, 수익성 분석, 거래처 기여도 등 다양한 관점의 리포트를 제공합니다.',
    benefits: [
      '실시간 매출 대시보드',
      '거래처 수익성 분석',
      '제품별 판매 트렌드',
      '엑셀/PDF 리포트 출력'
    ],
    color: 'primary'
  },
  {
    id: 8,
    icon: Package,
    title: '인수인계 패키지',
    subtitle: '업무 연속성 보장',
    description: '담당자 변경 시 거래 이력, 진행 중인 건, 주요 이슈 등을 자동으로 문서화합니다. 신규 담당자가 빠르게 업무에 적응할 수 있도록 지원합니다.',
    benefits: [
      '거래처별 히스토리 요약',
      '미결 건 자동 리스트업',
      '주요 연락처/이슈 정리',
      '템플릿 기반 인수인계서'
    ],
    color: 'secondary'
  }
]

const processSteps = [
  { id: 1, name: 'PI 발행', icon: FileText },
  { id: 2, name: 'PO 접수', icon: ClipboardCheck },
  { id: 3, name: '생산/구매 지시', icon: Factory },
  { id: 4, name: '출하 지시', icon: Truck },
  { id: 5, name: 'CI/PL', icon: FileSpreadsheet },
  { id: 6, name: '출하 현황', icon: BarChart3 },
  { id: 7, name: '판매 현황', icon: TrendingUp }
]
</script>

<template>
  <div class="pt-20">
    <!-- Hero Section -->
    <section class="py-16 bg-gradient-to-br from-primary-50 to-white">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="text-center max-w-3xl mx-auto">
          <h1 class="text-4xl md:text-5xl font-bold text-gray-900 mb-6">
            서비스 소개
          </h1>
          <p class="text-lg md:text-xl text-gray-600">
            SalesBoost는 해외 B2B 영업의 전체 프로세스를 하나의 플랫폼에서 관리할 수 있는
            올인원 영업관리 솔루션입니다.
          </p>
        </div>
      </div>
    </section>

    <!-- Process Flow Section -->
    <section class="py-16 bg-white">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="text-center mb-12">
          <h2 class="section-title">업무 프로세스 흐름도</h2>
          <p class="section-subtitle">
            PI 발행부터 판매 현황까지, 영업 업무의 전체 흐름을 자동화합니다.
          </p>
        </div>

        <!-- Desktop Process Flow -->
        <div class="hidden lg:flex items-center justify-center">
          <div
            v-for="(step, index) in processSteps"
            :key="step.id"
            class="flex items-center"
          >
            <div class="flex flex-col items-center">
              <div class="w-16 h-16 bg-primary-100 rounded-2xl flex items-center justify-center mb-3 hover:bg-primary-200 transition-colors cursor-pointer">
                <component :is="step.icon" class="w-8 h-8 text-primary-600" />
              </div>
              <span class="text-sm font-medium text-gray-700 text-center whitespace-nowrap">
                {{ step.name }}
              </span>
            </div>
            <ChevronRight
              v-if="index < processSteps.length - 1"
              class="w-6 h-6 text-gray-300 mx-4"
            />
          </div>
        </div>

        <!-- Mobile Process Flow -->
        <div class="lg:hidden">
          <div class="relative">
            <!-- Vertical line -->
            <div class="absolute left-8 top-0 bottom-0 w-0.5 bg-primary-100"></div>

            <div
              v-for="(step, index) in processSteps"
              :key="step.id"
              class="relative flex items-center mb-6 last:mb-0"
            >
              <div class="relative z-10 w-16 h-16 bg-primary-100 rounded-2xl flex items-center justify-center flex-shrink-0">
                <component :is="step.icon" class="w-8 h-8 text-primary-600" />
              </div>
              <div class="ml-4">
                <span class="text-sm font-medium text-gray-700">
                  {{ index + 1 }}. {{ step.name }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Features Detail Section -->
    <section class="py-16 bg-gray-50">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="text-center mb-16">
          <h2 class="section-title">8대 핵심 기능 상세</h2>
          <p class="section-subtitle">
            각 기능의 상세 내용과 제공되는 혜택을 확인하세요.
          </p>
        </div>

        <div class="space-y-12">
          <div
            v-for="(feature, index) in features"
            :key="feature.id"
            class="bg-white rounded-2xl shadow-lg overflow-hidden"
          >
            <div
              class="grid lg:grid-cols-2 gap-0"
              :class="{ 'lg:flex-row-reverse': index % 2 === 1 }"
            >
              <!-- Content -->
              <div
                class="p-8 lg:p-12"
                :class="{ 'lg:order-2': index % 2 === 1 }"
              >
                <div class="flex items-center mb-4">
                  <div
                    class="w-12 h-12 rounded-xl flex items-center justify-center mr-4"
                    :class="feature.color === 'primary' ? 'bg-primary-100' : 'bg-secondary-100'"
                  >
                    <component
                      :is="feature.icon"
                      class="w-6 h-6"
                      :class="feature.color === 'primary' ? 'text-primary-600' : 'text-secondary-600'"
                    />
                  </div>
                  <div>
                    <h3 class="text-xl font-bold text-gray-900">{{ feature.title }}</h3>
                    <p class="text-sm text-gray-500">{{ feature.subtitle }}</p>
                  </div>
                </div>

                <p class="text-gray-600 mb-6">
                  {{ feature.description }}
                </p>

                <div class="space-y-3">
                  <div
                    v-for="benefit in feature.benefits"
                    :key="benefit"
                    class="flex items-start"
                  >
                    <CheckCircle
                      class="w-5 h-5 mr-3 flex-shrink-0 mt-0.5"
                      :class="feature.color === 'primary' ? 'text-primary-500' : 'text-secondary-500'"
                    />
                    <span class="text-gray-700">{{ benefit }}</span>
                  </div>
                </div>
              </div>

              <!-- Image -->
              <div
                class="relative h-64 lg:h-auto"
                :class="[
                  feature.color === 'primary' ? 'bg-primary-600' : 'bg-secondary-600',
                  { 'lg:order-1': index % 2 === 1 }
                ]"
              >
                <div class="absolute inset-0 flex items-center justify-center">
                  <component
                    :is="feature.icon"
                    class="w-32 h-32 text-white/20"
                  />
                </div>
                <div class="absolute inset-0 flex items-center justify-center">
                  <div class="text-center text-white">
                    <div class="text-6xl font-bold mb-2">{{ String(feature.id).padStart(2, '0') }}</div>
                    <div class="text-lg font-medium text-white/80">{{ feature.subtitle }}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- CTA Section -->
    <section class="py-16 bg-primary-600">
      <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
        <h2 class="text-3xl md:text-4xl font-bold text-white mb-4">
          SalesBoost로 영업 효율을 높이세요
        </h2>
        <p class="text-lg text-primary-100 mb-8">
          무료 상담을 통해 귀사에 맞는 솔루션을 제안받으세요.
        </p>
        <router-link to="/inquiry" class="inline-flex items-center px-8 py-4 bg-white text-primary-600 font-semibold rounded-lg hover:bg-primary-50 transition-colors cursor-pointer">
          무료 상담 신청
          <ArrowRight class="w-5 h-5 ml-2" />
        </router-link>
      </div>
    </section>
  </div>
</template>
