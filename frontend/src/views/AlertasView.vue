<script setup>
import { ref, onMounted } from 'vue'
import { useToast } from 'primevue/usetoast'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Button from 'primevue/button'
import SelectButton from 'primevue/selectbutton'
import StatusBadge from '../components/StatusBadge.vue'
import { listarAlertas, listarAlertasAbertos, resolverAlerta } from '../services/alertasService'

const TAMANHO_PAGINA = 20
const toast = useToast()

const filtro = ref('abertos')
const opcoesFiltro = [
  { rotulo: 'Abertos', valor: 'abertos' },
  { rotulo: 'Todos', valor: 'todos' }
]

const alertas = ref([])
const totalRegistros = ref(0)
const carregando = ref(false)

function formatarHora(dataHoraIso) {
  return dataHoraIso ? new Date(dataHoraIso).toLocaleString('pt-BR') : '—'
}

async function carregar(page = 0) {
  carregando.value = true
  try {
    if (filtro.value === 'abertos') {
      const dados = await listarAlertasAbertos()
      alertas.value = dados
      totalRegistros.value = dados.length
    } else {
      const pagina = await listarAlertas(page, TAMANHO_PAGINA)
      alertas.value = pagina.content
      totalRegistros.value = pagina.totalElements
    }
  } finally {
    carregando.value = false
  }
}

async function resolver(alerta) {
  await resolverAlerta(alerta.id)
  toast.add({ severity: 'success', summary: 'Alerta resolvido', life: 3000 })
  carregar()
}

function aoMudarPagina(evento) {
  carregar(evento.page)
}

onMounted(() => carregar())
</script>

<template>
  <div class="pagina">
    <div class="cabecalho-pagina">
      <h1>Alertas</h1>
      <SelectButton v-model="filtro" :options="opcoesFiltro" optionLabel="rotulo" optionValue="valor" @change="carregar(0)" />
    </div>

    <div class="superficie">
      <DataTable
        :value="alertas"
        :loading="carregando"
        :lazy="filtro === 'todos'"
        :paginator="filtro === 'todos'"
        :rows="TAMANHO_PAGINA"
        :totalRecords="totalRegistros"
        @page="aoMudarPagina"
      >
        <Column header="Nível">
          <template #body="{ data }"><StatusBadge :status="data.nivel" /></template>
        </Column>
        <Column field="tipo" header="Tipo" />
        <Column field="origem" header="Origem" />
        <Column field="mensagem" header="Mensagem" />
        <Column header="Aberto em">
          <template #body="{ data }">{{ formatarHora(data.dataHora) }}</template>
        </Column>
        <Column header="Status">
          <template #body="{ data }">
            <span :style="{ color: data.resolvido ? 'var(--cor-status-ok)' : 'var(--cor-status-critico)' }">
              {{ data.resolvido ? 'Resolvido' : 'Aberto' }}
            </span>
          </template>
        </Column>
        <Column header="">
          <template #body="{ data }">
            <Button
              v-if="!data.resolvido"
              label="Resolver"
              size="small"
              text
              @click="resolver(data)"
            />
          </template>
        </Column>
        <template #empty>Nenhum alerta encontrado.</template>
      </DataTable>
    </div>
  </div>
</template>
