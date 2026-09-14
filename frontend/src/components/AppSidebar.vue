<script setup>
const itensMenu = [
  { rota: '/', rotulo: 'Dashboard', icone: 'pi pi-th-large' },
  { rota: '/cameras', rotulo: 'Câmeras', icone: 'pi pi-video' },
  { rota: '/monitoramento', rotulo: 'Monitoramento', icone: 'pi pi-chart-line' },
  { rota: '/alertas', rotulo: 'Alertas', icone: 'pi pi-bell' },
  { rota: '/configuracoes', rotulo: 'Configurações', icone: 'pi pi-cog' }
]
</script>

<template>
  <aside class="sidebar">
    <div class="sidebar__marca">
      <span class="sidebar__marca-ponto"></span>
      <span class="sidebar__marca-texto">MONITOR<br />EMBARCADO</span>
    </div>
    <nav class="sidebar__nav">
      <router-link
        v-for="item in itensMenu"
        :key="item.rota"
        :to="item.rota"
        class="sidebar__link"
        active-class="sidebar__link--ativo"
      >
        <i :class="item.icone"></i>
        <span>{{ item.rotulo }}</span>
      </router-link>
    </nav>
  </aside>
</template>

<style scoped>
.sidebar {
  width: var(--largura-sidebar);
  flex-shrink: 0;
  background: var(--cor-superficie);
  border-right: 1px solid var(--cor-borda);
  display: flex;
  flex-direction: column;
}

.sidebar__marca {
  display: flex;
  align-items: center;
  gap: var(--espaco-3);
  padding: var(--espaco-5) var(--espaco-4);
  border-bottom: 1px solid var(--cor-borda);
}

.sidebar__marca-ponto {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--cor-status-ok);
  flex-shrink: 0;
}

.sidebar__marca-texto {
  font-family: var(--fonte-dados);
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.04em;
  line-height: 1.4;
  color: var(--cor-texto-secundario);
}

.sidebar__nav {
  display: flex;
  flex-direction: column;
  padding: var(--espaco-3);
  gap: 2px;
}

.sidebar__link {
  display: flex;
  align-items: center;
  gap: var(--espaco-3);
  padding: var(--espaco-3) var(--espaco-3);
  border-radius: var(--raio-borda);
  color: var(--cor-texto-secundario);
  text-decoration: none;
  font-size: 13px;
  font-weight: 500;
  transition: background-color 0.12s ease, color 0.12s ease;
}

.sidebar__link:hover {
  background: var(--cor-superficie-alta);
  color: var(--cor-texto);
}

.sidebar__link--ativo {
  background: var(--cor-superficie-alta);
  color: var(--cor-acento);
}
</style>
