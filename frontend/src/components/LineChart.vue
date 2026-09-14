<script setup>
import { onMounted, onBeforeUnmount, ref, watch } from 'vue'
import Chart from 'chart.js/auto'

const props = defineProps({
  labels: { type: Array, required: true },
  valores: { type: Array, required: true },
  rotulo: { type: String, required: true },
  cor: { type: String, default: '#3FA9F5' }
})

const canvasRef = ref(null)
let instanciaChart = null

function montarGrafico() {
  if (!canvasRef.value) return
  instanciaChart = new Chart(canvasRef.value, {
    type: 'line',
    data: {
      labels: props.labels,
      datasets: [{
        label: props.rotulo,
        data: props.valores,
        borderColor: props.cor,
        backgroundColor: props.cor + '22',
        tension: 0.25,
        fill: true,
        pointRadius: 2
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: { legend: { display: false } },
      scales: {
        x: { ticks: { color: '#8B98A0' }, grid: { color: '#262E33' } },
        y: { ticks: { color: '#8B98A0' }, grid: { color: '#262E33' } }
      }
    }
  })
}

onMounted(montarGrafico)
onBeforeUnmount(() => instanciaChart?.destroy())

watch(() => [props.labels, props.valores], () => {
  instanciaChart?.destroy()
  montarGrafico()
})
</script>

<template>
  <div class="grafico-container">
    <canvas ref="canvasRef"></canvas>
  </div>
</template>

<style scoped>
.grafico-container {
  height: 200px;
}
</style>
