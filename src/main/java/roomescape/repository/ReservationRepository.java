package roomescape.repository;

import static roomescape.domain.exception.DomainErrorCode.RESERVATION_NOT_FOUND;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import roomescape.domain.Reservation;
import roomescape.domain.exception.RoomEscapeException;

public interface ReservationRepository {

    List<Reservation> findAll();

    List<Reservation> findByName(String name);

    Optional<Reservation> findById(Long id);

    Set<Long> findReservedTimeIdsByDateAndThemeId(LocalDate date, Long themeId);

    boolean existsByTimeId(Long timeId);

    boolean existsByThemeId(Long themeId);

    boolean existsBy(Reservation reservation);

    Long save(Reservation reservation);

    void deleteById(Long id);

    void updateDateTime(Reservation updated);

    default Reservation getById(Long id, String message) {
        return findById(id).orElseThrow(() -> new RoomEscapeException(RESERVATION_NOT_FOUND, message));
    }
}
