package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.theme.domain.Theme;

public interface ReservationJpaRepository extends CrudRepository<Reservation, Long> {
    boolean existsByDateAndReservationTimeAndTheme(LocalDate date, ReservationTime reservationTime, Theme theme);

    List<Reservation> findByThemeAndMember(Theme theme, Member member);

    List<Reservation> findByDateAndTheme(LocalDate date, Theme theme);

    List<Reservation> findByMember(Member member);
}
