package roomescape.reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.util.List;

public interface ReservationJpaRepository extends JpaRepository<Reservation, Long> {

    boolean existsByDateAndReservationTimeAndTheme(LocalDate date, ReservationTime reservationTime, Theme theme);

    List<Reservation> findByThemeAndMember(Theme theme, Member member);

    List<Reservation> findByDateAndTheme(LocalDate date, Theme theme);

    List<Reservation> findByMember(Member member);
}
