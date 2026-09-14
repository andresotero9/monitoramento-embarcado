import { createRouter, createWebHistory } from 'vue-router'
import { estaAutenticado } from '../stores/auth'

const routes = [
    {
        path: '/login',
        name: 'login',
        component: () => import('../views/LoginView.vue'),
        meta: { publica: true }
    },
    {
        path: '/register',
        name: 'register',
        component: () => import('../views/RegisterView.vue'),
        meta: { publica: true }
    },
    {
        path: '/',
        name: 'dashboard',
        component: () => import('../views/DashboardView.vue')
    },
    {
        path: '/cameras',
        name: 'cameras',
        component: () => import('../views/CamerasView.vue')
    },
    {
        path: '/cameras/novo',
        name: 'camera-nova',
        component: () => import('../views/CameraFormView.vue')
    },
    {
        path: '/cameras/:id/editar',
        name: 'camera-editar',
        component: () => import('../views/CameraFormView.vue'),
        props: true
    },
    {
        path: '/cameras/:id/stream',
        name: 'camera-stream',
        component: () => import('../views/StreamView.vue'),
        props: true
    },
    {
        path: '/monitoramento',
        name: 'monitoramento',
        component: () => import('../views/MonitoramentoView.vue')
    },
    {
        path: '/alertas',
        name: 'alertas',
        component: () => import('../views/AlertasView.vue')
    },
    {
        path: '/configuracoes',
        name: 'configuracoes',
        component: () => import('../views/ConfiguracoesView.vue')
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

router.beforeEach((to) => {
    if (!to.meta.publica && !estaAutenticado()) {
        return { name: 'login', query: { redirect: to.fullPath } }
    }
    if (to.name === 'login' && estaAutenticado()) {
        return { name: 'dashboard' }
    }
    return true
})

export default router
