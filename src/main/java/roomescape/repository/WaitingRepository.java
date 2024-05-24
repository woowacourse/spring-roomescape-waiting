package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.model.ReservationTime;
import roomescape.model.Waiting;
import roomescape.model.WaitingWithRank;
import roomescape.model.theme.Theme;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    @Query("""
            SELECT new roomescape.model.WaitingWithRank(w, (SELECT COUNT(w2) + 1
                        FROM Waiting w2
                        WHERE w2.theme = w.theme
                            AND w2.date = w.date
                            AND w2.time = w.time
                            AND w2.id < w.id))
            FROM Waiting w
            WHERE w.member.id = ?1
            """)
    List<WaitingWithRank> findWaitingWithRankByMemberId(Long memberId);

    @Query("""
            SELECT w
            FROM Waiting w
            WHERE w.date = ?1
                AND w.time = ?2
                AND w.theme = ?3
            ORDER BY w.id ASC
            LIMIT 1
            """)
    Optional<Waiting> findFirstByDateAndTimeAndTheme(LocalDate date, ReservationTime time, Theme theme);

    boolean existsByDateAndTimeIdAndThemeIdAndMemberId(LocalDate date, long timeId, long themeId, long memberId);
}
