
---

# Документация и запуск

## Документация API

Файл спецификации: `docs/openapi.yaml`
Swagger UI доступен по адресу: `http://localhost:8089/swagger-ui.html`

## Запуск через Docker

1. В корне проекта выполнить:

```bash
docker-compose up --build
```

2. Backend доступен по `http://localhost:8090`
3. Для остановки:

```bash
docker-compose down
```

Миграции базы данных применяются автоматически через Liquibase.

---


