package roomescape.waiting.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import roomescape.waiting.Waiting;
import roomescape.waiting.WaitingWithRank;

@Repository
public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    boolean existsByMemberIdAndThemeIdAndReservationTimeIdAndDate(Long memberId, Long themeId, Long reservationTimeId,
        LocalDate date);

    @Query("SELECT new roomescape.waiting.WaitingWithRank(" +
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


    Optional<Waiting> findFirstByThemeIdAndDateAndReservationTimeId(Long themeId, LocalDate date, Long reservationTimeId);
}
