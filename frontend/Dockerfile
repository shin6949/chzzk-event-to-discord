# syntax=docker/dockerfile:1

FROM node:24-alpine AS build
WORKDIR /app

COPY package.json yarn.lock ./
RUN yarn install --frozen-lockfile

COPY . .

ARG VITE_API_BASE_URL
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}

RUN yarn build

FROM nginx:1.30.2-alpine
WORKDIR /usr/share/nginx/html
RUN rm -rf ./*

COPY --from=build /app/dist/ .
COPY nginx/default.conf /etc/nginx/conf.d/default.conf
RUN sed -i 's#pid .*nginx.pid;#pid /tmp/nginx.pid;#' /etc/nginx/nginx.conf \
    && chown -R nginx:nginx /usr/share/nginx/html /var/cache/nginx /var/run /var/log/nginx

USER nginx

EXPOSE 8080
CMD ["nginx", "-g", "daemon off;"]
