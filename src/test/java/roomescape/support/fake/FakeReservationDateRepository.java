package roomescape.support.fake;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationdate.ReservationDateRepository;

public class FakeReservationDateRepository implements ReservationDateRepository {

    private final Map<Long, ReservationDate> storage = new LinkedHashMap<>();
    private long sequence = 1L;

    @Override
    public Optional<ReservationDate> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<ReservationDate> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public ReservationDate save(ReservationDate reservationDate) {
        Long id = reservationDate.getId();
        if (id == null) {
            id = sequence++;
        } else {
            sequence = Math.max(sequence, id + 1);
        }
        ReservationDate savedReservationDate = ReservationDate.of(id, reservationDate.getDate());
        storage.put(id, savedReservationDate);
        return savedReservationDate;
    }

    @Override
    public int deleteById(Long id) {
        ReservationDate removedReservationDate = storage.remove(id);
        if (removedReservationDate == null) {
            return 0;
        }
        return 1;
    }

    @Override
    public Optional<ReservationDate> findByDate(LocalDate startWhen) {
        return storage.values().stream()
            .filter(reservationDate -> startWhen.equals(reservationDate.getDate()))
            .findFirst();
    }
}
