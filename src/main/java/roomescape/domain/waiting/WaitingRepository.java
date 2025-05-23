package roomescape.domain.waiting;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    Optional<Waiting> findFirstByDateAndTimeSlotIdAndThemeIdOrderByIdAsc(LocalDate date, long timeSlotId, long themeId);

    @Query("""
                SELECT new roomescape.domain.waiting.WaitingWithRank(
                    w,
                    (
                        SELECT COUNT(w2)
                        FROM Waiting w2
                        WHERE w2.theme = w.theme
                          AND w2.date = w.date
                          AND w2.timeSlot = w.timeSlot
                          AND w2.id < w.id
                    )
                )
                FROM Waiting w
                WHERE w.user.id = :userId
            """)
    List<WaitingWithRank> findWaitingWithRankByUserId(Long userId);

    boolean existsByDateAndTimeSlotIdAndThemeIdAndUserId(LocalDate date, long timeId, long themeId, long userId);
}
