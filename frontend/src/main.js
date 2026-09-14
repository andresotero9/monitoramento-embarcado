import { createApp } from 'vue'
import PrimeVue from 'primevue/config'
import { definePreset } from '@primevue/themes'
import Aura from '@primevue/themes/aura'
import ToastService from 'primevue/toastservice'
import ConfirmationService from 'primevue/confirmationservice'

import 'primeicons/primeicons.css'
import './assets/styles/global.css'

import App from './App.vue'
import router from './router'

// Preset customizado: mantém a estrutura do Aura, mas usa o acento técnico
// (azul) e superfícies escuras definidos nos tokens de design do projeto,
// em vez da paleta padrão do PrimeVue.
const PresetTecnico = definePreset(Aura, {
    semantic: {
        primary: {
            50: '#eaf6fe', 100: '#cfeafd', 200: '#a3d8fb', 300: '#71c1f8',
            400: '#4bacf7', 500: '#3FA9F5', 600: '#2d8fd6', 700: '#2571ad',
            800: '#215d8c', 900: '#1e4d73', 950: '#142f47'
        }
    }
})

const app = createApp(App)

app.use(router)
app.use(PrimeVue, {
    theme: {
        preset: PresetTecnico,
        options: { darkModeSelector: false }
    }
})
app.use(ToastService)
app.use(ConfirmationService)

app.mount('#app')
