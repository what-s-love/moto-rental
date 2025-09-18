package ge.tsepesh.motorental.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "clients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 20)
    private String phone;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(name = "telegram_id", length = 100)
    private String telegramId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}