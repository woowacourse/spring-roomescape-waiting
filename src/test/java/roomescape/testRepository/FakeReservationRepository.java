package roomescape.testRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import roomescape.domain.Reservation;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.dto.ReservationSearchFilter;
import roomescape.exception.NotFoundException;

public class FakeReservationRepository implements ReservationRepository {
    private final List<Reservation> reservations = new ArrayList<>();

    private Long index = 0L;

    @Override
    public List<Reservation> findAll() {
        return Collections.unmodifiableList(reservations);
    }

    @Override
    public Long save(Reservation reservation) {
        Reservation reservationWithId = Reservation.assignId(++index, reservation);
        reservations.add(reservationWithId);
        return index;
    }

    @Override
    public boolean deleteById(Long id) {
        Reservation reservation = findById(id);
        reservations.remove(reservation);
        return true;
    }

    @Override
    public List<Reservation> searchWith(ReservationSearchFilter reservationSearchFilter) {
        return List.of();
    }

    private Reservation findById(Long id) {
        return reservations.stream()
                .filter(reservation -> reservation.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("id에 해당하는 Reservation이 존재하지 않습니다."));
    }
}
