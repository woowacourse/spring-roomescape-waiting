package roomescape.reservation.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.common.exception.NotFoundException;
import roomescape.reservation.domain.dto.ReservationQueryResult;

public interface ReservationRepository {
    Reservation save(Reservation reservation);

    void update(Reservation changedReservation);

    Optional<Reservation> findById(Long id);

    Optional<Reservation> findNextPendingReservation(LocalDate date, Long timeId, Long themeId);

    List<Reservation> findAll(int page, int size);

    List<Reservation> findByThemeAndDate(Long themeId, LocalDate date);

    List<ReservationQueryResult> findAllByName(String username);

    boolean existsByReservationTime(Long timeId);

    boolean existsByTheme(Long themeId);

    boolean existsActiveReservationByDateTimeAndTheme(Long timeId, Long themeId, LocalDate date);

    boolean existsByUsernameAndDateTimeAndTheme(Long timeId, Long themeId, LocalDate date, String name);

    default Reservation getById(Long id) {
        return findById(id).orElseThrow(() -> new NotFoundException("존재하지 않는 예약입니다."));
    }
}
