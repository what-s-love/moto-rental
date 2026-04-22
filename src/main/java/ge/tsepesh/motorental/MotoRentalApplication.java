package ge.tsepesh.motorental;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MotoRentalApplication {

	public static void main(String[] args) {
		SpringApplication.run(MotoRentalApplication.class, args);
	}

}
//ToDo Доделать сворачивание admin-sidebar на узких экранах
//ToDo Доделать стилизацию карточек на странице списка маршрутов в Админке
//ToDo Доделать на всех страницах отображение ошибок как на /admin/routes/list (bootstrap alert)
//ToDo Проверить функционал остановки карусели при наведении курсора, чтобы можно было читать описания
//ToDo Добавить 2 планировщика, которые в нужную дату будут менять значение enable летних/зимних смен
//ToDo Заменить alert информации о бронировании на модальное окно с информацией и кнопкой "Закрыть", если будет плохо выглядеть на смартфоне
//ToDo Убрать возможность открытия модального окна со сменами для минувших дней
//ToDo Исправить обработку ошибки, если бронирование создаётся на сегодня (или не позволять открывать страницу создания бронирования на сегодня)
//ToDo Исправить обработку создания бронирования: если почтовый сервис недоступен возвращается ошибка
//ToDo Вынести весь текст сайта в переменные и занести соответствующий текст в messages.properties и messages_en.properties
//ToDo Добавить возможность отменить бронирование по ссылке из письма???
//ToDo Добавить Telegram-бота
//ToDo Добавить функционал отправки уведомлений в телеграм-бота о бронированиях и оплатах