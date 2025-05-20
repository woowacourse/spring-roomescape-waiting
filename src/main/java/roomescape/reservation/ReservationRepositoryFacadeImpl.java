package roomescape.reservation;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.member.Member;
import roomescape.reservationtime.ReservationTime;
import roomescape.theme.Theme;

@Repository
@AllArgsConstructor
public class ReservationRepositoryFacadeImpl implements ReservationRepositoryFacade {

    private final ReservationRepository reservationRepository;

    @Override
    public Reservation save(final Reservation reservation) {
        return reservationRepository.save(reservation);
    }

    @Override
    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    @Override
    public List<Reservation> findAllByMember(final Member member) {
        return reservationRepository.findAllByMember(member);
    }

    @Override
    public List<Reservation> findAllByThemeAndDate(final Theme theme, final LocalDate date) {
        return reservationRepository.findAllByThemeAndDate(theme, date);
    }

    @Override
    public List<Reservation> findAllByMemberAndThemeAndDateBetween(final Member member, final Theme theme,
                                                                   final LocalDate from,
                                                                   final LocalDate to) {
        return reservationRepository.findAllByMemberAndThemeAndDateBetween(member, theme, from, to);
    }

    @Override
    public void deleteById(final Long id) {
        reservationRepository.deleteById(id);
    }

    @Override
    public boolean existsById(final Long id) {
        return reservationRepository.existsById(id);
    }

    @Override
    public boolean existsByReservationTime(final ReservationTime reservationTime) {
        return reservationRepository.existsByReservationTime(reservationTime);
    }

    @Override
    public boolean existsByReservationTimeAndDateAndTheme(final ReservationTime reservationTime, final LocalDate date,
                                                          final Theme theme) {
        return reservationRepository.existsByReservationTimeAndDateAndTheme(reservationTime, date, theme);
    }
}
