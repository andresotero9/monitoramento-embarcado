# Este Dockerfile é construído com o CONTEXTO NA RAIZ DO REPOSITÓRIO
# (ver docker-compose.yml: build.context = ".", build.dockerfile = "frontend/Dockerfile"),
# para poder copiar docker/nginx.conf, que fica fora da pasta frontend/.

# ---- Stage 1: build ----
FROM node:20-alpine AS build

WORKDIR /app

COPY frontend/package.json ./
RUN npm install

COPY frontend/. .
RUN npm run build

# ---- Stage 2: runtime ----
FROM nginx:1.27-alpine

COPY --from=build /app/dist /usr/share/nginx/html
COPY docker/nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

HEALTHCHECK --interval=15s --timeout=5s --start-period=10s --retries=5 \
    CMD wget -q --spider http://localhost/ || exit 1
