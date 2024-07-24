Приложение из учебника «Android. Программирование для профессионалов» Big Nerd Ranch 4-е издание.

Темы: Retrofit, API, GSON, фоновые задачи и потоки, RecyclerView, Looper, Handler, HandlerThread, привязка к жизненному циклу фрагмента/представления фрагмента, сообщения и обработчики сообщений, SearchView, SharedPreferences, WorkManager, Worker, уведомления, широковещательные интенты, WebView.

Представляет собой приложение-клиент Flickr по Flickr API. Загружает и отображает интересные общедоступные фото в списке. Есть возможность поиска фото по запросу, включения / отключения фоновой работы по проверке наличия новых фото, перехода на фото с WebView.

Выполненные задания: 

- Добавить GSON десериалайзер модели фото

- Добавить слушатель для динамического определения количества столбцов в зависимости от размера экрана

- Добавить наблюдателя жизненного цикла представления активности

- Изменить наблюдателя жизненного цикла фрагмента ThumbnailDownloader так, чтобы он сам удалялся в качестве наблюдателя при выходе из приложения

- Реализовать кеш для фото, осуществить предварительную загрузку 10 предшествующих и 10 следующих элементов

- Скрыть клавиатуру после поискового запроса, очистить представление и отобразить индикатор загрузки перед загрузкой и убрать его после завершения

- Добавить способ отображения через пользовательские вкладки Chrome

- Реализовать переходы назад по вкладкам в WebView