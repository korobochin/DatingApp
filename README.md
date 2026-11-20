# Знакомства: симуляция платформы (вариант 25, этап 2 — механика)

Библиотека классов с механикой социальной платформы знакомств и консольное приложение,
которое прогоняет симуляцию с ботами или даёт человеку играть за один профиль.

* Java 21, Maven, JUnit 5. Сторонних библиотек нет (JSON и Jackson/Gson появятся на этапе 3).
* `dating-core` — вся механика. **Не печатает в консоль** и не знает про `System.out`.
* `dating-cli` — консольный слой: вывод журнала, ввод команд человека.

## Сборка и запуск

```bash
mvn clean verify          # сборка и все тесты
mvn javadoc:javadoc -pl dating-core   # javadoc публичного API
```

Запуск (после `mvn package`; на Windows разделитель в classpath — `;`):

```bash
CP=dating-core/target/classes:dating-cli/target/classes

# бот-симуляция с журналом событий
java -cp $CP dating.cli.Main sim --seed 42 --users 30 --attempts 300
java -cp $CP dating.cli.Main sim --seed 7 --users 25 --target 20 --quiet   # без журнала, только итоги

# человек играет профилем №1, остальные профили ведут боты
java -cp $CP dating.cli.Main play --seed 42 --users 20 --me 1
```

Параметры `sim`: `--seed` (по умолчанию 42), `--users` (30), `--attempts` (300, лимит попыток),
`--target N` (остановиться после N новых дружб), `--quiet`.
Команды режима `play`: `help`, `me`, `match [n]`, `know [n]`, `photos [id]`, `like <photoId>`,
`request <id>`, `accept <id>`, `decline <id>`, `unfriend <id>`,
`requests`, `friends`, `stats`, `pass`, `quit`. После каждого успешного хода человека боты делают один раунд.

## Структура пакетов

| Пакет | Содержимое |
|---|---|
| `model` | данные: `Profile`, `Photo`, `FriendRequest`, `Pair`, `Gender`, `Intent` |
| `graph` | `Graph<V>` |
| `sim` | правила: `Network` (состояние), `Command`, `Event`, `SimulationEngine`, исключение, генератор профилей, статистика |
| `matching` | подбор и рекомендации: `Matcher`, `Recommender`, скореры, компараторы, `ProfileFilters` |
| `bot` | боты и цикл партии `Simulation` |
| `random` | `RandomSource` |

## Модель

| Понятие | Класс | Замечание |
|---|---|---|
| Профиль | `Profile` (record + Builder) | неизменяемый; возраст ≥ 18; интересы нормализуются (trim, lower-case) и хранятся отсортированными |
| Фото | `Photo` | лайки хранятся в состоянии сети, не в фото |
| Граф связей | `Graph<V extends Comparable<V>>` | неориентированный, без петель; все обходы в отсортированном порядке |
| Состояние | `Network` | профили, граф дружбы, фото и лайки, заявки, отклонённые заявки, мэтчи; изменяется только командами |
| Ход | `Command` (sealed) | `LikePhoto`, `SendFriendRequest`, `AcceptFriendRequest`, `DeclineFriendRequest`, `RemoveFriend` |
| Событие | `Event` (sealed) | `PhotoLiked`, `MatchFormed`, `FriendRequestSent`, `FriendRequestDeclined`, `FriendshipCreated`, `FriendshipEnded`, `ActionRejected`, `SimulationEnded` |
| Движок | `SimulationEngine` | `apply` (бросает исключение), `submit` (превращает нарушение в `ActionRejected`), журнал принятых команд, наблюдатели |
| Прогон | `Simulation` | раунды: каждый бот по очереди предлагает ход |

### Правила

* Нельзя лайкать своё фото, лайкать одно фото дважды, ссылаться на несуществующего пользователя или фото.
* Если пользователь A лайкнул фото B, а B уже лайкал фото A, возникает `MatchFormed` (один раз для пары).
* Заявку в друзья нельзя отправить себе, другу, повторно, при встречной ожидающей заявке (её нужно принять) и после того, как адресат её уже отклонял.
* Принять и отклонить можно только существующую заявку. Удалить из друзей можно только друга.
* Нарушение правил — `IllegalActionException`; состояние при этом **не меняется**.
* После `finish` движок не принимает команды (`IllegalStateException`).

### Условия завершения (`EndReason`)

`STEP_LIMIT` — исчерпан лимит попыток; `TARGET_REACHED` — создано заданное число дружб;
`NO_MOVES` — несколько раундов подряд никто не может или не хочет ходить.

### Функционал поиска

* **Подбор** (`Matcher`): ранжирование кандидатов по `MatchScorer` (по умолчанию коэффициент Жаккара интересов), фильтры, лимит. Порядок — композиция компараторов: счёт ↓, общие друзья ↓, общие интересы ↓, id ↑ (`MatchComparators`).
* **Общие знакомые**: `Graph.commonNeighbors`, `Recommender.commonFriends`.
* **«Вы можете знать»** (`Recommender`): друзья друзей, которые ещё не друзья; сортировка по числу общих друзей, затем по id.
* **Фильтры** (`ProfileFilters`): возраст (границы включены), город, интересы, совместимость по ориентации и цели знакомства; компонуются через `and`/`or`/`negate`.
* **Статистика** (`Statistics`): `groupingBy`, `counting`, `averagingInt`, `summarizingInt`, `partitioningBy`, `flatMap`, сортировка записей по значению.

## Паттерны проектирования

| Паттерн | Где |
|---|---|
| Strategy | `MatchScorer`/`MatchScorers` (алгоритмы оценки), `Bot` и его реализации (`ActiveBot`, `PassiveBot`) |
| Command | `Command` с раздельными `validate` и `execute`; журнал = список команд |
| Observer | `EventObserver`, подписка на `SimulationEngine` (журнал в консоли, счётчик дружб в `Simulation`) |
| Builder | `Profile.Builder`, `Network.Builder` (проверяет ссылки между сущностями) |
| Factory | `BotFactory`, `ProfileGenerator` |
| Specification | фильтры как `Predicate<Profile>` (`ProfileFilters`), компонуются через `and`/`or`/`negate` |

## Требования этапа

* **Отделение правил от вывода.** В `dating-core` нет `System.out`. Механика возвращает `List<Event>`; текст делает только `ConsoleRenderer` в `dating-cli`. Тест `gameRunsWithoutAnyConsoleOutput` подменяет `System.out`/`System.err` и прогоняет партию целиком.
* **Воспроизводимость.** Единственный источник случайности — `RandomSource(seed)`; `new Random()` и `Math.random()` не используются. Структуры данных упорядочены (`TreeMap`/`TreeSet`), поэтому итерация не зависит от хэшей. Одинаковый seed даёт побайтово одинаковый журнал и вывод.
* **Задел под этап 3.** Состояние — простые данные с ссылками по `int id` (удобно для JSON); журнал принятых команд уже ведётся (для повтора партии); `Matcher`/`Recommender` только читают `Network`, поэтому параллельный расчёт рекомендаций добавится отдельным классом без переделки.

## Тесты

JUnit 5, 74 сценария в `dating-core/src/test`: граф и алгоритмы на нём, профиль и граничный возраст, скореры, ранжирование и тай-брейки, фильтры, рекомендации, все правила и исключения команд, события и их порядок, журнал, наблюдатели, статистика, воспроизводимость (один seed / разные seed), полная партия из зафиксированного seed 2024 (ожидаемые числа зашиты в тест), инварианты после партии, отсутствие вывода в консоль.
