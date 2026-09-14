import { reactive } from 'vue'

const CHAVE_STORAGE = 'monitoramento.auth'

function carregarEstadoInicial() {
    const bruto = localStorage.getItem(CHAVE_STORAGE)
    if (!bruto) {
        return { token: null, username: null, role: null }
    }
    try {
        return JSON.parse(bruto)
    } catch {
        return { token: null, username: null, role: null }
    }
}

export const authStore = reactive(carregarEstadoInicial())

export function definirSessao({ token, username, role }) {
    authStore.token = token
    authStore.username = username
    authStore.role = role
    localStorage.setItem(CHAVE_STORAGE, JSON.stringify({ token, username, role }))
}

authStore.logout = function logout() {
    this.token = null
    this.username = null
    this.role = null
    localStorage.removeItem(CHAVE_STORAGE)
}

export function estaAutenticado() {
    return Boolean(authStore.token)
}
