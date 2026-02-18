import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

// Public views
import HomeView from '@/views/HomeView.vue'
import ServiceView from '@/views/ServiceView.vue'
import PortfolioView from '@/views/PortfolioView.vue'
import InquiryView from '@/views/InquiryView.vue'

// Admin views
import AdminLoginView from '@/views/admin/AdminLoginView.vue'
import AdminInquiriesView from '@/views/admin/AdminInquiriesView.vue'
import AdminPortfoliosView from '@/views/admin/AdminPortfoliosView.vue'

const routes = [
  // Public routes
  {
    path: '/',
    name: 'home',
    component: HomeView,
    meta: { title: 'SalesBoost - B2B 영업관리 솔루션' }
  },
  {
    path: '/service',
    name: 'service',
    component: ServiceView,
    meta: { title: '서비스 소개 - SalesBoost' }
  },
  {
    path: '/portfolio',
    name: 'portfolio',
    component: PortfolioView,
    meta: { title: '포트폴리오 - SalesBoost' }
  },
  {
    path: '/inquiry',
    name: 'inquiry',
    component: InquiryView,
    meta: { title: '제휴 문의 - SalesBoost' }
  },

  // Admin routes
  {
    path: '/admin/login',
    name: 'admin-login',
    component: AdminLoginView,
    meta: { title: '관리자 로그인 - SalesBoost' }
  },
  {
    path: '/admin/inquiries',
    name: 'admin-inquiries',
    component: AdminInquiriesView,
    meta: { title: '제휴문의 관리 - SalesBoost', requiresAuth: true }
  },
  {
    path: '/admin/portfolios',
    name: 'admin-portfolios',
    component: AdminPortfoliosView,
    meta: { title: '포트폴리오 관리 - SalesBoost', requiresAuth: true }
  },

  // Catch-all redirect
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    }
    if (to.hash) {
      return { el: to.hash, behavior: 'smooth' }
    }
    return { top: 0, behavior: 'smooth' }
  }
})

// Navigation guard for auth
router.beforeEach((to, from, next) => {
  // Update page title
  document.title = to.meta.title || 'SalesBoost'

  // Check auth for protected routes
  if (to.meta.requiresAuth) {
    const authStore = useAuthStore()
    if (!authStore.isAuthenticated) {
      next({ name: 'admin-login', query: { redirect: to.fullPath } })
      return
    }
  }

  next()
})

export default router
