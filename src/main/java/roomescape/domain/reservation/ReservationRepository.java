package roomescape.domain.reservation;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository {

    Reservation save(Reservation reservation);

    List<Reservation> findAllByConditions(Long memberId, Long themeId, LocalDate dateFrom, LocalDate dateTo);

    void deleteById(Long id);

    boolean existsById(Long id);

    boolean existsByTimeId(Long id);

    boolean existsByThemeId(Long id);

    boolean existsByReservation(LocalDate date, Long timeId, Long themeId);
}
