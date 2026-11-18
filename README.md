# Reliability project

## О проекте

Демонстрация отказоустойчивости микросервисов на Spring Boot. Сервис **main_service** принимает запрос на список рекомендованных игр, сначала получает рекомендации из **recommendations**, затем запрашивает по их идентификаторам названия игр в **game_service**. Внешние сервисы намеренно нестабильны (периодически возвращают ошибки), а main_service защищается от этого через **Resilience4j**: retry, circuit breaker, rate limiter и кэш Caffeine.

## Стек

| Компонент | Технологии |
|---|---|
| Общее | Java 21, Spring Boot 4.1.2, Gradle (wrapper 9.6.0), Lombok, Docker, Docker Compose |
| main_service | Spring Web MVC, Spring Boot Actuator, Spring AOP, Resilience4j (retry, circuit breaker, rate limiter), Caffeine cache, RestClient |
| game_service | Spring Web MVC |
| recommendations | Spring Web MVC |

## Быстрый старт

```powershell
docker compose up --build
```

Сервисы запускаются независимо друг от друга, main_service стартует после game_service и recommendations.

| Что | Адрес |
|---|---|
| Рекомендации игр (main_service) | http://localhost:8080/api/v1/service/games-recommendation |
| Health и метрики main_service | http://localhost:8080/actuator/health |
| Состояние circuit breaker | http://localhost:8080/actuator/circuitbreakers |
| game_service (напрямую) | http://localhost:8081/api/v1/games/list?gamesIdList=1&gamesIdList=2 |
| recommendations (напрямую) | http://localhost:8082/api/v1/recommendations/list |

Остановка: `docker compose down`.

## Проверка функционала

1. **Рекомендации.** Запросите список рекомендованных игр через main_service:

```powershell
curl http://localhost:8080/api/v1/service/games-recommendation
```

Сервис recommendations отвечает ошибкой 500 на запросы с номерами 1, 4, 7 и т.д., ошибкой 400 на запросы 3, 6, 9 и т.д. Повторные попытки и circuit breaker в main_service скрывают часть этих сбоев, поэтому часть запросов будет успешной даже при нестабильных зависимостях.

2. **Отказоустойчивость.** Проверьте состояние circuit breaker:

```powershell
curl http://localhost:8080/actuator/circuitbreakers
```

3. **Прямые запросы к сервисам.** Список игр по идентификаторам отдаёт game_service. Он отвечает успешно только на первый запрос после старта, все последующие получают ошибку 500, и это ожидаемое поведение демонстрации:

```powershell
curl "http://localhost:8081/api/v1/games/list?gamesIdList=1&gamesIdList=2"
```

4. **Ограничение частоты.** Эндпоинт `/api/v1/service/games-recommendation` ограничен rate limiter'ом: 10 запросов в секунду, сверх лимита сервис возвращает ошибку.

5. **Логи.**

```powershell
docker compose logs -f main-service
```

## API

| Сервис | Маршрут | Описание |
|---|---|---|
| main_service | `GET /api/v1/service/games-recommendation` | Рекомендованные игры (названия) |
| main_service | `GET /api/v1/service/exception` | Искусственная ошибка для проверки обработки исключений |
| game_service | `GET /api/v1/games/list?gamesIdList=` | Названия игр по идентификаторам |
| recommendations | `GET /api/v1/recommendations/list` | Список идентификаторов рекомендованных игр |

## Структура проекта

```
.
├── docker-compose.yml          # game-service, recommendations, main-service
└── src
    ├── game_service            # список игр по идентификаторам (Spring Web)
    │   ├── Dockerfile
    │   └── src/main/java/games
    ├── main_service            # оркестрация вызовов, Resilience4j, кэш, rate limiter
    │   ├── Dockerfile
    │   ├── load_test_config    # JMeter-сценарий нагрузочного теста
    │   └── src/main/java/app
    └── recommendations         # список рекомендованных игр (Spring Web)
        ├── Dockerfile
        └── src/main/java/recomendations
```

Поток запроса: `клиент → main_service → recommendations → main_service → game_service → клиент`.

## Особенности и ограничения

- Сбои в game_service и recommendations намеренные: это демонстрация, а не продуктовая логика.
- Адреса внутренних сервисов в Docker задаются переменными окружения `REST_CLIENT_BASE_URI_GAME` и `REST_CLIENT_BASE_URI_RECOMMENDATIONS` в `docker-compose.yml`. Вне Docker используются значения из `application.properties` (`127.0.0.1`).
- Внутри compose сервисы обращаются друг к другу по именам `game-service` и `recommendations`.
- Базы данных в проекте нет, состояние хранится только в памяти и в кэше Caffeine.
- Образы собираются многоступенчато: сборка Gradle в `eclipse-temurin:21-jdk`, запуск в `eclipse-temurin:21-jre` от непривилегированного пользователя.
