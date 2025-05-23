package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import roomescape.reservation.domain.Waiting;

public interface WaitingReservationRepository {

    List<Waiting> findFilteredReservations(Long themeId,
                                           Long memberId,
                                           LocalDate startDate,
                                           LocalDate endDate);

    List<Waiting> findAll();

    void deleteById(Long id);

    Waiting save(Waiting upcomingReservationWithUnassignedId);

    int findMaxOrderByDateAndTimeAndTheme(LocalDate date, Long timeId, Long themeId);

    List<Waiting> findByMemberId(Long id);
}
