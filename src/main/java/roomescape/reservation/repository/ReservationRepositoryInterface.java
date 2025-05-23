package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.theme.domain.Theme;

public interface ReservationRepositoryInterface {
    List<Reservation> findAll();

    boolean existsByDateAndTimeAndTheme(
            final LocalDate date,
            final ReservationTime time,
            final Theme theme);

    List<Theme> findPopularThemesByReservationBetween(
            final LocalDate dateFrom,
            final LocalDate dateTo,
            final PageRequest pageRequest);

    List<Reservation> findByMember(final Member member);

    Optional<Reservation> findById(final Long id);

    Reservation save(final Reservation reservation);

    void deleteById(final Long id);

    List<Reservation> findByThemeAndMemberAndDateBetween(final Theme theme, final Member member,
                                                         final LocalDate dateFrom, final LocalDate dateTo);
}

