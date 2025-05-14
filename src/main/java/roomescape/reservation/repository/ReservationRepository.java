package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.theme.domain.Theme;

public interface ReservationRepository extends CrudRepository<Reservation, Long> {

    List<Reservation> findAll();

    boolean existsByDateAndTimeAndTheme(LocalDate date, ReservationTime time, Theme theme);

    List<Reservation> findByThemeAndMemberAndDateBetween(final Theme theme, final Member member,
                                                         final LocalDate dateFrom,
                                                         final LocalDate dateTo);
}
