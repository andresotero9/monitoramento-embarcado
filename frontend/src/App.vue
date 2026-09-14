<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AppSidebar from './components/AppSidebar.vue'
import AppTopbar from './components/AppTopbar.vue'
import Toast from 'primevue/toast'

const route = useRoute()
const ehPaginaPublica = computed(() => Boolean(route.meta.publica))
</script>

<template>
  <Toast />
  <router-view v-if="ehPaginaPublica" />
  <div v-else class="layout">
    <AppSidebar />
    <div class="layout__conteudo">
      <AppTopbar />
      <main class="layout__main">
        <router-view />
      </main>
    </div>
  </div>
</template>

<style>
.layout {
  display: flex;
  min-height: 100vh;
}

.layout__conteudo {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.layout__main {
  flex: 1;
  overflow-y: auto;
}
</style>
