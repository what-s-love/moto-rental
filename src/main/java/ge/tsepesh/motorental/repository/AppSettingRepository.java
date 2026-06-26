package ge.tsepesh.motorental.repository;

import ge.tsepesh.motorental.model.AppSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AppSettingRepository extends JpaRepository<AppSetting, Integer> {
    @Query("select a from AppSetting a where a.key = ?1")
    Optional<AppSetting> findByKey(String key);
}
