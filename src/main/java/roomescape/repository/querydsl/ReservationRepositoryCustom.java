package roomescape.repository.querydsl;

import java.time.LocalDate;
import java.util.List;
import roomescape.domain.Reservation;

public interface ReservationRepositoryCustom {

    List<Reservation> findByMemberIdAndThemeIdAndDateFromAndDateTo(Long memberId,
                                                                   Long themeId,
                                                                   LocalDate dateFrom,
                                                                   LocalDate dateTo);
}
