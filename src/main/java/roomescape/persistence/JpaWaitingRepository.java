package roomescape.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.Waiting;

import java.time.LocalDate;
import java.util.List;

public interface JpaWaitingRepository extends JpaRepository<Waiting, Long> {

    List<Waiting> findByThemeIdAndDateAndTimeId(Long themeId, LocalDate date, Long reservationTimeId);

    @Query(
            """
            SELECT COALESCE(MAX(w.order), 0)
            FROM Waiting w
            WHERE w.theme.id = :themeId
                AND w.date = :date
                AND w.time.id = :reservationTimeId
            """
    )
    int findMaxOrderByThemeIdAndDateAndTimeId(Long themeId, LocalDate date, Long reservationTimeId);
}
