package roomescape.reservation.repository;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.fixture.FakeReservationRepository;

public class StaleFirstReadReservationRepository extends FakeReservationRepository {

    private final Map<Long, Reservation> staleReservations = new HashMap<>();
    private final Set<Long> returnedStaleReservationIds = new HashSet<>();

    public void returnStaleOnce(Reservation reservation) {
        staleReservations.put(reservation.getId(), reservation);
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        if (staleReservations.containsKey(id) && returnedStaleReservationIds.add(id)) {
            return Optional.of(staleReservations.get(id));
        }
        return super.findById(id);
    }
}
