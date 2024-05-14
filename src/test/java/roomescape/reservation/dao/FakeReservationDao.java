package roomescape.reservation.dao;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.repository.ReservationRepository;

public class FakeReservationDao implements ReservationRepository {
    private final Map<Long, Reservation> reservations = new HashMap<>();

    @Override
    public Reservation save(final Reservation reservation) {
        reservations.put((long) reservations.size() + 1, reservation);
        return new Reservation((long) reservations.size(), reservation.getDate(), reservation.getTime(),
                reservation.getTheme());
    }

    @Override
    public void delete(final long reservationId) {
        reservations.remove(reservationId);
    }

    @Override
    public boolean existsByTimeId(long timeId) {
        return reservations.values().stream().anyMatch(reservation -> reservation.getTime().getId() == timeId);
    }

    @Override
    public boolean existsByThemeId(long themeId) {
        return reservations.values().stream().anyMatch(reservation -> reservation.getTheme().getId() == themeId);
    }

    @Override
    public boolean existsBy(LocalDate date, ReservationTime time, Theme theme) {
        return reservations.values().stream().anyMatch(
                reservation -> reservation.isSame(date, time, theme));
    }
}
