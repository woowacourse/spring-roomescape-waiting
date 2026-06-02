package roomescape.repository;

import java.util.Optional;
import roomescape.domain.Reservation;
import roomescape.repository.dto.ReservationCondition;

public interface ReservationRepository {

    Reservation save(Reservation reservation);

    Optional<Reservation> findById(long id);

    Optional<Reservation> findByDateAndThemeAndTimeForUpdate(ReservationCondition condition);

    Optional<Reservation> findByEntryId(long entryId);

    Optional<Reservation> findByEntryIdForUpdate(long entryId);

    void update(Reservation reservation);
}
