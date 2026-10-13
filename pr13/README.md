# Практическая работа №13. Date Time API и проверка пересечения интервалов

## Запуск

```bash
javac -encoding UTF-8 -d out src/*.java
java -cp out BookingApp   # заявки из data/sample.csv (room;start;end), clockNow = 2026-10-20T09:00
java -cp out SelfCheck
```

## Индивидуальный вариант

**Вариант 1: максимальная длительность = 10 часов** (базовое правило — 8 ч). Правило: бронирование длится от 15 минут до 10 часов **включительно**. 600 минут допустимо, 601 — нет.
Параметр: `BookingService.MAX_DURATION`.

**Буфер между бронированиями** (шаг 4) реализован как параметр конструктора `BookingService(repository, buffer)`. По умолчанию буфер нулевой, поэтому выполняется базовое правило «стык не является конфликтом». С буфером 10 минут следующее бронирование может начаться не раньше чем через 10 минут после окончания предыдущего (проверено в SelfCheck). Если вариант предусматривает буфер, меняется только значение при создании сервиса.

## Правила интервала

* Бронирование — полуинтервал `[start, end)`: `end` строго позже `start`.
* Конфликт: `startA < endB && startB < endA`. При `endA == startB` первое условие ложно, значит стык — не конфликт.
* С буфером: `start < existing.end + buffer && existing.start − buffer < end`.
* Нельзя начать раньше переданного `clockNow`. Начало ровно в `clockNow` допустимо.

## Почему сравнение только часов и минут без даты ошибочно

Бронирования разных дней в одно и то же время (10:00–11:00 сегодня и 10:00–11:00 завтра) при сравнении `LocalTime` выглядят как конфликт, хотя не пересекаются. Бронирование через полночь 23:00–01:00 при сравнении `LocalTime` получает `end < start` и отклоняется как некорректное или ломает формулу пересечения. Поэтому везде используется `LocalDateTime`: дата и время сравниваются вместе, а переход через полночь становится обычным интервалом длиной 120 минут (проверено в SelfCheck и `sample.csv`, строки 9–10).

## Activity-диаграмма (итоговая)

```mermaid
flowchart TD
    S([Начало]) --> IN[/Получить комнату и интервал start, end, clockNow/]
    IN --> A1["A1: validateInterval: все значения заданы; end строго после start;<br/>start не раньше clockNow; 15 мин ≤ Duration.between(start, end) ≤ 10 ч"]
    A1 -- ошибка --> ERR[IllegalArgumentException с причиной] --> E([Завершение])
    A1 -- корректно --> F["Найти существующие бронирования комнаты: repository.findByRoom(roomId)"]
    F --> O["Проверить пересечение с каждым интервалом: start < b.end + buffer && b.start − buffer < end"]
    O --> D{Есть конфликт?}
    D -- нет --> C["Создать Booking и сохранить: repository.save"] --> E
    D -- да --> X["Вернуть конфликт с id бронирования: BookingConflictException"] --> E
```

## Диаграмма классов (итоговая)

```mermaid
classDiagram
    class BookingService {
        +MIN_DURATION: Duration = 15 мин
        +MAX_DURATION: Duration = 10 ч
        -repository: BookingRepository
        -buffer: Duration
        +BookingService(BookingRepository)
        +BookingService(BookingRepository, Duration)
        +book(roomId, start, end, clockNow) Booking
        +isAvailable(roomId, start, end) boolean
        +findConflict(roomId, start, end) Optional~Booking~
        -overlaps(Booking, start, end) boolean
        ~validateInterval(start, end, clockNow)$
    }
    class BookingRepository {
        -bookings: List~Booking~
        +save(Booking)
        +findByRoom(String) List~Booking~
    }
    class Booking {
        -id: String
        -roomId: String
        -start: LocalDateTime
        -end: LocalDateTime
        +duration() Duration
    }
    class BookingConflictException {
        -conflictingBookingId: String
    }
    class BookingApp
    BookingService o-- "1" BookingRepository
    BookingRepository o-- "0..*" Booking
    BookingService ..> BookingConflictException : throws
    BookingApp ..> BookingService
```

## Анализ фрагмента кода

```java
boolean conflict = newStart.isBefore(oldEnd) || newEnd.isAfter(oldStart); // условие слишком широкое
```

Ошибка: используется `||` вместо `&&`. Почти любой интервал удовлетворяет хотя бы одной половине условия. Например, бронирование на завтра с 10:00 (старое — сегодня 10:00–11:00): `newEnd.isAfter(oldStart)` истинно, значит «конфликт». Бронирование на вчера: истинна первая половина. В итоге комната занята навсегда после первого бронирования.
Исправление: `start.isBefore(existing.getEnd()) && existing.getStart().isBefore(end)` в методе `overlaps`. Оба условия должны выполняться одновременно, и строгие сравнения делают стык неконфликтным.

## Граничные проверки (результат SelfCheck: 27/27 OK)

| Проверка | Ожидание |
|---|---|
| Типовое: 10:00–11:00 | создано, 60 мин |
| Стык 11:00–12:00 / 09:00–10:00 | не конфликт |
| Частичное, вложенное, охватывающее, совпадающее пересечение | конфликт с id существующего |
| Нижняя граница длительности: 14 / 15 мин | ошибка / OK |
| Верхняя граница: 10 ч / 10 ч 1 мин | OK / ошибка |
| `end == start`, `end < start`, начало в прошлом / ровно `clockNow` | ошибка / OK |
| Пустое значение (`null`) и неверный формат `20.10.2026 10:00` | контролируемая ошибка |
| Переход через полночь 23:00–01:00 и пересечение 00:30–02:00 | OK / конфликт |
| Буфер 10 мин: 11:05 / 11:10 | конфликт / OK |
| `isAvailable` не меняет репозиторий | количество записей не изменилось |

## Собственные формулировки

**Какая проверка защищает от наиболее вероятной ошибки и где она.** Чаще всего путают местами начало и конец или указывают нулевую длительность. `validateInterval` первым делом проверяет `!end.isAfter(start)`, затем прошлое и длительность. Проверка выполняется до поиска конфликтов, поэтому некорректный интервал не участвует в сравнении.

**Где ключевое бизнес-правило и почему.** Формула пересечения находится в единственном методе `BookingService.overlaps`. Её используют и `isAvailable`, и `book`, поэтому проверка доступности и создание бронирования не могут разойтись в трактовке стыка или буфера.

**Как Activity-диаграмма отражает код.** A1 — `validateInterval(start, end, clockNow)`. «Найти существующие бронирования» — `repository.findByRoom(roomId)` в `findConflict`. «Проверить пересечение с каждым» — цикл `for` с вызовом `overlaps`. Ветка «да» — `throw new BookingConflictException(conflict.get())`, ветка «нет» — `new Booking(...)` и `repository.save`.

**Какое изменение затронет минимум классов.** Изменение максимальной или минимальной длительности — одна константа. Изменение буфера — один аргумент конструктора, формула в `overlaps` уже его учитывает.
