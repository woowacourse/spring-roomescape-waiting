package roomescape.repository;

import java.util.List;
import java.util.Optional;
import roomescape.domain.Reservation;
import roomescape.repository.dto.ReservationCondition;

public interface ReservationRepository {

    Reservation save(Reservation reservation);

    List<Reservation> findAll();

    Optional<Reservation> findById(long id);

    Optional<Reservation> findByDateAndThemeAndTimeForUpdate(ReservationCondition condition);

    Optional<Reservation> findByEntryIdForUpdate(long entryId);

    void update(Reservation reservation);
}
