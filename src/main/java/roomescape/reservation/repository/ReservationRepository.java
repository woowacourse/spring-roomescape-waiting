package roomescape.reservation.repository;

import java.util.List;
import roomescape.reservation.domain.Reservation;

public interface ReservationRepository {
    Reservation save(Reservation reservation);

    List<Reservation> findAll();

    void deleteById(Long id);

    boolean existsByTimeId(Long id);

    List<Reservation> findByMemberAndThemeAndVisitDateBetween(Long themeId, Long memberId, String dateFrom,
                                                              String dateTo);
}
