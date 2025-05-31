package roomescape.waiting.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingWithRank;

@Repository
public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    boolean existsByMemberIdAndReservationSchedule_Theme_IdAndReservationSchedule_ReservationTime_IdAndReservationSchedule_Date(
        Long memberId,
        Long themeId,
        Long reservationTimeId,
        LocalDate date
    );

    @Query("SELECT new roomescape.waiting.domain.WaitingWithRank(" +
        "    w, " +
        "    (SELECT COUNT(w2) + 1" +
        "     FROM Waiting w2 " +
        "     WHERE w2.reservationSchedule.theme.id = w.reservationSchedule.theme.id" +
        "       AND w2.reservationSchedule.date = w.reservationSchedule.date" +
        "       AND w2.reservationSchedule.reservationTime.id = w.reservationSchedule.reservationTime.id " +
        "       AND w2.id < w.id)) " +
        "FROM Waiting w " +
        "WHERE w.member.id = :memberId")
    List<WaitingWithRank> findWaitingsWithRankByMemberId(Long memberId);

    Optional<Waiting> findFirstByReservationSchedule_Theme_IdAndReservationSchedule_DateAndReservationSchedule_ReservationTime_IdOrderById(Long themeId, LocalDate date, Long reservationTimeId);
}
