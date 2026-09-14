<script setup>
import { useRouter } from 'vue-router'
import { authStore } from '../stores/auth'
import Button from 'primevue/button'

const router = useRouter()

function sair() {
  authStore.logout()
  router.push('/login')
}
</script>

<template>
  <header class="topbar">
    <div></div>
    <div class="topbar__usuario">
      <i class="pi pi-user"></i>
      <span>{{ authStore.username }}</span>
      <Button
        icon="pi pi-sign-out"
        text
        severity="secondary"
        aria-label="Sair"
        @click="sair"
      />
    </div>
  </header>
</template>

<style scoped>
.topbar {
  height: 56px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--espaco-6);
  border-bottom: 1px solid var(--cor-borda);
  background: var(--cor-fundo);
}

.topbar__usuario {
  display: flex;
  align-items: center;
  gap: var(--espaco-2);
  font-size: 13px;
  color: var(--cor-texto-secundario);
}
</style>
