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
  CheckCircle2,
  Zap,
  Shield,
  Users
} from 'lucide-vue-next'

const features = [
  {
    icon: FileText,
    title: 'PI 발행',
    description: 'Proforma Invoice 자동 생성 및 관리'
  },
  {
    icon: ClipboardCheck,
    title: 'PO 접수',
    description: 'Purchase Order 자동 접수 및 연동'
  },
  {
    icon: Factory,
    title: '생산/구매 지시',
    description: '주문 기반 생산 및 구매 지시 자동화'
  },
  {
    icon: Truck,
    title: '출하 지시',
    description: '출하 일정 관리 및 자동 지시 발행'
  },
  {
    icon: FileSpreadsheet,
    title: 'CI/PL',
    description: 'Commercial Invoice & Packing List 자동 생성'
  },
  {
    icon: BarChart3,
    title: '출하 현황',
    description: '실시간 출하 현황 모니터링 대시보드'
  },
  {
    icon: TrendingUp,
    title: '판매 현황',
    description: '거래처별/제품별 판매 분석 리포트'
  },
  {
    icon: Package,
    title: '인수인계 패키지',
    description: '담당자 변경 시 자동 인수인계 문서 생성'
  }
]

const benefits = [
  {
    icon: Zap,
    title: '업무 효율 40% 향상',
    description: '반복적인 문서 작업을 자동화하여 본연의 영업 활동에 집중'
  },
  {
    icon: Shield,
    title: '오류 및 누락 방지',
    description: '동일 정보의 반복 입력을 제거하여 실수 가능성 최소화'
  },
  {
    icon: Users,
    title: '원활한 인수인계',
    description: '담당자 변경 시에도 거래 히스토리 완벽 보존'
  }
]
</script>

<template>
  <div>
    <!-- Hero Section -->
    <section class="relative min-h-screen flex items-center pt-20 overflow-hidden">
      <!-- Background gradient -->
      <div class="absolute inset-0 bg-gradient-to-br from-primary-50 via-white to-secondary-50"></div>
      <div class="absolute inset-0 bg-[radial-gradient(ellipse_at_top_right,_var(--tw-gradient-stops))] from-primary-100/50 via-transparent to-transparent"></div>

      <!-- Grid pattern -->
      <div class="absolute inset-0 opacity-[0.03]" style="background-image: url('data:image/svg+xml,%3Csvg width=&quot;60&quot; height=&quot;60&quot; viewBox=&quot;0 0 60 60&quot; xmlns=&quot;http://www.w3.org/2000/svg&quot;%3E%3Cg fill=&quot;none&quot; fill-rule=&quot;evenodd&quot;%3E%3Cg fill=&quot;%23000000&quot; fill-opacity=&quot;1&quot;%3E%3Cpath d=&quot;M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z&quot;/%3E%3C/g%3E%3C/g%3E%3C/svg%3E')"></div>

      <div class="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-20">
        <div class="grid lg:grid-cols-2 gap-12 items-center">
          <!-- Left content -->
          <div>
            <div class="inline-flex items-center px-4 py-2 bg-primary-100 text-primary-700 rounded-full text-sm font-medium mb-6">
              <Zap class="w-4 h-4 mr-2" />
              해외 B2B 영업관리의 새로운 기준
            </div>
            <h1 class="text-4xl md:text-5xl lg:text-6xl font-bold text-gray-900 leading-tight mb-6">
              무역서류 자동화로<br />
              <span class="text-primary-600">영업 효율</span>을<br />
              극대화하세요
            </h1>
            <p class="text-lg md:text-xl text-gray-600 mb-8 max-w-lg">
              PI부터 CI/PL까지, 복잡한 무역서류를 한 번의 입력으로 자동 생성하고
              실시간으로 출하현황을 관리하세요.
            </p>
            <div class="flex flex-col sm:flex-row gap-4">
              <router-link to="/inquiry" class="btn-primary text-lg px-8 py-4">
                무료 상담 신청
                <ArrowRight class="w-5 h-5 ml-2" />
              </router-link>
              <router-link to="/service" class="btn-outline text-lg px-8 py-4">
                서비스 알아보기
              </router-link>
            </div>

            <!-- Stats -->
            <div class="flex flex-wrap gap-8 mt-12 pt-8 border-t border-gray-200">
              <div>
                <div class="text-3xl font-bold text-primary-600">40%</div>
                <div class="text-gray-600">업무 효율 향상</div>
              </div>
              <div>
                <div class="text-3xl font-bold text-primary-600">2,000+</div>
                <div class="text-gray-600">연간 절감 시간</div>
              </div>
              <div>
                <div class="text-3xl font-bold text-primary-600">100+</div>
                <div class="text-gray-600">기업 고객</div>
              </div>
            </div>
          </div>

          <!-- Right content - Hero Image/Illustration -->
          <div class="relative lg:pl-8">
            <div class="relative">
              <!-- Main card -->
              <div class="bg-white rounded-2xl shadow-2xl p-6 transform rotate-2">
                <div class="flex items-center justify-between mb-4">
                  <div class="flex items-center space-x-2">
                    <div class="w-3 h-3 rounded-full bg-red-400"></div>
                    <div class="w-3 h-3 rounded-full bg-yellow-400"></div>
                    <div class="w-3 h-3 rounded-full bg-green-400"></div>
                  </div>
                  <span class="text-sm text-gray-400">SalesBoost Dashboard</span>
                </div>
                <img
                  src="https://images.unsplash.com/photo-1551288049-bebda4e38f71?w=600&h=400&fit=crop"
                  alt="SalesBoost Dashboard"
                  class="rounded-lg w-full"
                />
              </div>

              <!-- Floating cards -->
              <div class="absolute -top-4 -left-4 bg-white rounded-xl shadow-lg p-4 transform -rotate-6">
                <div class="flex items-center space-x-3">
                  <div class="w-10 h-10 bg-green-100 rounded-lg flex items-center justify-center">
                    <CheckCircle2 class="w-6 h-6 text-green-600" />
                  </div>
                  <div>
                    <div class="text-sm font-medium text-gray-900">PI 자동 생성 완료</div>
                    <div class="text-xs text-gray-500">방금 전</div>
                  </div>
                </div>
              </div>

              <div class="absolute -bottom-4 -right-4 bg-white rounded-xl shadow-lg p-4 transform rotate-3">
                <div class="flex items-center space-x-3">
                  <div class="w-10 h-10 bg-primary-100 rounded-lg flex items-center justify-center">
                    <TrendingUp class="w-6 h-6 text-primary-600" />
                  </div>
                  <div>
                    <div class="text-sm font-medium text-gray-900">월간 매출</div>
                    <div class="text-lg font-bold text-primary-600">+23.5%</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Features Section -->
    <section class="py-20 bg-white">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="text-center mb-16">
          <h2 class="section-title">SalesBoost 8대 핵심 기능</h2>
          <p class="section-subtitle">
            PI 발행부터 인수인계까지, B2B 영업의 모든 프로세스를 하나의 플랫폼에서 관리하세요.
          </p>
        </div>

        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
          <div
            v-for="(feature, index) in features"
            :key="index"
            class="group p-6 bg-gray-50 rounded-2xl hover:bg-primary-50 transition-all duration-300 cursor-pointer"
          >
            <div class="w-14 h-14 bg-primary-100 rounded-xl flex items-center justify-center mb-4 group-hover:bg-primary-200 transition-colors">
              <component :is="feature.icon" class="w-7 h-7 text-primary-600" />
            </div>
            <h3 class="text-lg font-semibold text-gray-900 mb-2">{{ feature.title }}</h3>
            <p class="text-gray-600 text-sm">{{ feature.description }}</p>
          </div>
        </div>

        <div class="text-center mt-12">
          <router-link to="/service" class="btn-secondary">
            기능 자세히 보기
            <ArrowRight class="w-5 h-5 ml-2" />
          </router-link>
        </div>
      </div>
    </section>

    <!-- Benefits Section -->
    <section class="py-20 bg-gradient-to-br from-primary-600 to-primary-800 text-white">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="text-center mb-16">
          <h2 class="text-3xl md:text-4xl font-bold mb-4">왜 SalesBoost인가요?</h2>
          <p class="text-lg text-primary-100 max-w-2xl mx-auto">
            복잡한 해외 B2B 영업 업무를 단순화하고, 비즈니스 성장에 집중하세요.
          </p>
        </div>

        <div class="grid md:grid-cols-3 gap-8">
          <div
            v-for="(benefit, index) in benefits"
            :key="index"
            class="bg-white/10 backdrop-blur-sm rounded-2xl p-8 hover:bg-white/20 transition-colors cursor-pointer"
          >
            <div class="w-14 h-14 bg-white/20 rounded-xl flex items-center justify-center mb-6">
              <component :is="benefit.icon" class="w-7 h-7" />
            </div>
            <h3 class="text-xl font-semibold mb-3">{{ benefit.title }}</h3>
            <p class="text-primary-100">{{ benefit.description }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- CTA Section -->
    <section class="py-20 bg-gray-50">
      <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
        <h2 class="text-3xl md:text-4xl font-bold text-gray-900 mb-4">
          지금 바로 SalesBoost를 경험해보세요
        </h2>
        <p class="text-lg text-gray-600 mb-8 max-w-2xl mx-auto">
          무료 상담을 통해 귀사의 영업 프로세스에 맞는 최적의 솔루션을 제안받으세요.
        </p>
        <div class="flex flex-col sm:flex-row gap-4 justify-center">
          <router-link to="/inquiry" class="btn-primary text-lg px-8 py-4">
            무료 상담 신청
            <ArrowRight class="w-5 h-5 ml-2" />
          </router-link>
          <router-link to="/portfolio" class="btn-outline text-lg px-8 py-4">
            도입 사례 보기
          </router-link>
        </div>
      </div>
    </section>
  </div>
</template>
