Приложение из учебника «Android. Программирование для профессионалов» Big Nerd Ranch 4-е издание.

Темы: фрагменты, жизненный цикл фрагмента, ViewModel, RecyclerView, ViewHolder, ConstraintLayout, Room, потоки приложения, синглтоны, навигация по фрагментам, аргументы фрагментов, DialogFragment, панель приложения, неявные интенты, хранилище файлов, локализация, спец. возможности, LiveData.

Представляет собой приложение для хранения информации об «Офисных преступлениях». На главной странице находится список преступлений, где есть возможность добавить новое преступление или перейти на отдельное преступление.

Каждое преступление состоит из заголовка, даты, времени и фотографии. Можно изменить данные преступления, добавить подозреваемого из списка контактов, позвонить подозреваемому или отправить отчет по преступлению.

Выполненные задания: 

- Добавить свойство "Требуется полиция", изменять представление в зависимости от значения

- Добавить местоположение схемы БД для Room

- Изменить RecycleView.Adapter на ListAdapter, подключить определение разницы между текущим и новым набором данных, чтобы при обновлении преступлений в списке перерисовывались только измененные строки

- Изменить переопределение адаптера на отправку измененных данных

- Добавить фрагмент для изменения времени преступления, разделить дату и время

- Добавить текст и кнопку для отображения при отсутствии элементов в бд

- Добавить возможность позвонить подозреваемому

- Добавить фрагмент для отображения увеличенной фотографии преступления

- Зарегистрировать слушателя обработки макета, чтобы масштабировать фотографию каждый раз после обработки макета

- Добавить описание кнопкам дат и выбора подозреваемого для поддержки TalkBack