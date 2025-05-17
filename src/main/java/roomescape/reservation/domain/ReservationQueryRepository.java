package roomescape.reservation.domain;

import java.time.LocalDate;
import java.util.List;

public interface ReservationQueryRepository {

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    Reservation getByIdOrThrow(Long id);

    List<Reservation> findAll();

    List<Reservation> findAllByThemeIdAndMemberIdAndDateRange(Long themeId, Long memberId, LocalDate dateFrom,
                                                              LocalDate dateTo);

    List<Reservation> findAllByDateAndThemeId(LocalDate date, Long themeId);

    List<Reservation> findAllByMemberId(Long memberId);
}
