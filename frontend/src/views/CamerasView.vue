<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useConfirm } from 'primevue/useconfirm'
import { useToast } from 'primevue/usetoast'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Button from 'primevue/button'
import Tag from 'primevue/tag'
import ConfirmDialog from 'primevue/confirmdialog'
import { listarCameras, excluirCamera } from '../services/camerasService'

const router = useRouter()
const confirmar = useConfirm()
const toast = useToast()

const cameras = ref([])
const carregando = ref(true)

async function carregar() {
  carregando.value = true
  try {
    cameras.value = await listarCameras()
  } finally {
    carregando.value = false
  }
}

function irParaEdicao(camera) {
  router.push({ name: 'camera-editar', params: { id: camera.id } })
}

function irParaStream(camera) {
  router.push({ name: 'camera-stream', params: { id: camera.id } })
}

function confirmarExclusao(camera) {
  confirmar.require({
    message: `Remover a câmera "${camera.nome}"? Essa ação não pode ser desfeita.`,
    header: 'Confirmar remoção',
    icon: 'pi pi-exclamation-triangle',
    acceptLabel: 'Remover',
    rejectLabel: 'Cancelar',
    acceptClass: 'p-button-danger',
    accept: async () => {
      await excluirCamera(camera.id)
      toast.add({ severity: 'success', summary: 'Câmera removida', life: 3000 })
      carregar()
    }
  })
}

onMounted(carregar)
</script>

<template>
  <div class="pagina">
    <ConfirmDialog />
    <div class="cabecalho-pagina">
      <h1>Câmeras</h1>
      <Button label="Nova câmera" icon="pi pi-plus" @click="router.push({ name: 'camera-nova' })" />
    </div>

    <div class="superficie">
      <DataTable :value="cameras" :loading="carregando" dataKey="id" responsiveLayout="scroll">
        <Column field="nome" header="Nome" />
        <Column field="enderecoIp" header="Endereço IP" />
        <Column field="portaRtsp" header="Porta RTSP" />
        <Column header="Status">
          <template #body="{ data }">
            <Tag :value="data.ativa ? 'Ativa' : 'Inativa'" :severity="data.ativa ? 'success' : 'secondary'" />
          </template>
        </Column>
        <Column header="Ações" style="width: 220px">
          <template #body="{ data }">
            <div style="display: flex; gap: var(--espaco-2)">
              <Button icon="pi pi-video" text rounded aria-label="Ver stream" @click="irParaStream(data)" />
              <Button icon="pi pi-pencil" text rounded aria-label="Editar" @click="irParaEdicao(data)" />
              <Button icon="pi pi-trash" text rounded severity="danger" aria-label="Remover" @click="confirmarExclusao(data)" />
            </div>
          </template>
        </Column>
        <template #empty>Nenhuma câmera cadastrada ainda.</template>
      </DataTable>
    </div>
  </div>
</template>
