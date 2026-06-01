package roomescape.repository;

import static roomescape.domain.exception.DomainErrorCode.RESERVATION_NOT_FOUND;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import roomescape.domain.Reservation;
import roomescape.domain.Waitlist;
import roomescape.domain.exception.RoomEscapeException;

public interface WaitlistRepository {

    Optional<Waitlist> findById(Long id);

    boolean existsBySameUser(Reservation reservation);

    Long save(Reservation reservation, LocalDateTime createdAt);

    void deleteById(Long id);

    List<Waitlist> findByName(String name);

    List<Waitlist> findBySlot(LocalDate date, Long timeId, Long themeId);

    default Waitlist getById(Long id, String message) {
        return findById(id).orElseThrow(() -> new RoomEscapeException(RESERVATION_NOT_FOUND, message));
    }
}
