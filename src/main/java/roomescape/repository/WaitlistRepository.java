package roomescape.repository;

import static roomescape.domain.exception.DomainErrorCode.RESERVATION_NOT_FOUND;

import java.util.Optional;
import roomescape.domain.Reservation;
import roomescape.domain.Waitlist;
import roomescape.domain.exception.RoomEscapeException;

public interface WaitlistRepository {

    Optional<Waitlist> findById(Long id);

    int countBefore(Waitlist waitlist);

    boolean existsByTimeId(Long timeId);

    boolean existsByThemeId(Long themeId);

    boolean existsBySameUser(Reservation reservation);

    Long save(Reservation reservation);

    void deleteById(Long id);

    default Waitlist getById(Long id, String message) {
        return findById(id).orElseThrow(() -> new RoomEscapeException(RESERVATION_NOT_FOUND, message));
    }
}
