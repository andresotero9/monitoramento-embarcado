import api from './api'

export function iniciarStream(cameraId) {
    return api.post(`/stream/${cameraId}/start`).then((res) => res.data)
}

export function pararStream(cameraId) {
    return api.delete(`/stream/${cameraId}/stop`)
}

export function statusStream(cameraId) {
    return api.get(`/stream/${cameraId}/status`).then((res) => res.data)
}
