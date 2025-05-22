package roomescape.reservation.domain.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.WaitingWithRank;

@Repository
public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    boolean existsByMemberIdAndThemeIdAndReservationTimeIdAndDate(Long memberId, Long themeId, Long reservationTimeId,
        LocalDate date);

    @Query("SELECT new roomescape.reservation.domain.WaitingWithRank(" +
        "    w, " +
        "    (SELECT COUNT(w2) + 1" +
        "     FROM Waiting w2 " +
        "     WHERE w2.theme.id = w.theme.id " +
        "       AND w2.date = w.date " +
        "       AND w2.reservationTime.id = w.reservationTime.id " +
        "       AND w2.id < w.id)) " +
        "FROM Waiting w " +
        "WHERE w.member.id = :memberId")
    List<WaitingWithRank> findWaitingsWithRankByMemberId(Long memberId);
}
