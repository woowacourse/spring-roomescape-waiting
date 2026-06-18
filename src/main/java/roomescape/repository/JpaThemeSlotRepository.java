package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.ThemeSlot;

public interface JpaThemeSlotRepository extends
        JpaRepository<ThemeSlot, Long>,
        ThemeSlotRepository,
        JpaThemeSlotRepositoryCustom {

    @Override
    @Query("""
            SELECT ts
            FROM ThemeSlot ts
            WHERE ts.theme.id = :themeId
            AND ts.date = :date
            """)
    List<ThemeSlot> findByThemeIdAndDate(
            @Param("themeId") long themeId,
            @Param("date") LocalDate date
    );

    @Override
    default void updateReserved(ThemeSlot themeSlot) {
    }
}
