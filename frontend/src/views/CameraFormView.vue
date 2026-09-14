<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from 'primevue/usetoast'
import InputText from 'primevue/inputtext'
import InputNumber from 'primevue/inputnumber'
import Password from 'primevue/password'
import ToggleSwitch from 'primevue/toggleswitch'
import Button from 'primevue/button'
import { buscarCamera, criarCamera, atualizarCamera } from '../services/camerasService'

const props = defineProps({
  id: { type: [String, Number], default: null }
})

const router = useRouter()
const toast = useToast()
const ehEdicao = computed(() => props.id != null)
const salvando = ref(false)

const form = ref({
  nome: '',
  descricao: '',
  enderecoIp: '',
  portaHttp: null,
  portaRtsp: 554,
  usuario: '',
  senha: '',
  ativa: true
})

async function carregarCamera() {
  if (!ehEdicao.value) return
  const camera = await buscarCamera(props.id)
  form.value = { ...camera, senha: '' }
}

async function salvar() {
  salvando.value = true
  try {
    const payload = { ...form.value }
    if (!payload.senha) {
      delete payload.senha // Atualização sem trocar a senha: backend preserva a senha atual.
    }

    if (ehEdicao.value) {
      await atualizarCamera(props.id, payload)
      toast.add({ severity: 'success', summary: 'Câmera atualizada', life: 3000 })
    } else {
      await criarCamera(payload)
      toast.add({ severity: 'success', summary: 'Câmera cadastrada', life: 3000 })
    }
    router.push({ name: 'cameras' })
  } catch (erro) {
    toast.add({
      severity: 'error',
      summary: 'Erro ao salvar câmera',
      detail: erro.response?.data?.message || 'Verifique os dados informados',
      life: 5000
    })
  } finally {
    salvando.value = false
  }
}

onMounted(carregarCamera)
</script>

<template>
  <div class="pagina">
    <div class="cabecalho-pagina">
      <h1>{{ ehEdicao ? 'Editar câmera' : 'Nova câmera' }}</h1>
    </div>

    <form class="superficie formulario" @submit.prevent="salvar">
      <div class="campo">
        <label>Nome</label>
        <InputText v-model="form.nome" required />
      </div>

      <div class="campo">
        <label>Descrição</label>
        <InputText v-model="form.descricao" />
      </div>

      <div class="formulario__linha">
        <div class="campo">
          <label>Endereço IP</label>
          <InputText v-model="form.enderecoIp" required placeholder="192.168.0.10" />
        </div>
        <div class="campo">
          <label>Porta HTTP</label>
          <InputNumber v-model="form.portaHttp" :useGrouping="false" />
        </div>
        <div class="campo">
          <label>Porta RTSP</label>
          <InputNumber v-model="form.portaRtsp" :useGrouping="false" required />
        </div>
      </div>

      <div class="formulario__linha">
        <div class="campo">
          <label>Usuário</label>
          <InputText v-model="form.usuario" />
        </div>
        <div class="campo">
          <label>Senha {{ ehEdicao ? '(deixe em branco para manter a atual)' : '' }}</label>
          <Password v-model="form.senha" :feedback="false" toggleMask />
        </div>
      </div>

      <div class="campo campo--linha">
        <ToggleSwitch v-model="form.ativa" />
        <label>Câmera ativa (monitorada pelo sistema)</label>
      </div>

      <div class="formulario__acoes">
        <Button type="button" label="Cancelar" severity="secondary" text @click="router.push({ name: 'cameras' })" />
        <Button type="submit" label="Salvar" :loading="salvando" />
      </div>
    </form>
  </div>
</template>

<style scoped>
.formulario {
  max-width: 640px;
  display: flex;
  flex-direction: column;
  gap: var(--espaco-4);
}

.formulario__linha {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: var(--espaco-4);
}

.formulario__acoes {
  display: flex;
  justify-content: flex-end;
  gap: var(--espaco-3);
  margin-top: var(--espaco-3);
}

.campo {
  display: flex;
  flex-direction: column;
  gap: var(--espaco-2);
}

.campo label {
  font-size: 12px;
  color: var(--cor-texto-secundario);
}

.campo--linha {
  flex-direction: row;
  align-items: center;
}
</style>
