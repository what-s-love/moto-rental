package ge.tsepesh.motorental.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "bikes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Bike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String brand;

    @Column(nullable = false, length = 100)
    private String model;

    @Column(name = "engine_cc", nullable = false)
    private Integer engineCc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "limit_id", nullable = false)
    private Limit limits;

    @Column(length = 500)
    private String photoPath;

    @Column(nullable = false)
    private Boolean enabled;
}