package roomescape.reservation.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.reservation.application.exception.ReservationNotFoundException;
import roomescape.reservation.domain.dto.ReservationQueryResult;

public interface ReservationRepository {
    Reservation save(Reservation reservation);
    Optional<Reservation> findById(Long id);
    Optional<Reservation> findNextPendingReservation(LocalDate date, Long timeId, Long themeId);
    List<Reservation> findAll();
    List<Reservation> findByThemeAndDate(Long themeId, LocalDate date);
    List<ReservationQueryResult> findAllByName(String username);
    boolean existsByReservationTime(Long timeId);
    boolean existsByReservationTimeAndThemeAndDate(Long timeId, Long themeId, LocalDate date);
    boolean existsByReservationTimeAndThemeAndDateAndIdNot(Long id, Long timeId, Long themeId, LocalDate date);
    boolean existsByTheme(Long id);
    boolean existsPendingReservationByName(Long timeId, Long themeId, LocalDate date, String name);
    int deleteById(Long id);
    void updateDetails(Long id, Reservation changedReservation);
    void cancel(Reservation reservation);
    void promoteToActive(Long id);

    default Reservation getById(final Long id) {
        return findById(id).orElseThrow(() -> new ReservationNotFoundException("해당 ID의 예약을 찾을 수 없습니다."));
    }
}
