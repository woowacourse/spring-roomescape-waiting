package roomescape.reservation.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationQueryRepository {


    Boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    Optional<Reservation> findById(Long id);

    Reservation getById(Long id);

    List<Reservation> findAll();

    List<Reservation> findAllByThemeIdAndMemberIdAndDateRange(Long themeId, Long memberId, LocalDate dateFrom,
                                                              LocalDate dateTo);

    List<Reservation> findReservationsByDateAndThemeId(LocalDate date, Long themeId);
}
