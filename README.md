# Delivery Dispatch Core

Backend-система для розподілу замовлень між кур'єрами в реальному часі. Система знаходить найкращого доступного кур'єра для кожного замовлення на основі відстані, типу транспорту та навантаження.

## Технології

- Java 21
- Spring Boot 3.4.2
- Maven
- SpringDoc OpenAPI (Swagger UI)
- In-memory storage (ConcurrentHashMap)

## Вимоги

- **Java 21+** (перевірити: `java -version`)
- **Maven 3.9+** або використовувати вбудований `mvnw`

## Запуск

### Linux / macOS

```bash
chmod +x run.sh
./run.sh          # запуск сервера
./run.sh build    # збірка JAR
./run.sh test     # запуск тестів
./run.sh jar      # запуск з JAR-файлу
```

### Windows

```cmd
run.bat            REM запуск сервера
run.bat build      REM збірка JAR
run.bat test       REM запуск тестів
run.bat jar        REM запуск з JAR-файлу
```

### Напряму через Maven

```bash
./mvnw spring-boot:run     # запуск
./mvnw test                # тести
./mvnw clean package       # збірка
```

Після запуску сервер доступний на `http://localhost:8080`.

## API

Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

### Ендпоінти

| Метод   | URL                              | Опис                           |
|---------|----------------------------------|--------------------------------|
| `POST`  | `/api/orders`                    | Створити замовлення            |
| `GET`   | `/api/orders/{id}`               | Отримати замовлення за ID      |
| `PATCH` | `/api/orders/{id}/complete`      | Завершити замовлення           |
| `GET`   | `/api/couriers/free`             | Список вільних кур'єрів        |
| `PATCH` | `/api/couriers/{id}/location`    | Оновити локацію кур'єра        |
| `GET`   | `/api/dispatch/stats`            | Статистика системи             |

### Приклади запитів

**Створити замовлення:**
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "pickupLocation": {"x": 10, "y": 20},
    "deliveryLocation": {"x": 80, "y": 90},
    "priority": 5,
    "weightKg": 3.0
  }'
```

**Подивитись вільних кур'єрів:**
```bash
curl http://localhost:8080/api/couriers/free
```

**Завершити замовлення:**
```bash
curl -X PATCH http://localhost:8080/api/orders/{id}/complete
```

**Статистика:**
```bash
curl http://localhost:8080/api/dispatch/stats
```

## Алгоритм розподілу

### Формула оцінки

```
Score = (Distance * TransportWeight) - (OrderPriority * 0.5)
```

Найнижчий score перемагає.

### Ваги транспорту

| Тип         | Вага | Макс. вантаж |
|-------------|------|--------------|
| PEDESTRIAN  | 1.5  | 5 кг         |
| BICYCLE     | 1.0  | 15 кг        |
| CAR         | 0.7  | 50 кг        |

### Тайбрейкер

Коли різниця відстані між двома кур'єрами менше 1 одиниці, перевага надається кур'єру з меншою кількістю завершених замовлень за день (`completedOrdersToday`).

### Черга замовлень

Якщо немає доступного кур'єра, замовлення потрапляє в чергу зі статусом `QUEUED`. Коли кур'єр звільняється (через завершення замовлення), система автоматично призначає чергові замовлення.

## Статуси замовлення

```
CREATED -> SEARCHING -> ASSIGNED -> COMPLETED
                    \-> QUEUED -> ASSIGNED -> COMPLETED
```

## Координатна система

Всі точки знаходяться на 2D сітці з координатами `[0, 100]`. Відстань обчислюється як евклідова.

## Структура проєкту

```
src/main/java/com/glovo/delivery/
  config/            - конфігурація (OpenAPI, початкові дані)
  controller/        - REST контролери
  dto/               - об'єкти запитів/відповідей
  exception/         - обробка помилок
  model/             - доменні моделі (Order, Courier, Point)
  repository/        - in-memory сховища
  service/           - бізнес-логіка та диспетчеризація
    strategy/        - стратегія підбору кур'єра
```

## Тести

140 юніт-тестів покривають моделі, репозиторії, сервіси та контролери:

```bash
./mvnw test
```
