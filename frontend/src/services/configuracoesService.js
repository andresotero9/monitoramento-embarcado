import api from './api'

export function listarConfiguracoes() {
    return api.get('/configuracoes').then((res) => res.data)
}

export function atualizarConfiguracoes(configuracoes) {
    return api.put('/configuracoes', configuracoes).then((res) => res.data)
}
