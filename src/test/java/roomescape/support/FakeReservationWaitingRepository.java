package roomescape.support;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationwaiting.ReservationWaiting;
import roomescape.domain.reservationwaiting.ReservationWaitingLine;
import roomescape.repository.reservationwaiting.ReservationWaitingRepository;

public class FakeReservationWaitingRepository implements ReservationWaitingRepository {

    private final Map<Long, ReservationWaiting> waitings = new LinkedHashMap<>();
    private long sequence = 1L;

    @Override
    public ReservationWaiting save(final ReservationWaiting reservationWaiting) {
        ReservationWaiting saved = reservationWaiting;
        if (saved.getId() == null) {
            saved = saved.withId(sequence++);
        }

        waitings.put(saved.getId(), saved);
        return saved;
    }

    @Override
    public Optional<ReservationWaiting> findById(final Long id) {
        return Optional.ofNullable(waitings.get(id));
    }

    @Override
    public ReservationWaitingLine findLineByReservation(final Reservation reservation) {
        return ReservationWaitingLine.fromWaitings(waitings.values()
                .stream()
                .filter(waiting -> waiting.getReservation().equals(reservation))
                .toList());
    }

    @Override
    public void delete(final ReservationWaiting reservationWaiting) {
        waitings.remove(reservationWaiting.getId());
    }
}
