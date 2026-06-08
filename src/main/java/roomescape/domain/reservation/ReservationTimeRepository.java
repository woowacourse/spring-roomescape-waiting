package roomescape.domain.reservation;

import roomescape.domain.DomainErrorCode;
import roomescape.domain.RoomEscapeException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static roomescape.domain.DomainErrorCode.RESOURCE_NOT_FOUND;

public interface ReservationTimeRepository {
    ReservationTime save(ReservationTime time);

    List<ReservationTime> findAll();

    Optional<ReservationTime> findById(long id);

    List<ReservationTime> findByDateAndTheme(LocalDate date, long themeId);

    void delete(long id);

    boolean existsById(long id);

    default ReservationTime getById(long id) {
        return findById(id)
                .orElseThrow(() -> new RoomEscapeException(RESOURCE_NOT_FOUND, "해당 시간을 찾을 수 없습니다. : " + id));
    }
}
