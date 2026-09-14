import api from './api'

export function buscarUltimoInternet() {
    return api.get('/monitoramento/internet/latest').then((res) => res.data)
}

export function listarHistoricoInternet(page = 0, size = 20) {
    return api.get('/monitoramento/internet', { params: { page, size } }).then((res) => res.data)
}

export function buscarUltimoDisco() {
    return api.get('/monitoramento/disco/latest').then((res) => res.data)
}

export function listarHistoricoDisco(page = 0, size = 20) {
    return api.get('/monitoramento/disco', { params: { page, size } }).then((res) => res.data)
}

export function listarHistoricoCameras(page = 0, size = 20) {
    return api.get('/monitoramento/cameras', { params: { page, size } }).then((res) => res.data)
}

export function listarHistoricoPorCamera(cameraId, page = 0, size = 20) {
    return api.get(`/monitoramento/cameras/${cameraId}`, { params: { page, size } }).then((res) => res.data)
}

export function buscarUltimoPorCamera(cameraId) {
    return api.get(`/monitoramento/cameras/${cameraId}/latest`).then((res) => res.data)
}
