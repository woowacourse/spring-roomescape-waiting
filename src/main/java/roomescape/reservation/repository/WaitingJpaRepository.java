package roomescape.reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.WaitingWithRank;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WaitingJpaRepository extends JpaRepository<Waiting, Long> {

    boolean existsByDateAndReservationTimeAndThemeAndMember(
            LocalDate date, ReservationTime reservationTime, Theme theme, Member member);

    @Query("SELECT new roomescape.reservation.domain.WaitingWithRank(" +
            "    w, " +
            " new roomescape.reservation.domain.Rank(" +
            "    (SELECT COUNT(w2) " +
            "     FROM Waiting w2 " +
            "     WHERE w2.theme = w.theme " +
            "       AND w2.date = w.date " +
            "       AND w2.reservationTime = w.reservationTime " +
            "       AND w2.id < w.id))) " +
            "FROM Waiting w " +
            "WHERE w.member.id = :memberId")
    List<WaitingWithRank> findWaitingsWithRankByMemberId(Long memberId);

    Optional<Waiting> findTopByDateAndReservationTimeAndTheme(
            LocalDate date, ReservationTime reservationTime, Theme theme);
}
