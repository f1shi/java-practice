# Практическая работа №9. Коллекции Java и операции над объектами предметной области

## Запуск

```bash
javac -encoding UTF-8 -d out src/*.java
java -cp out WarehouseApp   # сценарий data/sample.csv
java -cp out SelfCheck
```

## Индивидуальный вариант

**Вариант 3: правило списания, модификатор 20.** Номер варианта: (58 mod 8) + 1 = 3.
Реализованное правило: за **одну операцию выдачи (списания)** можно списать **не более 20 единиц** позиции. 20 допустимо, 21 — `IllegalArgumentException`, остаток не меняется, операция не попадает в журнал. Больший объём оформляется несколькими операциями, и каждая видна в журнале отдельно. Приход не ограничен. Базовое правило «остаток не может стать отрицательным» действует независимо.
Параметр: `Warehouse.MAX_ISSUE_PER_OPERATION`, проверка — в `Warehouse.issue`.

## Выбор коллекций

| Данные | Коллекция | Почему |
|---|---|---|
| Карточки позиций | `Map<String, StockItem>` (`LinkedHashMap`) | Доступ по уникальному инвентарному коду за O(1). Уникальность гарантирует сама структура (`putIfAbsent`). `LinkedHashMap` сохраняет порядок регистрации для стабильного вывода. |
| Журнал операций | `List<StockMovement>` (`ArrayList`) | Операции упорядочены во времени, могут повторяться, к ним обращаются последовательно. |
| Сводка по категориям | `Map<Category, Integer>` (`EnumMap`) | Ключ — enum. `EnumMap` компактна и выдаёт категории в порядке объявления. |
| Перечень используемых категорий | `Set<Category>` (`EnumSet`) | Нужен уникальный перечень без повторов. |

**Когда `List` хуже `Map` в этом кейсе.** Каждая операция `receive`/`issue` начинается с поиска позиции по коду. В `List` это линейный перебор O(n), а уникальность кода пришлось бы проверять тем же перебором перед каждой вставкой. Ошибка в этой проверке даст две карточки с одним кодом и «размазанный» остаток. `Map` решает обе задачи одной операцией.

## Activity-диаграмма (итоговая)

```mermaid
flowchart TD
    S([Начало]) --> IN[/Получить складскую операцию: код, тип, количество/]
    IN --> F["Найти позицию по коду: items.get(code)"]
    F --> EX{Позиция существует?}
    EX -- нет --> ERR[Вернуть ошибку] --> E([Завершение])
    EX -- да --> A1["A1: количество > 0;<br/>для ISSUE: количество ≤ MAX_ISSUE_PER_OPERATION (20) и количество ≤ остаток"]
    A1 -- ошибка --> ERR
    A1 -- допустимо --> UPD["Обновить остаток: increase / decrease"]
    UPD --> MOV["Добавить запись движения: movements.add(new StockMovement(...))"]
    MOV --> A2["A2: сводка по категории актуальна автоматически —<br/>totalsByCategory() пересчитывает EnumMap по текущим карточкам при запросе"]
    A2 --> E
```

Блок A2 сделан **вычисляемым**, а не отдельно хранимым агрегатом. Так сводка не может разойтись с карточками, если какую-то операцию забыли учесть. На диаграмме это отражено формулировкой A2.

## Диаграмма классов (итоговая)

```mermaid
classDiagram
    class Warehouse {
        +MAX_ISSUE_PER_OPERATION: int = 20
        -items: Map~String, StockItem~
        -movements: List~StockMovement~
        +register(StockItem)
        +receive(String, int)
        +issue(String, int)
        +find(String) StockItem
        +findByCategory(Category) List~StockItem~
        +totalsByCategory() Map~Category, Integer~
        +usedCategories() Set~Category~
        +getMovements() List~StockMovement~
    }
    class StockItem {
        -code: String
        -name: String
        -category: Category
        -quantity: int
        ~increase(int)
        ~decrease(int)
        +equals(Object) boolean
        +hashCode() int
    }
    class StockMovement {
        <<record>>
        itemCode: String
        type: MovementType
        quantity: int
    }
    class Category {
        <<enumeration>>
        LAPTOP
        MONITOR
        PERIPHERAL
        NETWORK
        FURNITURE
    }
    class MovementType {
        <<enumeration>>
        RECEIPT
        ISSUE
    }
    class WarehouseApp
    Warehouse *-- "0..*" StockItem
    Warehouse *-- "0..*" StockMovement
    StockItem --> Category
    StockMovement --> MovementType
    WarehouseApp ..> Warehouse
```

`equals`/`hashCode` у `StockItem` определены по инвентарному коду: две карточки с одним кодом — один и тот же объект учёта.

## Анализ фрагмента кода

```java
for (StockItem x : items) { if (x.getCode().equals(code)) return x; } // при наличии Map поиск линейным циклом избыточен
```

Ошибка: склад уже хранит позиции в `Map<String, StockItem>` с ключом-кодом, а фрагмент ищет перебором. Он тратит O(n) вместо O(1), дублирует назначение ключа Map и молча возвращает «первую попавшуюся», если коллекция когда-нибудь допустит дубликаты.
Исправление: `Warehouse.find(code)` — это `items.get(code.trim())` плюс понятная ошибка, если позиции нет.

## Граничные проверки (результат SelfCheck: 26/26 OK)

| Проверка | Ожидание |
|---|---|
| Типовая выдача 25 − 5 | остаток 20 |
| Граница списания (вариант 3): выдача 20 / 21 за раз | OK / IllegalArgumentException, остаток не изменился |
| Выдача больше остатка, выдача при нулевом остатке | IllegalStateException |
| Нижняя граница количества: 0, −3 | IllegalArgumentException |
| Пустые значения: код `null`, пустой код, категория `null` | контролируемая ошибка |
| Неверный формат: категория `KITCHEN`, количество `abc` | контролируемая ошибка |
| Повтор: повторный инвентарный код | отказ, исходная карточка сохранена |
| Изоляция: `clear()` результата поиска, изменение сводки, `getMovements().clear()` | склад не меняется / UnsupportedOperationException |

## Собственные формулировки

**Какая проверка защищает от наиболее вероятной ошибки и где она.** Чаще всего пытаются выдать больше, чем есть. `StockItem.decrease` сравнивает запрошенное количество с остатком **до** изменения поля, поэтому остаток никогда не становится отрицательным. Лимит списания за одну операцию проверяется в `Warehouse.issue` ещё раньше. Неуспешная операция не попадает в журнал.

**Где ключевое бизнес-правило и почему.** Уникальность кода находится в `Warehouse.register` (`putIfAbsent`), потому что только склад видит все карточки. Правило неотрицательного остатка — в `StockItem.decrease`, потому что остаток — поле карточки. Лимит списания — в `Warehouse.MAX_ISSUE_PER_OPERATION` и `Warehouse.issue`: это политика склада для операции, а не свойство отдельной позиции.

**Как Activity-диаграмма отражает код.** «Найти позицию по коду» и ромб «Позиция существует» — это `Warehouse.find`. A1 — это проверка лимита в `Warehouse.issue`, а также `requirePositive` и сравнение с остатком в `StockItem.decrease`. «Добавить запись движения» — `movements.add(...)` в `issue`/`receive`, он выполняется только после успешного изменения остатка.

**Какое изменение затронет минимум классов.** Изменение лимита списания — одна константа. Новая категория — одна строка в `enum Category`: `EnumMap`/`EnumSet` подхватят её автоматически.
