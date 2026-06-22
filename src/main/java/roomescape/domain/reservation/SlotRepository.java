package roomescape.domain.reservation;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.RoomEscapeException;
import roomescape.domain.theme.Theme;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static roomescape.domain.DomainErrorCode.RESOURCE_NOT_FOUND;

public interface SlotRepository extends JpaRepository<Slot, Long> {
    Optional<Slot> findByDateAndTimeAndTheme(ReservationDate date, ReservationTime time, Theme theme);

    List<Slot> findByDateAndThemeId(ReservationDate date, Long themeId);

boolean existsByTimeId(Long timeId);

    boolean existsByThemeId(Long themeId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Slot s WHERE s.id = :id")
    Optional<Slot> findByIdForUpdate(@Param("id") Long id);

    default Slot getById(Long id) {
        return findById(id)
                .orElseThrow(() -> new RoomEscapeException(RESOURCE_NOT_FOUND, "해당 예약 슬롯을 찾을 수 없습니다. : " + id));
    }

    default Slot getByIdForUpdate(Long id) {
        return findByIdForUpdate(id)
                .orElseThrow(() -> new RoomEscapeException(RESOURCE_NOT_FOUND, "해당 예약 슬롯을 찾을 수 없습니다. : " + id));
    }

    default Slot findOrCreate(ReservationDate date, ReservationTime time, Theme theme, LocalDateTime now) {
        return findByDateAndTimeAndTheme(date, time, theme)
                .orElseGet(() -> save(Slot.create(date, time, theme, now)));
    }
}
