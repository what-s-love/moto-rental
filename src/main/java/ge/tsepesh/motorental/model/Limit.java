package ge.tsepesh.motorental.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "limits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Limit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "height_min", nullable = false)
    private Integer heightMin;

    @Column(name = "height_max", nullable = false)
    private Integer heightMax;

    @Column(name = "age_min", nullable = false)
    private Integer ageMin;

    @Column(name = "only_men", nullable = false)
    private Boolean onlyMen = false;
}