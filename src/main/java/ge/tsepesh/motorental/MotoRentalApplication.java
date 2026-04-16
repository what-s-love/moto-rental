package ge.tsepesh.motorental;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MotoRentalApplication {

	public static void main(String[] args) {
		SpringApplication.run(MotoRentalApplication.class, args);
	}

}
//ToDo Добавить Boolean enabled поле в Shift для разделения на лето/зиму
//ToDo Добавить страницы админки, не забыть про admin-sidebar
//ToDo Добавить 2 планировщика, которые в нужную дату будут менять значение enable вечерней смены
//ToDo Заменить alert информации о бронировании на модальное окно с информацией и кнопкой "Закрыть", если будет плохо выглядеть на смартфоне
//ToDo Убрать возможность открытия модального окна со сменами для минувших дней
//ToDo Исправить обработку ошибки, если бронирование создаётся на сегодня (или не позволять открывать страницу создания бронирования на сегодня)
//ToDo Вынести весь текст сайта в переменные и занести соответствующий текст в messages.properties и messages_en.properties
//ToDo Добавить Telegram-бота
//ToDo Добавить функционал отправки уведомлений в телеграм-бота о бронированиях и оплатах