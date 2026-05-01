package ge.tsepesh.motorental.repository;

import ge.tsepesh.motorental.model.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BannerRepository extends JpaRepository<Banner, Integer> {

    /**
     * Найти все включенные баннеры
     */
    @Query("SELECT b FROM Banner b WHERE b.enabled = true ORDER BY b.createdAt DESC")
    List<Banner> findAllEnabledBanners();

    /**
     * Найти активный баннер (первый включенный)
     */
    @Query("SELECT b FROM Banner b WHERE b.enabled = true ORDER BY b.createdAt DESC")
    Optional<Banner> findActiveBanner();

    /**
     * Проверить, существует ли включенный баннер
     */
    @Query("SELECT COUNT(b) > 0 FROM Banner b WHERE b.enabled = true")
    boolean existsEnabledBanner();

    /**
     * Выключить все баннеры
     */
    @Modifying
    @Query("UPDATE Banner b SET b.enabled = false, b.updatedAt = CURRENT_TIMESTAMP")
    void disableAllBanners();

    /**
     * Выключить все баннеры кроме указанного
     */
    @Modifying
    @Query("UPDATE Banner b SET b.enabled = false, b.updatedAt = CURRENT_TIMESTAMP WHERE b.id != :bannerId")
    void disableAllBannersExcept(@Param("bannerId") Integer bannerId);

    /**
     * Найти все баннеры, отсортированные по дате создания (новые первыми)
     */
    @Query("SELECT b FROM Banner b ORDER BY b.enabled DESC, b.createdAt DESC")
    List<Banner> findAllOrderByEnabledAndCreatedAt();

    /**
     * Найти баннеры по ID маршрута
     */
    @Query("SELECT b FROM Banner b WHERE b.route.id = :routeId ORDER BY b.createdAt DESC")
    List<Banner> findByRouteId(@Param("routeId") Integer routeId);

    /**
     * Подсчитать количество активных баннеров
     */
    @Query("SELECT COUNT(b) FROM Banner b WHERE b.enabled = true")
    long countEnabledBanners();
}