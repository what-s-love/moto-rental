package ge.tsepesh.motorental.service;

import ge.tsepesh.motorental.model.Booking;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendPaymentLink(Booking booking, String paymentUrl) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(booking.getClient().getEmail());
        message.setSubject("Подтверждение бронирования и оплата");
        message.setText(buildPaymentMessageBody(booking, paymentUrl));

        mailSender.send(message);
        log.info("Payment email sent to {}", booking.getClient().getEmail());
    }

    private String buildPaymentMessageBody(Booking booking, String paymentUrl) {
        return "Здравствуйте, " + booking.getClient().getName() + "!\n\n"
                + "Благодарим за использование нашего сайта!\n"
                + "Ваше бронирование №" + booking.getId() + " успешно создано. Перейдите по ссылке для оплаты:\n"
                + paymentUrl + "\n\n"
                + "Спасибо!";
    }
}