<script setup>
import { ref, onMounted } from 'vue'
import Tabs from 'primevue/tabs'
import TabList from 'primevue/tablist'
import Tab from 'primevue/tab'
import TabPanels from 'primevue/tabpanels'
import TabPanel from 'primevue/tabpanel'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import StatusBadge from '../components/StatusBadge.vue'
import {
  listarHistoricoInternet,
  listarHistoricoDisco,
  listarHistoricoCameras
} from '../services/monitoramentoService'

const TAMANHO_PAGINA = 15

const internet = ref({ content: [], totalElements: 0 })
const disco = ref({ content: [], totalElements: 0 })
const cameras = ref({ content: [], totalElements: 0 })
const carregando = ref({ internet: false, disco: false, cameras: false })

function formatarHora(dataHoraIso) {
  return new Date(dataHoraIso).toLocaleString('pt-BR')
}

async function carregarInternet(page = 0) {
  carregando.value.internet = true
  try {
    internet.value = await listarHistoricoInternet(page, TAMANHO_PAGINA)
  } finally {
    carregando.value.internet = false
  }
}

async function carregarDisco(page = 0) {
  carregando.value.disco = true
  try {
    disco.value = await listarHistoricoDisco(page, TAMANHO_PAGINA)
  } finally {
    carregando.value.disco = false
  }
}

async function carregarCameras(page = 0) {
  carregando.value.cameras = true
  try {
    cameras.value = await listarHistoricoCameras(page, TAMANHO_PAGINA)
  } finally {
    carregando.value.cameras = false
  }
}

function aoMudarPaginaInternet(evento) {
  carregarInternet(evento.page)
}

function aoMudarPaginaDisco(evento) {
  carregarDisco(evento.page)
}

function aoMudarPaginaCameras(evento) {
  carregarCameras(evento.page)
}

onMounted(() => {
  carregarInternet()
  carregarDisco()
  carregarCameras()
})
</script>

<template>
  <div class="pagina">
    <div class="cabecalho-pagina">
      <h1>Monitoramento</h1>
    </div>

    <div class="superficie">
      <Tabs value="internet">
        <TabList>
          <Tab value="internet">Internet</Tab>
          <Tab value="disco">Disco</Tab>
          <Tab value="cameras">Câmeras</Tab>
        </TabList>
        <TabPanels>
          <TabPanel value="internet">
            <DataTable
              :value="internet.content"
              :loading="carregando.internet"
              lazy
              paginator
              :rows="TAMANHO_PAGINA"
              :totalRecords="internet.totalElements"
              @page="aoMudarPaginaInternet"
            >
              <Column header="Status">
                <template #body="{ data }"><StatusBadge :status="data.status" /></template>
              </Column>
              <Column header="Tempo de resposta">
                <template #body="{ data }">
                  <span class="dado-numerico">{{ data.tempoResposta != null ? data.tempoResposta + ' ms' : '—' }}</span>
                </template>
              </Column>
              <Column header="Data/hora">
                <template #body="{ data }">{{ formatarHora(data.dataHora) }}</template>
              </Column>
              <Column field="mensagemErro" header="Detalhes" />
            </DataTable>
          </TabPanel>

          <TabPanel value="disco">
            <DataTable
              :value="disco.content"
              :loading="carregando.disco"
              lazy
              paginator
              :rows="TAMANHO_PAGINA"
              :totalRecords="disco.totalElements"
              @page="aoMudarPaginaDisco"
            >
              <Column header="% utilizado">
                <template #body="{ data }"><span class="dado-numerico">{{ data.percentualUtilizado }}%</span></template>
              </Column>
              <Column header="Espaço livre">
                <template #body="{ data }">
                  <span class="dado-numerico">{{ (data.espacoLivre / 1e9).toFixed(1) }} GB</span>
                </template>
              </Column>
              <Column header="Espaço total">
                <template #body="{ data }">
                  <span class="dado-numerico">{{ (data.espacoTotal / 1e9).toFixed(1) }} GB</span>
                </template>
              </Column>
              <Column header="Data/hora">
                <template #body="{ data }">{{ formatarHora(data.dataHora) }}</template>
              </Column>
            </DataTable>
          </TabPanel>

          <TabPanel value="cameras">
            <DataTable
              :value="cameras.content"
              :loading="carregando.cameras"
              lazy
              paginator
              :rows="TAMANHO_PAGINA"
              :totalRecords="cameras.totalElements"
              @page="aoMudarPaginaCameras"
            >
              <Column field="cameraNome" header="Câmera" />
              <Column header="Status">
                <template #body="{ data }"><StatusBadge :status="data.status" /></template>
              </Column>
              <Column header="Ping">
                <template #body="{ data }">
                  <span class="dado-numerico">{{ data.tempoPing != null ? data.tempoPing + ' ms' : '—' }}</span>
                </template>
              </Column>
              <Column header="Frame capturado">
                <template #body="{ data }">{{ data.frameCapturado ? 'Sim' : 'Não' }}</template>
              </Column>
              <Column header="Data/hora">
                <template #body="{ data }">{{ formatarHora(data.dataHora) }}</template>
              </Column>
              <Column field="mensagemErro" header="Detalhes" />
            </DataTable>
          </TabPanel>
        </TabPanels>
      </Tabs>
    </div>
  </div>
</template>
