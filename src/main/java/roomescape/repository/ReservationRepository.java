package roomescape.repository;

import java.util.Optional;
import roomescape.domain.Reservation;
import roomescape.exception.EntityNotFoundException;
import roomescape.repository.dto.ReservationCondition;

public interface ReservationRepository {

    Reservation save(Reservation reservation);

    Optional<Reservation> findById(long id);

    Optional<Reservation> findByDateAndThemeAndTimeForUpdate(ReservationCondition condition);

    Optional<Reservation> findByEntryId(long entryId);

    Optional<Reservation> findByEntryIdForUpdate(long entryId);

    default Reservation getByEntryId(long entryId) {
        return findByEntryId(entryId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 예약 정보입니다."));
    }

    default Reservation getByEntryIdForUpdate(long entryId) {
        return findByEntryIdForUpdate(entryId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 예약 정보입니다."));
    }

    void update(Reservation reservation);
}
