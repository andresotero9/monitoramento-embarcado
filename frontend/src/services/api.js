import axios from 'axios'
import { authStore } from '../stores/auth'
import router from '../router'

const api = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || '/api'
})

api.interceptors.request.use((config) => {
    if (authStore.token) {
        config.headers.Authorization = `Bearer ${authStore.token}`
    }
    return config
})

api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response && error.response.status === 401) {
            authStore.logout()
            router.push('/login')
        }
        return Promise.reject(error)
    }
)

export default api
