<script setup>
import { computed } from 'vue'

const props = defineProps({
  status: { type: String, required: true }
})

const MAPA_STATUS = {
  ONLINE: { cor: 'var(--cor-status-ok)', rotulo: 'Online' },
  OFFLINE: { cor: 'var(--cor-status-critico)', rotulo: 'Offline' },
  INATIVA: { cor: 'var(--cor-status-neutro)', rotulo: 'Inativa' },
  INFO: { cor: 'var(--cor-acento)', rotulo: 'Info' },
  WARNING: { cor: 'var(--cor-status-warning)', rotulo: 'Atenção' },
  CRITICAL: { cor: 'var(--cor-status-critico)', rotulo: 'Crítico' }
}

const info = computed(() => MAPA_STATUS[props.status] ?? { cor: 'var(--cor-status-neutro)', rotulo: props.status })
</script>

<template>
  <span class="status-pill" :style="{ color: info.cor, borderColor: info.cor + '55' }">
    <span class="status-pill__ponto" :style="{ background: info.cor }"></span>
    {{ info.rotulo }}
  </span>
</template>
