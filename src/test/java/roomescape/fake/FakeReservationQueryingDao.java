package roomescape.fake;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import roomescape.domain.reservation.Reservation;
import roomescape.repository.ReservationQueryingDao;

public class FakeReservationQueryingDao extends ReservationQueryingDao {

    private final List<Reservation> store = new ArrayList<>();

    public FakeReservationQueryingDao() {
        super(null);
    }

    public void save(Reservation reservation) {
        store.add(reservation);
    }

    @Override
    public Optional<Reservation> findReservationByThemeAndDateAndTime(Long themeId, LocalDate date, Long timeId) {
        return store.stream()
                .filter(r -> r.getTheme().getId().equals(themeId)
                          && r.getDate().equals(date)
                          && r.getTime().getId().equals(timeId))
                .findFirst();
    }

    @Override
    public Optional<Reservation> findReservationById(long id) {
        return store.stream()
                .filter(reservation->reservation.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<Reservation> findAllReservations() {
        return List.copyOf(store);
    }
}
