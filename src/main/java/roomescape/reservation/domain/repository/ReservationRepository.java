package roomescape.reservation.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.WaitingWithRank;
import roomescape.theme.domain.Theme;

public interface ReservationRepository extends JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {

    List<Reservation> findByReservationTime(ReservationTime reservationTime);

    Optional<Reservation> findFirstByReservationTimeAndDateAndThemeAndReservationStatusOrderById(
            ReservationTime reservationTime,
            LocalDate date,
            Theme theme,
            final ReservationStatus waiting
    );

    List<Reservation> findByDateAndTheme(LocalDate date, Theme theme);

    List<Reservation> findByMember(Member member);

    @Query("SELECT new roomescape.reservation.domain.WaitingWithRank(" +
           "    w, " +
           "    (SELECT COUNT(w2) " +
           "     FROM Reservation w2 " +
           "     WHERE w2.theme = w.theme " +
           "       AND w2.date = w.date " +
           "       AND w2.reservationTime = w.reservationTime " +
           "       AND w2.id < w.id)) " +
           "FROM Reservation w " +
           "WHERE w.member.id = :memberId")
    List<WaitingWithRank> findWaitingsWithRankByMemberId(Long memberId);

}
