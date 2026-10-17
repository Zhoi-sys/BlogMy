FROM node:24-alpine AS build
RUN npm install -g pnpm@11.28.5
WORKDIR /app
COPY package.json pnpm-lock.yaml pnpm-workspace.yaml .npmrc ./
RUN pnpm install --frozen-lockfile
COPY . .
ARG NUXT_API_BASE=http://backend:8080/api
ARG NUXT_PUBLIC_API_BASE=/api
ARG NUXT_PUBLIC_SITE_URL=http://localhost:8080
ENV NUXT_API_BASE=$NUXT_API_BASE \
    NUXT_PUBLIC_API_BASE=$NUXT_PUBLIC_API_BASE \
    NUXT_PUBLIC_SITE_URL=$NUXT_PUBLIC_SITE_URL
RUN pnpm build

FROM node:24-alpine
WORKDIR /app
ENV NODE_ENV=production \
    PORT=3000 \
    HOST=0.0.0.0
COPY --from=build /app/.output ./.output
USER node
EXPOSE 3000
CMD ["node", ".output/server/index.mjs"]
