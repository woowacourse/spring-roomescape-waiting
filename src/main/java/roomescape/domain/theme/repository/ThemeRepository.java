package roomescape.domain.theme.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import roomescape.domain.theme.error.type.ThemeErrorType;
import roomescape.domain.theme.entity.Theme;
import roomescape.global.error.exception.GeneralException;

@Repository
public interface ThemeRepository extends JpaRepository<Theme, Long> {

    List<Theme> findAllByDeletedAtIsNull();

    Theme save(Theme theme);

    default void deleteThemeById(Long id) {
        int updatedRowCount = softDeleteById(id);
        if (updatedRowCount == 0) {
            throw new GeneralException(ThemeErrorType.THEME_NOT_FOUND);
        }
    }

    @Modifying
    @Query("""
        UPDATE Theme t
        SET t.deletedAt = CURRENT_TIMESTAMP
        WHERE t.id = :id
          AND t.deletedAt IS NULL
        """)
    int softDeleteById(@Param("id") Long id);

    Optional<Theme> findThemeByIdAndDeletedAtIsNull(Long id);

    boolean existsThemeByIdAndDeletedAtIsNull(Long id);

    boolean existsThemeByNameAndDeletedAtIsNull(String name);

    @Query("""
        SELECT t
        FROM Reservation r
        JOIN r.theme t
        JOIN r.time rt
        WHERE r.date BETWEEN :startDate AND :endDate
          AND r.status = roomescape.domain.reservation.entity.ReservationStatus.ACTIVE
          AND r.deletedAt IS NULL
          AND t.deletedAt IS NULL
          AND rt.deletedAt IS NULL
        GROUP BY t
        ORDER BY COUNT(r.id) DESC, t.id ASC
        """)
    List<Theme> findPopularThemesDateBetween(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        Pageable pageable
    );
}
