package roomescape.core.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.core.domain.Member;
import roomescape.core.domain.Reservation;
import roomescape.core.domain.ReservationTime;
import roomescape.core.domain.Theme;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findAllByDateAndTheme(final LocalDate date, final Theme theme);

    List<Reservation> findAllByMemberAndThemeAndDateBetween(final Member member,
                                                            final Theme theme,
                                                            final LocalDate dateFrom,
                                                            final LocalDate dateTo);

    List<Reservation> findAllByMember(final Member member);

    Integer countByTime(final ReservationTime reservationTime);

    Integer countByTheme(final Theme theme);

    Integer countByDateAndTimeAndTheme(final LocalDate date, final ReservationTime time, final Theme theme);

    void deleteById(final long id);
}
