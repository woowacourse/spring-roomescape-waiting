package roomescape.reservation.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationtime.presentation.dto.response.AvailableReservationTimeResponse;

public interface ReservationRepository {

    List<Reservation> findByThemeIdAndDateBetweenAndWaitingMemberId(Long themeId, LocalDate startDate,
                                                                    LocalDate endDate,
                                                                    Long memberId);

    boolean existsByTimeId(Long id);

    boolean existsByThemeId(Long id);

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    Optional<Reservation> findByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    List<AvailableReservationTimeResponse> findBookedTimesByDateAndThemeId(LocalDate date,
                                                                           Long themeId);

    List<Reservation> findAll();

    void deleteById(Long id);

    Reservation save(Reservation reservation);
}
