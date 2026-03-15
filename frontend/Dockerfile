# syntax=docker/dockerfile:1.7-labs

FROM node:22-alpine AS builder
WORKDIR /app

ARG NG_APP_API_URL=/api/v1
ARG NG_APP_GOOGLE_CLIENT_ID=
ENV NG_APP_API_URL=${NG_APP_API_URL}
ENV NG_APP_GOOGLE_CLIENT_ID=${NG_APP_GOOGLE_CLIENT_ID}

COPY package.json package-lock.json ./
RUN --mount=type=cache,target=/root/.npm npm ci --silent

COPY . .
RUN npm run build

FROM nginx:1.27-alpine AS runtime
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /app/dist/portal-noticias-ui/browser /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
