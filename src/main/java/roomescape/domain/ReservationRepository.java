package roomescape.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

    List<Reservation> findAll();

    Optional<Reservation> findById(Long id);

    Reservation save(Reservation reservation);

    void deleteById(Long id);

    boolean existByReservationTimeId(Long id);

    boolean existByThemeId(Long id);

    boolean existByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    List<Reservation> filter(Long themeId, Long memberId, LocalDate dateFrom, LocalDate dateTo);
}
