package roomescape.reservation.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.Waiting;

public interface JpaWaitingRepository extends JpaRepository<Waiting, Long> {

    boolean existsByDateAndTimeIdAndThemeId(ReservationDate date, Long timeId, Long themeId);

    boolean existsByDateAndTimeIdAndThemeIdAndMemberId(ReservationDate reservationDate, Long timeId, Long themeId,
                                                       Long memberId);

    List<Waiting> findByMemberId(Long memberId);

    @Query("""
                 SELECT w FROM Waiting w
                 WHERE w.date = :date AND w.time.id = :timeId AND w.theme.id = :themeId
                 ORDER BY w.createdAt ASC
                 LIMIT 1
            """)
    Optional<Waiting> findEarliestByDateAndTimeIdAndThemeId(
            ReservationDate date,
            Long timeId,
            Long themeId
    );

    @Query("""
                SELECT w
                FROM Waiting w
                LEFT JOIN FETCH w.theme
                LEFT JOIN FETCH w.time
                ORDER BY w.date ASC, w.time.startAt ASC, w.createdAt ASC
            """)
    List<Waiting> findAllByOrderByAsc();

//    @Query("SELECT new roomescape.reservation.service.dto.WaitingWithRank(" +
//            "    w, " +
//            "    (SELECT COUNT(w2) " +
//            "     FROM Waiting w2 " +
//            "     WHERE w2.theme = w.theme " +
//            "       AND w2.date = w.date " +
//            "       AND w2.time = w.time " +
//            "       AND w2.createdAt < w.createdAt)) " +
//            "FROM Waiting w " +
//            "WHERE w.memberId = :memberId")
//    List<WaitingWithRank> findWaitingsWithRankByMemberId(Long memberId);
}
