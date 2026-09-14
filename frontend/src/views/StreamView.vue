<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import Hls from 'hls.js'
import Button from 'primevue/button'
import Message from 'primevue/message'
import StatusBadge from '../components/StatusBadge.vue'
import { buscarCamera } from '../services/camerasService'
import { buscarUltimoPorCamera } from '../services/monitoramentoService'
import { iniciarStream, pararStream, statusStream } from '../services/streamService'

const props = defineProps({
  id: { type: [String, Number], required: true }
})

const router = useRouter()
const camera = ref(null)
const ultimoMonitoramento = ref(null)
const videoRef = ref(null)
const mensagemErro = ref('')
const carregandoStream = ref(true)

let instanciaHls = null
let temporizadorPolling = null

async function carregarInfoCamera() {
  camera.value = await buscarCamera(props.id)
  try {
    ultimoMonitoramento.value = await buscarUltimoPorCamera(props.id)
  } catch {
    ultimoMonitoramento.value = null
  }
}

async function aguardarStreamPronto() {
  const resultado = await statusStream(props.id)
  if (resultado.status === 'PRONTO') {
    return true
  }
  return new Promise((resolve) => {
    temporizadorPolling = setTimeout(async () => {
      resolve(await aguardarStreamPronto())
    }, 1500)
  })
}

function anexarPlayer(playlistUrl) {
  if (Hls.isSupported()) {
    instanciaHls = new Hls()
    instanciaHls.loadSource(playlistUrl)
    instanciaHls.attachMedia(videoRef.value)
    instanciaHls.on(Hls.Events.MANIFEST_PARSED, () => videoRef.value.play())
  } else if (videoRef.value.canPlayType('application/vnd.apple.mpegurl')) {
    // Suporte nativo (Safari)
    videoRef.value.src = playlistUrl
    videoRef.value.addEventListener('loadedmetadata', () => videoRef.value.play())
  } else {
    mensagemErro.value = 'Este navegador não suporta reprodução de vídeo HLS.'
  }
}

async function iniciar() {
  carregandoStream.value = true
  mensagemErro.value = ''
  try {
    const { playlistUrl } = await iniciarStream(props.id)
    await aguardarStreamPronto()
    anexarPlayer(playlistUrl)
  } catch (erro) {
    mensagemErro.value = erro.response?.data?.message || 'Não foi possível iniciar o stream desta câmera.'
  } finally {
    carregandoStream.value = false
  }
}

onMounted(async () => {
  await carregarInfoCamera()
  await iniciar()
})

onBeforeUnmount(() => {
  clearTimeout(temporizadorPolling)
  instanciaHls?.destroy()
  pararStream(props.id).catch(() => {})
})
</script>

<template>
  <div class="pagina">
    <div class="cabecalho-pagina">
      <div>
        <h1>{{ camera?.nome || 'Câmera' }}</h1>
        <div class="rotulo-secundario dado-numerico">{{ camera?.enderecoIp }}</div>
      </div>
      <Button label="Voltar" icon="pi pi-arrow-left" severity="secondary" text @click="router.push({ name: 'cameras' })" />
    </div>

    <div class="superficie">
      <div style="margin-bottom: var(--espaco-4); display: flex; align-items: center; gap: var(--espaco-3)">
        <StatusBadge v-if="ultimoMonitoramento" :status="ultimoMonitoramento.status" />
        <span v-else class="rotulo-secundario">Ainda sem verificação registrada</span>
      </div>

      <Message v-if="mensagemErro" severity="warn" :closable="false">{{ mensagemErro }}</Message>

      <div class="video-container">
        <video ref="videoRef" controls muted class="video-container__video"></video>
        <div v-if="carregandoStream" class="video-container__carregando">
          <i class="pi pi-spin pi-spinner"></i>
          Conectando ao stream…
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.video-container {
  position: relative;
  background: #000;
  border-radius: var(--raio-borda);
  overflow: hidden;
  aspect-ratio: 16 / 9;
  max-width: 960px;
}

.video-container__video {
  width: 100%;
  height: 100%;
}

.video-container__carregando {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--espaco-2);
  color: var(--cor-texto-secundario);
  font-size: 13px;
  background: rgba(15, 20, 23, 0.6);
}
</style>
