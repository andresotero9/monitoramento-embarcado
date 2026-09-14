<script setup>
import { ref, computed, onMounted } from 'vue'
import { useToast } from 'primevue/usetoast'
import InputText from 'primevue/inputtext'
import InputNumber from 'primevue/inputnumber'
import Button from 'primevue/button'
import { listarConfiguracoes, atualizarConfiguracoes } from '../services/configuracoesService'

const toast = useToast()
const configuracoes = ref([])
const salvando = ref(false)

const GRUPOS = [
  { prefixo: 'internet.', titulo: 'Internet' },
  { prefixo: 'disco.', titulo: 'Disco' },
  { prefixo: 'camera.', titulo: 'Câmeras' }
]

function grupoDe(chave) {
  return GRUPOS.find((g) => chave.startsWith(g.prefixo))?.titulo || 'Outras'
}

const configuracoesAgrupadas = computed(() => {
  const grupos = {}
  for (const config of configuracoes.value) {
    const titulo = grupoDe(config.chave)
    if (!grupos[titulo]) grupos[titulo] = []
    grupos[titulo].push(config)
  }
  return grupos
})

async function carregar() {
  const dados = await listarConfiguracoes()
  // InputNumber exige Number; a API retorna "valor" sempre como String.
  configuracoes.value = dados.map((config) => ({
    ...config,
    valor: (config.tipo === 'INTEGER' || config.tipo === 'DECIMAL') ? Number(config.valor) : config.valor
  }))
}

async function salvar() {
  salvando.value = true
  try {
    const payload = configuracoes.value.map((config) => ({ ...config, valor: String(config.valor) }))
    await atualizarConfiguracoes(payload)
    toast.add({ severity: 'success', summary: 'Configurações salvas', life: 3000 })
    await carregar()
  } catch (erro) {
    toast.add({
      severity: 'error',
      summary: 'Erro ao salvar configurações',
      detail: erro.response?.data?.message,
      life: 5000
    })
  } finally {
    salvando.value = false
  }
}

onMounted(carregar)
</script>

<template>
  <div class="pagina">
    <div class="cabecalho-pagina">
      <h1>Configurações</h1>
      <Button label="Salvar alterações" icon="pi pi-save" :loading="salvando" @click="salvar" />
    </div>

    <div v-for="(itens, titulo) in configuracoesAgrupadas" :key="titulo" class="superficie" style="margin-bottom: var(--espaco-4)">
      <h3>{{ titulo }}</h3>
      <div class="config-grade">
        <div v-for="config in itens" :key="config.chave" class="campo">
          <label>{{ config.descricao || config.chave }}</label>
          <InputNumber
            v-if="config.tipo === 'INTEGER' || config.tipo === 'DECIMAL'"
            v-model="config.valor"
            :useGrouping="false"
            :minFractionDigits="config.tipo === 'DECIMAL' ? 0 : undefined"
            :maxFractionDigits="config.tipo === 'DECIMAL' ? 2 : undefined"
          />
          <InputText v-else v-model="config.valor" />
          <span class="rotulo-secundario dado-numerico">{{ config.chave }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.config-grade {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: var(--espaco-4);
}

.campo {
  display: flex;
  flex-direction: column;
  gap: var(--espaco-2);
}

.campo label {
  font-size: 13px;
}
</style>
