<script setup>
import { reactive, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { useToast } from 'primevue/usetoast'
import InputText from 'primevue/inputtext'
import Password from 'primevue/password'
import Button from 'primevue/button'
import Message from 'primevue/message'
import { register } from '../services/authService'

const router = useRouter()
const toast = useToast()

const form = reactive({
  nome: '',
  username: '',
  password: '',
  confirmPassword: ''
})

const erros = reactive({
  nome: '',
  username: '',
  password: '',
  confirmPassword: ''
})

const mensagemErro = ref('')
const cadastrando = ref(false)

const SENHA_MIN = 8
const REGEX_SENHA_FORTE = /^(?=.*[A-Za-z])(?=.*\d).+$/

function validar() {
  erros.nome = ''
  erros.username = ''
  erros.password = ''
  erros.confirmPassword = ''

  const nome = form.nome.trim()
  if (!nome) {
    erros.nome = 'O nome é obrigatório.'
  } else if (nome.length < 3 || nome.length > 150) {
    erros.nome = 'O nome deve ter entre 3 e 150 caracteres.'
  }

  const username = form.username.trim()
  if (!username) {
    erros.username = 'O username é obrigatório.'
  } else if (/\s/.test(username)) {
    erros.username = 'O username não pode conter espaços.'
  } else if (username.length < 3 || username.length > 50) {
    erros.username = 'O username deve ter entre 3 e 50 caracteres.'
  }

  if (!form.password) {
    erros.password = 'A senha é obrigatória.'
  } else if (form.password.length < SENHA_MIN) {
    erros.password = `A senha deve ter no mínimo ${SENHA_MIN} caracteres.`
  } else if (!REGEX_SENHA_FORTE.test(form.password)) {
    erros.password = 'A senha deve conter letras e números.'
  }

  if (!form.confirmPassword) {
    erros.confirmPassword = 'A confirmação de senha é obrigatória.'
  } else if (form.password && form.confirmPassword !== form.password) {
    erros.confirmPassword = 'As senhas não conferem.'
  }

  return !erros.nome && !erros.username && !erros.password && !erros.confirmPassword
}

async function cadastrar() {
  mensagemErro.value = ''

  if (!validar()) {
    return
  }

  cadastrando.value = true
  try {
    await register({
      nome: form.nome.trim(),
      username: form.username.trim(),
      password: form.password,
      confirmPassword: form.confirmPassword
    })
    toast.add({ severity: 'success', summary: 'Usuário criado com sucesso!', life: 3000 })
    router.push({ name: 'login' })
  } catch (erro) {
    const status = erro.response?.status
    if (status === 409) {
      erros.username = 'Username já cadastrado.'
    } else {
      mensagemErro.value =
        erro.response?.data?.detalhes?.[0] ||
        erro.response?.data?.message ||
        'Não foi possível concluir o cadastro. Verifique os dados informados.'
    }
  } finally {
    cadastrando.value = false
  }
}
</script>

<template>
  <div class="tela-cadastro">
    <form class="tela-cadastro__cartao superficie" @submit.prevent="cadastrar">
      <div class="tela-cadastro__marca">
        <span class="tela-cadastro__ponto"></span>
        <h1>Monitoramento Embarcado</h1>
      </div>
      <p class="rotulo-secundario">Preencha os dados abaixo para criar sua conta.</p>

      <Message v-if="mensagemErro" severity="error" :closable="false">{{ mensagemErro }}</Message>

      <div class="campo">
        <label for="nome">Nome</label>
        <InputText id="nome" v-model="form.nome" autofocus :invalid="Boolean(erros.nome)" />
        <small v-if="erros.nome" class="campo__erro">{{ erros.nome }}</small>
      </div>

      <div class="campo">
        <label for="username">Username</label>
        <InputText id="username" v-model="form.username" autocomplete="username" :invalid="Boolean(erros.username)" />
        <small v-if="erros.username" class="campo__erro">{{ erros.username }}</small>
      </div>

      <div class="campo">
        <label for="password">Senha</label>
        <Password
          id="password"
          v-model="form.password"
          :feedback="false"
          toggleMask
          autocomplete="new-password"
          :invalid="Boolean(erros.password)"
        />
        <small v-if="erros.password" class="campo__erro">{{ erros.password }}</small>
        <small v-else class="rotulo-secundario">Mínimo de 8 caracteres, com letras e números.</small>
      </div>

      <div class="campo">
        <label for="confirmPassword">Confirmar senha</label>
        <Password
          id="confirmPassword"
          v-model="form.confirmPassword"
          :feedback="false"
          toggleMask
          autocomplete="new-password"
          :invalid="Boolean(erros.confirmPassword)"
        />
        <small v-if="erros.confirmPassword" class="campo__erro">{{ erros.confirmPassword }}</small>
      </div>

      <Button type="submit" label="Criar usuário" :loading="cadastrando" class="tela-cadastro__botao" />

      <RouterLink :to="{ name: 'login' }" class="tela-cadastro__link">Voltar para login</RouterLink>
    </form>
  </div>
</template>

<style scoped>
.tela-cadastro {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--cor-fundo);
  padding: var(--espaco-4);
}

.tela-cadastro__cartao {
  width: 380px;
  max-width: 100%;
  display: flex;
  flex-direction: column;
  gap: var(--espaco-4);
}

.tela-cadastro__marca {
  display: flex;
  align-items: center;
  gap: var(--espaco-3);
}

.tela-cadastro__marca h1 {
  font-size: 18px;
  margin: 0;
}

.tela-cadastro__ponto {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--cor-status-ok);
  flex-shrink: 0;
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

.campo :deep(.p-inputtext),
.campo :deep(.p-password) {
  width: 100%;
}

.campo__erro {
  color: var(--cor-status-critico);
  font-size: 12px;
}

.tela-cadastro__botao {
  margin-top: var(--espaco-2);
}

.tela-cadastro__link {
  align-self: center;
  font-size: 13px;
  color: var(--cor-texto-secundario);
  text-decoration: none;
}

.tela-cadastro__link:hover {
  color: var(--cor-acento);
}
</style>
