package roomescape.domain.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
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

    @Query("SELECT r.slot FROM Reservation r WHERE r.member.id = :memberId")
    List<Slot> findAllByMemberId(@Param("memberId") Long memberId);

    boolean existsByTimeId(Long timeId);

    boolean existsByThemeId(Long themeId);

    default Slot getById(Long id) {
        return findById(id)
                .orElseThrow(() -> new RoomEscapeException(RESOURCE_NOT_FOUND, "해당 예약 슬롯을 찾을 수 없습니다. : " + id));
    }

    default Slot findOrCreate(ReservationDate date, ReservationTime time, Theme theme, LocalDateTime now) {
        return findByDateAndTimeAndTheme(date, time, theme)
                .orElseGet(() -> save(Slot.create(date, time, theme, now)));
    }
}
