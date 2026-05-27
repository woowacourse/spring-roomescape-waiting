package roomescape.repository;

import static roomescape.domain.exception.DomainErrorCode.RESERVATION_TIME_NOT_FOUND;

import java.util.List;
import java.util.Optional;
import roomescape.domain.ReservationTime;
import roomescape.domain.exception.RoomEscapeException;

public interface ReservationTimeRepository {

    List<ReservationTime> findAll();

    Optional<ReservationTime> findById(Long id);

    Long save(ReservationTime time);

    void deleteById(Long id);

    boolean existsById(Long id);

    default ReservationTime getById(Long id, String message) {
        return findById(id).orElseThrow(() -> new RoomEscapeException(RESERVATION_TIME_NOT_FOUND, message));
    }
}
