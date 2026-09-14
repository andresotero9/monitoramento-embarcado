<script setup>
import { ref } from 'vue'
import { useRouter, useRoute, RouterLink } from 'vue-router'
import InputText from 'primevue/inputtext'
import Password from 'primevue/password'
import Button from 'primevue/button'
import Message from 'primevue/message'
import { login } from '../services/authService'
import { definirSessao } from '../stores/auth'

const router = useRouter()
const route = useRoute()

const username = ref('')
const password = ref('')
const carregando = ref(false)
const mensagemErro = ref('')

async function autenticar() {
  mensagemErro.value = ''
  carregando.value = true
  try {
    const resposta = await login(username.value, password.value)
    definirSessao(resposta)
    router.push(route.query.redirect || '/')
  } catch (erro) {
    mensagemErro.value = erro.response?.data?.message || 'Usuário ou senha inválidos'
  } finally {
    carregando.value = false
  }
}
</script>

<template>
  <div class="tela-login">
    <form class="tela-login__cartao superficie" @submit.prevent="autenticar">
      <div class="tela-login__marca">
        <span class="tela-login__ponto"></span>
        <h1>Monitoramento Embarcado</h1>
      </div>
      <p class="rotulo-secundario">Acesse com suas credenciais para continuar.</p>

      <Message v-if="mensagemErro" severity="error" :closable="false">{{ mensagemErro }}</Message>

      <div class="campo">
        <label for="username">Usuário</label>
        <InputText id="username" v-model="username" autofocus required autocomplete="username" />
      </div>

      <div class="campo">
        <label for="password">Senha</label>
        <Password
          id="password"
          v-model="password"
          :feedback="false"
          toggleMask
          required
          autocomplete="current-password"
        />
      </div>

      <Button type="submit" label="Entrar" :loading="carregando" class="tela-login__botao" />

      <RouterLink :to="{ name: 'register' }" class="tela-login__link">
        Ainda não possui usuário? Criar usuário
      </RouterLink>
    </form>
  </div>
</template>

<style scoped>
.tela-login {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--cor-fundo);
}

.tela-login__cartao {
  width: 360px;
  display: flex;
  flex-direction: column;
  gap: var(--espaco-4);
}

.tela-login__marca {
  display: flex;
  align-items: center;
  gap: var(--espaco-3);
}

.tela-login__marca h1 {
  font-size: 18px;
  margin: 0;
}

.tela-login__ponto {
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

.tela-login__botao {
  margin-top: var(--espaco-2);
}

.tela-login__link {
  align-self: center;
  font-size: 13px;
  color: var(--cor-texto-secundario);
  text-decoration: none;
}

.tela-login__link:hover {
  color: var(--cor-acento);
}
</style>
