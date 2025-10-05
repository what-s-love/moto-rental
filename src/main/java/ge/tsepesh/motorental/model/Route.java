package ge.tsepesh.motorental.model;

import ge.tsepesh.motorental.enums.Difficulty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "routes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer distance;

    @Column(nullable = false)
    private Integer duration;

    @Column(nullable = false)
    private Integer difficulty;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column
    private String description;

    @Column
    private String mapPath;

    @Column(nullable = false)
    private Boolean isSpecial;

    @Column(nullable = false)
    private Boolean enabled;
}