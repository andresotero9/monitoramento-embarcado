import api from './api'

export function listarAlertas(page = 0, size = 20) {
    return api.get('/alertas', { params: { page, size } }).then((res) => res.data)
}

export function listarAlertasRecentes() {
    return api.get('/alertas/recentes').then((res) => res.data)
}

export function listarAlertasAbertos() {
    return api.get('/alertas/abertos').then((res) => res.data)
}

export function resolverAlerta(id) {
    return api.put(`/alertas/${id}/resolver`).then((res) => res.data)
}
