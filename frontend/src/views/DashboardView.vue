<script setup>
import { ref, onMounted } from 'vue'
import StatusBadge from '../components/StatusBadge.vue'
import LineChart from '../components/LineChart.vue'
import {
  buscarUltimoInternet, listarHistoricoInternet,
  buscarUltimoDisco, listarHistoricoDisco,
  buscarUltimoPorCamera
} from '../services/monitoramentoService'
import { listarCameras } from '../services/camerasService'
import { listarAlertasRecentes } from '../services/alertasService'

const internet = ref(null)
const disco = ref(null)
const alertasRecentes = ref([])
const resumoCameras = ref({ total: 0, online: 0, offline: 0, inativa: 0 })
const carregando = ref(true)

const graficoInternetLabels = ref([])
const graficoInternetValores = ref([])
const graficoDiscoLabels = ref([])
const graficoDiscoValores = ref([])

function formatarHora(dataHoraIso) {
  if (!dataHoraIso) return '—'
  return new Date(dataHoraIso).toLocaleString('pt-BR')
}

async function carregarResumoCameras() {
  const cameras = await listarCameras()
  const resumo = { total: cameras.length, online: 0, offline: 0, inativa: 0 }

  await Promise.all(cameras.map(async (camera) => {
    if (!camera.ativa) {
      resumo.inativa += 1
      return
    }
    try {
      const ultimo = await buscarUltimoPorCamera(camera.id)
      if (ultimo.status === 'ONLINE') resumo.online += 1
      else resumo.offline += 1
    } catch {
      resumo.offline += 1 // Ainda sem verificação registrada: tratada como offline por precaução.
    }
  }))

  resumoCameras.value = resumo
}

async function carregarDados() {
  carregando.value = true
  try {
    const [internetLatest, discoLatest, alertas, historicoInternet, historicoDisco] = await Promise.all([
      buscarUltimoInternet().catch(() => null),
      buscarUltimoDisco().catch(() => null),
      listarAlertasRecentes(),
      listarHistoricoInternet(0, 15).catch(() => ({ content: [] })),
      listarHistoricoDisco(0, 15).catch(() => ({ content: [] })),
      carregarResumoCameras()
    ])

    internet.value = internetLatest
    disco.value = discoLatest
    alertasRecentes.value = alertas

    const internetOrdenado = [...historicoInternet.content].reverse()
    graficoInternetLabels.value = internetOrdenado.map((m) => new Date(m.dataHora).toLocaleTimeString('pt-BR'))
    graficoInternetValores.value = internetOrdenado.map((m) => m.tempoResposta ?? 0)

    const discoOrdenado = [...historicoDisco.content].reverse()
    graficoDiscoLabels.value = discoOrdenado.map((m) => new Date(m.dataHora).toLocaleTimeString('pt-BR'))
    graficoDiscoValores.value = discoOrdenado.map((m) => m.percentualUtilizado)
  } finally {
    carregando.value = false
  }
}

onMounted(carregarDados)
</script>

<template>
  <div class="pagina">
    <div class="cabecalho-pagina">
      <h1>Dashboard</h1>
    </div>

    <div class="grade-cards" style="margin-bottom: var(--espaco-5)">
      <div class="superficie">
        <div class="rotulo-secundario">Internet</div>
        <StatusBadge v-if="internet" :status="internet.status" />
        <div v-else class="rotulo-secundario">Sem dados ainda</div>
        <div class="dado-numerico" style="margin-top: var(--espaco-2); font-size: 20px;">
          {{ internet?.tempoResposta != null ? internet.tempoResposta + ' ms' : '—' }}
        </div>
        <div class="rotulo-secundario">Última verificação: {{ formatarHora(internet?.dataHora) }}</div>
      </div>

      <div class="superficie">
        <div class="rotulo-secundario">Disco</div>
        <div class="dado-numerico" style="font-size: 20px;">
          {{ disco?.percentualUtilizado != null ? disco.percentualUtilizado + '%' : '—' }}
        </div>
        <div class="rotulo-secundario" v-if="disco">
          {{ (disco.espacoLivre / 1e9).toFixed(1) }} GB livres de {{ (disco.espacoTotal / 1e9).toFixed(1) }} GB
        </div>
        <div class="rotulo-secundario">Última verificação: {{ formatarHora(disco?.dataHora) }}</div>
      </div>

      <div class="superficie">
        <div class="rotulo-secundario">Câmeras</div>
        <div class="dado-numerico" style="font-size: 20px;">{{ resumoCameras.total }} cadastradas</div>
        <div class="rotulo-secundario">
          <span style="color: var(--cor-status-ok)">{{ resumoCameras.online }} online</span> ·
          <span style="color: var(--cor-status-critico)">{{ resumoCameras.offline }} offline</span> ·
          {{ resumoCameras.inativa }} inativa(s)
        </div>
      </div>

      <div class="superficie">
        <div class="rotulo-secundario">Alertas recentes</div>
        <div class="dado-numerico" style="font-size: 20px;">
          {{ alertasRecentes.filter(a => !a.resolvido).length }} abertos
        </div>
        <router-link to="/alertas" class="rotulo-secundario">Ver todos →</router-link>
      </div>
    </div>

    <div class="grade-cards" style="grid-template-columns: repeat(auto-fit, minmax(360px, 1fr)); margin-bottom: var(--espaco-5)">
      <div class="superficie">
        <h3>Tempo de resposta — Internet (ms)</h3>
        <LineChart :labels="graficoInternetLabels" :valores="graficoInternetValores" rotulo="Tempo de resposta" cor="#3FA9F5" />
      </div>
      <div class="superficie">
        <h3>Uso de disco (%)</h3>
        <LineChart :labels="graficoDiscoLabels" :valores="graficoDiscoValores" rotulo="% utilizado" cor="#F5A623" />
      </div>
    </div>

    <div class="superficie">
      <h3>Alertas recentes</h3>
      <div v-if="alertasRecentes.length === 0" class="rotulo-secundario">Nenhum alerta registrado ainda.</div>
      <table v-else class="tabela-simples">
        <tbody>
          <tr v-for="alerta in alertasRecentes" :key="alerta.id">
            <td><StatusBadge :status="alerta.nivel" /></td>
            <td>{{ alerta.tipo }}</td>
            <td>{{ alerta.mensagem }}</td>
            <td class="rotulo-secundario">{{ formatarHora(alerta.dataHora) }}</td>
            <td>{{ alerta.resolvido ? 'Resolvido' : 'Aberto' }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.tabela-simples {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.tabela-simples td {
  padding: var(--espaco-2) var(--espaco-3);
  border-bottom: 1px solid var(--cor-borda);
}
</style>
