import api from './api'

export function listarCameras() {
    return api.get('/cameras').then((res) => res.data)
}

export function buscarCamera(id) {
    return api.get(`/cameras/${id}`).then((res) => res.data)
}

export function criarCamera(dados) {
    return api.post('/cameras', dados).then((res) => res.data)
}

export function atualizarCamera(id, dados) {
    return api.put(`/cameras/${id}`, dados).then((res) => res.data)
}

export function excluirCamera(id) {
    return api.delete(`/cameras/${id}`)
}
