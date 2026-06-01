package roomescape.fake;

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
    public Optional<Reservation> findReservationById(long id) {
        return store.stream()
                .filter(reservation -> reservation.getId().equals(id))
                .findFirst();
    }

    @Override
    public Optional<Reservation> findReservationBySlotId(Long slotId) {
        return store.stream()
                .filter(reservation -> reservation.getSlot().getId().equals(slotId))
                .findFirst();
    }

    @Override
    public List<Reservation> findAllReservations() {
        return List.copyOf(store);
    }

    @Override
    public List<Reservation> findAllByName(String name) {
        return store.stream()
                .filter(reservation -> reservation.getName().equals(name))
                .toList();
    }
}
