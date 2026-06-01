package roomescape.reservation.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.reservation.application.exception.ReservationNotFoundException;

public interface ActiveReservationRepository {
    ActiveReservation save(ActiveReservation reservation);
    Optional<ActiveReservation> findById(Long id);
    List<ActiveReservation> findAll();
    List<ActiveReservation> findByThemeAndDate(Long themeId, LocalDate date);
    List<ActiveReservation> findAllByName(String name);
    boolean existsByReservationTime(Long timeId);
    boolean existsByTheme(Long id);
    boolean existsByActiveSlotId(Long id);
    void update(ActiveReservation changedReservation);
    void cancel(ActiveReservation reservation);

    default ActiveReservation getById(final Long id) {
        return findById(id).orElseThrow(() -> new ReservationNotFoundException("해당 ID의 예약을 찾을 수 없습니다."));
    }

}
