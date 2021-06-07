# SMS-Gateway

SMS-Gateway adalah server untuk mengirimkan SMS melalui protokol HTTP, WebSocket dan Message Broker. Pengguna dapat memasang SMS-Gateway pada server dengan IP address statis yang diapat diakses oleh klien yang akan mengirimkan SMS. Selain itu, pengguna juga dapat memasang SMS-Gateway pada server dengan IP address dinamis. Server ini kemudian mengakses sebuah server websocket atau server RabbitMQ. SMS-Gateway bertindak sebagai consumer yang akan mengirimkan semua SMS yang diterimanya.

Baik WebSocket maupun RabitMQ menggunakan sebuah channel yang dapat diseting dari kedua sisi (pengirim dan penerima).
