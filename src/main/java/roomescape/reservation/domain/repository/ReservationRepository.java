package roomescape.reservation.domain.repository;

import java.time.LocalDate;
import java.util.List;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationtime.presentation.dto.response.AvailableReservationTimeResponse;

public interface ReservationRepository {

    List<Reservation> findByThemeIdAndMemberIdAndDateBetween(Long themeId,
                                                             Long memberId,
                                                             LocalDate startDate,
                                                             LocalDate endDate);

    boolean existsByTimeId(Long id);

    boolean existsByThemeId(Long id);

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    List<AvailableReservationTimeResponse> findBookedTimesByDateAndThemeId(LocalDate date,
                                                                           Long themeId);

    List<Reservation> findByMemberId(Long id);

    List<Reservation> findAll();

    void deleteById(Long id);

    Reservation save(Reservation reservation);
}
