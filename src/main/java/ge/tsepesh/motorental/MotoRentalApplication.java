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
//ToDo Добавить 2 планировщика, которые в нужную дату будут менять значение enable вечерней смены
//ToDo Заменить alert информации о бронировании на модальное окно с информацией и кнопкой "Закрыть", если будет плохо выглядеть на смартфоне
//ToDo Убрать возможность открытия модального окна со сменами для минувших дней