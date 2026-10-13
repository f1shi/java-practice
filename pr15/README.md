# Практическая работа №15. Базовые паттерны проектирования на прикладной задаче

## Запуск

```bash
javac -encoding UTF-8 -d out src/*.java
java -cp out NotificationApp   # уведомления из data/sample.csv (channel;recipient;message)
java -cp out SelfCheck
```

## Применённые паттерны

| Паттерн / принцип | Где | Что даёт |
|---|---|---|
| **Strategy** | `NotificationChannel` и реализации `EmailChannel`, `SmsChannel`, `InternalChannel`, `PushChannel` | алгоритм отправки и проверки адресата выбирается во время выполнения |
| **Factory (упрощённый Factory Method)** | `ChannelFactory.get(ChannelType)` и `withDefaultChannels()` | выбор реализации в одном месте; фабрика не отправляет сообщения |
| **Dependency Injection через конструктор** | `new NotificationService(ChannelFactory)` | сервис не создаёт каналы сам; в тесте подставляется фабрика с фиктивным каналом |
| **Open/Closed** | новый канал = новый класс | `NotificationService` и существующие каналы не меняются |
| Отказ от `instanceof`/`switch` | `NotificationService.notify` | только полиморфные вызовы `supports` / `send` |

## Индивидуальный вариант

**Вариант 1: новый канал, модификатор 10.** Добавлен канал **PUSH** (`PushChannel`): уведомление на мобильное устройство. Правило адресата: токен устройства из латинских букв и цифр длиной **не менее 10 символов**. 10 символов принимаются, 9 — отказ.
Параметр: `PushChannel.MIN_TOKEN_LENGTH`. Для подключения нужны новый класс, константа `PUSH` в `ChannelType` и одна строка в `ChannelFactory.withDefaultChannels()`. Классы `EmailChannel`, `SmsChannel`, `InternalChannel` и `NotificationService` не изменялись.

## Правила адресатов (валидация внутри своего канала)

| Канал | Правило |
|---|---|
| EMAIL | один `@`, непустое имя, домен с точкой |
| SMS | `+7` и 10 цифр; текст ≤ 160 символов |
| INTERNAL | логин `[a-z0-9._-]{3,32}` |
| PUSH | токен `[A-Za-z0-9]`, длина ≥ 10 |

Общие правила для всех каналов (адресат и текст не пусты, текст ≤ 1000 символов) проверяет `NotificationService` (блок A1).

## Activity-диаграмма (итоговая)

```mermaid
flowchart TD
    S([Начало]) --> IN[/Получить NotificationRequest/]
    IN --> A1["A1: общая проверка запроса в NotificationService:<br/>запрос и канал заданы; адресат не пуст; текст не пуст и ≤ 1000 символов"]
    A1 -- ошибка --> VE
    A1 -- ок --> SEL["Выбрать NotificationChannel: factory.get(request.channel())"]
    SEL --> D{"Канал поддерживает адресата? channel.supports(request)"}
    D -- да --> SEND["Отправить через общий интерфейс: channel.send(request)"]
    SEND --> RES[Зафиксировать NotificationResult.sent] --> E([Завершение])
    D -- нет --> VE["Вернуть ошибку валидации: NotificationResult.failed(channel.rejectionReason)"] --> E
```

## Диаграмма классов (итоговая)

```mermaid
classDiagram
    class NotificationChannel {
        <<interface>>
        +type() ChannelType
        +supports(NotificationRequest) boolean
        +rejectionReason(NotificationRequest) String
        +send(NotificationRequest) NotificationResult
    }
    class EmailChannel
    class SmsChannel {
        ~MAX_LENGTH: int = 160
    }
    class InternalChannel
    class PushChannel {
        ~MIN_TOKEN_LENGTH: int = 10
    }
    class ChannelFactory {
        -channels: Map~ChannelType, NotificationChannel~
        +ChannelFactory(List~NotificationChannel~)
        +withDefaultChannels()$ ChannelFactory
        +get(ChannelType) NotificationChannel
    }
    class NotificationService {
        -factory: ChannelFactory
        +NotificationService(ChannelFactory)
        +notify(NotificationRequest) NotificationResult
    }
    class NotificationRequest {
        <<record>>
        channel: ChannelType
        recipient: String
        message: String
    }
    class NotificationResult {
        <<record>>
        success: boolean
        channel: ChannelType
        recipient: String
        details: String
    }
    class ChannelType {
        <<enumeration>>
        EMAIL
        SMS
        INTERNAL
        PUSH
    }
    NotificationChannel <|.. EmailChannel
    NotificationChannel <|.. SmsChannel
    NotificationChannel <|.. InternalChannel
    NotificationChannel <|.. PushChannel
    NotificationService o-- "1" ChannelFactory
    ChannelFactory ..> NotificationChannel : creates/selects
    NotificationService ..> NotificationResult
```

По сравнению с исходной диаграммой в интерфейс добавлен `rejectionReason(...)`. Каждый канал сам объясняет, почему адресат не подходит, и сервису не нужно знать правила каналов, чтобы сформировать понятный отказ. Также добавлен `type()` для регистрации в фабрике без `switch`.

## Анализ фрагмента кода

```java
switch (type) { case EMAIL -> new EmailSender().send(...); case SMS -> new SmsSender().send(...); }
```

Ошибки:
1. Сервис знает все конкретные классы и сам создаёт их через `new`. Подменить отправителя в тесте нельзя: каждый тест будет «реально» отправлять письма.
2. Новый канал требует правки этого `switch`, а если такие `switch` есть в нескольких местах, то правки каждого из них. Это нарушение Open/Closed.
3. Проверка адресата либо отсутствует, либо будет размазана по веткам того же метода. Канала INTERNAL в `switch` нет — такое сообщение молча не отправится.

Исправление: `NotificationService` получает `ChannelFactory` через конструктор и вызывает `channel.supports(...)`/`channel.send(...)` через интерфейс. В `SelfCheck` сервис проверяется с фиктивным каналом `RecordingChannel`.

## Какие изменения стали локальными

| Изменение | Затрагивает |
|---|---|
| Новый канал (PUSH в варианте) | новый класс + константа enum + строка в `withDefaultChannels` |
| Правило адресата одного канала (например, SMS-лимит 70 символов для кириллицы) | только класс этого канала |
| Общий лимит длины сообщения | только `NotificationService.MAX_MESSAGE_LENGTH` |
| Реальная интеграция с SMTP | только `EmailChannel.send` |
| Тестирование сервиса | фиктивная реализация интерфейса, без изменения production-кода |

## Граничные проверки (результат SelfCheck: 25/25 OK)

| Проверка | Ожидание |
|---|---|
| Типовые: email, sms, internal, push | ОТПРАВЛЕНО |
| Нижние границы: логин 2 символа, токен 9 символов | отказ |
| Верхние границы: SMS 160 / 161 символ, сообщение 1001 символ | OK / отказ / отказ |
| Пустые значения: адресат, текст, канал `null`, запрос `null` | отказ с причиной |
| Неверный формат: email без `@` / без точки / два `@`; номер `8916…`; кириллический логин; `FAX` | отказ / IllegalArgumentException |
| Конфликт: канал зарегистрирован дважды; канал не подключён | IllegalArgumentException |
| Фиктивный канал: `send` вызван ровно один раз; при ошибке A1 не вызван | OK |

## Собственные формулировки

**Какая проверка защищает от наиболее вероятной ошибки и где она.** Чаще всего ошибаются в формате адресата для выбранного канала: телефон с `8`, email без домена. Эту проверку выполняет `supports(...)` в классе конкретного канала. Правило лежит рядом с кодом отправки, которому оно нужно, и не теряется при добавлении каналов.

**Где ключевое бизнес-правило и почему.** Правило «сначала общие проверки, затем проверка канала, затем отправка» находится в `NotificationService.notify`, и это единственная логика сервиса. Правила адресатов — в каналах. Сервис не знает деталей, как требует задание.

**Как Activity-диаграмма отражает код.** A1 — `commonValidationError(request)`. «Выбрать NotificationChannel» — `factory.get(request.channel())`. Ромб — `if (!channel.supports(request))`. Ветка «нет» — `NotificationResult.failed(..., channel.rejectionReason(request))`, ветка «да» — `channel.send(request)`.

**Какое изменение затронет минимум классов.** Изменение правила адресата одного канала — только класс этого канала. Например, длина токена в варианте — константа `PushChannel.MIN_TOKEN_LENGTH`.
