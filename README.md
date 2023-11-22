# Неизменяемые структуры данных (Persistent Data Structures)

## Команда
Нуштаев Юрий Юрьевич 23225.2

Колочкин Денис Павлович 23224

## Постановка задачи
Реализовать библиотеку со следующими структурами данных в persistent-вариантах. Соблюсти единое API для всех структур.

## Базовые требования
- Массив (константное время доступа, переменная длина)
- Двусвязный список
- Ассоциативный массив (на основе Hash-таблицы, либо бинарного дерева)

## Дополнительные требования
- Обеспечить произвольную вложенность данных (по аналогии с динамическими языками), не отказываясь при этом полностью от типизации посредством generic/template.
- Реализовать универсальный undo-redo механизм для перечисленных структур с поддержкой каскадности (для вложенных структур)
- Реализовать более эффективное по скорости доступа представление структур данных, чем fat-node (path-copying с В-деревьями)

### Задачи, если останется время
- Расширить экономичное использование памяти на операцию преобразования одной структуры к другой (например, списка в массив)
- Реализовать поддержку транзакционной памяти (STM)

## Предполагаемый путь решения
Поиск и изучение алгоритмов и публикаций по Persistent Data Structures.

Реализация структур в виде библиотеки на Java 17 SE с использованием Maven и Junit 5. Написание unit тестов для тестирования каждой структуры и добавленной функциональности.

| Срок       | Задачи                                                                                                                                                                                                                                                                                                                               | Артефакт                                                                                                                 |
|------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------|
| 24.11.2023 | Погружение в область, поиск и изучение алгоритмов и публикаций по теме (вместе). Драфт проекта, инфраструктура для разработки, тестирования и сборки (вместе)                                                                                                                                                                        | Репозиторий на GitHub с заготовкой проекта. Краткие обзоры алгоритмов и структур.                                        |
| 08.12.2023 | Реализация базовых требований с unit тестами для каждой структуры - Массив (Юрий), Двусвязный список (Денис), Ассоциативный массив (Юрий)                                                                                                                                                                                            | Готовый к сборке проект. Набор тестов, демонстрирующий функциональность библиотеки                                       |
| 22.12.2023 | Реализация дополнительных требований с unit тестами каждой фичи - Обеспечить произвольную вложенность данных (Денис), Реализовать универсальный undo-redo механизм для перечисленных структур с поддержкой каскадности (Юрий), Реализовать более эффективное по скорости доступа представление структур данных, чем fat-node (Денис) | Готовый к сборке проект. Набор тестов, демонстрирующий функциональность библиотеки. Готовые сценарии для демонстрации.   |


## Основные понятия

**Персистентные структуры данных** (*persistent data structure*) — структуры данных, которые при внесении в них каких-то изменений сохраняют все свои предыдущие состояния и доступ к этим состояниям.

**Уровни персистентности**

- Частичная (partial)
- Полная (full)
- Конфлюэнтная (confluent)
- Функциональная (functional)

В частично персистентных структурах данных к каждой версии можно делать запросы, но изменять можно только последнюю версию структуры данных.

В полностью персистентных структурах данных можно менять не только последнюю, но и любую версию структур данных, также к любой версии можно делать запросы.

Конфлюэнтные структуры данных позволяют объединять две структуры данных в одну (деревья поиска, которые можно сливать).

Функциональные структуры данных полностью персистентны по определению, так как в них запрещаются уничтожающие присваивания, т.е. любой переменной значение может быть присвоено только один раз и изменять значения переменных нельзя. 

**В нашем случае реализуем частичную персистентность**

### Преобразование структур данных в персистентные

Способов преобразования:

- Полное копирование (full copy) <br> Когда при любой операции изменения полностью копируется структура данных и в получившуюся новую копию вносятся изменения
- Копирование пути (path copying)
- Метод «толстых» узлов (fat node)

## Обзор алгоритмов

Для реализации требования **реализовать более эффективное по скорости доступа представление структур данных, чем fat-node**, реализуем алгоритм **path coping** с применением B-деревьев.

### Path copying

При использовании **Path copying** копируется только путь до изменяемой вершины и сохраняется новое значение вершины. <br><br>
Таким образом при каждом изменении создается новый корень для новой версии структуры.

**Асимптотика**

- Поиск: *O(log n)* за счет использования дерева
- Изменение: *O(log n)* за счет использования дерева
- Вставка: *O(1)* так как можем преставить дерево в виде списка 