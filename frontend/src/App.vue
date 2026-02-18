<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import TheHeader from '@/components/layout/TheHeader.vue'
import TheFooter from '@/components/layout/TheFooter.vue'

const route = useRoute()

const isAdminPage = computed(() => route.path.startsWith('/admin'))
</script>

<template>
  <div class="min-h-screen flex flex-col bg-white">
    <TheHeader v-if="!isAdminPage" />
    <main class="flex-1">
      <router-view />
    </main>
    <TheFooter v-if="!isAdminPage" />
  </div>
</template>
