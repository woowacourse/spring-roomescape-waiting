package roomescape.repository;

import static roomescape.domain.exception.DomainErrorCode.RESERVATION_NOT_FOUND;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.Reservation;
import roomescape.domain.exception.RoomEscapeException;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findAllByOrderByDateDescTime_StartAtAsc();

    List<Reservation> findByName(String name);

    List<Reservation> findByDateAndTheme_Id(LocalDate date, Long themeId);

    boolean existsByTime_Id(Long timeId);

    boolean existsByTheme_Id(Long themeId);

    boolean existsByDateAndTime_IdAndTheme_Id(LocalDate date, Long timeId, Long themeId);

    boolean existsByNameAndDateAndTime_IdAndTheme_Id(
            String name,
            LocalDate date,
            Long timeId,
            Long themeId
    );

    default Reservation getById(Long id, String message) {
        return findById(id).orElseThrow(() -> new RoomEscapeException(RESERVATION_NOT_FOUND, message));
    }
}
