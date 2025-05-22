package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.WaitingWithRank;
import roomescape.reservation.service.WaitingRepository;

public interface WaitingJpaRepository extends JpaRepository<Waiting, Long>, WaitingRepository {

    @Query("SELECT COUNT(w) > 0 FROM Waiting w " +
            "WHERE w.member.id = :memberId " +
            "AND w.reservation.theme.id = :themeId " +
            "AND w.reservation.reservationTime.id = :timeId " +
            "AND w.reservation.reservationDate.reservationDate = :date")
    boolean existsBySameReservation(
            @Param("memberId") Long memberId,
            @Param("themeId") Long themeId,
            @Param("timeId") Long reservationTimeId,
            @Param("date") LocalDate date
    );

    void deleteById(Long id);

    @Query("SELECT new roomescape.reservation.domain.WaitingWithRank(" +
            "    w, " +
            "    (SELECT COUNT(w2) * 1L " +
            "     FROM Waiting w2 " +
            "     WHERE w2.reservation.id = w.reservation.id " +
            "       AND w2.id < w.id)) " +
            "FROM Waiting w " +
            "WHERE w.member.id = :memberId")
    List<WaitingWithRank> findWaitingsWithRankByMemberId(Long memberId);
}
