# ── Stage 1: Build ─────────────────────────────────────────────────────────────
FROM node:20-alpine AS builder

WORKDIR /app

# Copy package files first for dependency caching
COPY package*.json ./
RUN npm ci --silent

# Copy source and build
COPY . .
# VITE_API_URL is empty — Nginx will proxy /api and /ws to backend at runtime
ENV VITE_API_URL=""
RUN npm run build

# ── Stage 2: Serve ─────────────────────────────────────────────────────────────
# Serve the built static files with Nginx
FROM nginx:alpine

# Remove default Nginx page
RUN rm -rf /usr/share/nginx/html/*

# Copy the built frontend
COPY --from=builder /app/dist /usr/share/nginx/html

# Custom Nginx config that proxies /api and /ws to the backend service
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
