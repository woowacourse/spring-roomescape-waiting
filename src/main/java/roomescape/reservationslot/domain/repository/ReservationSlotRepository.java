package roomescape.reservationslot.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.reservationslot.domain.ReservationSlot;
import roomescape.reservationtime.presentation.dto.response.AvailableReservationTimeResponse;

public interface ReservationSlotRepository {

    List<ReservationSlot> findByThemeIdAndDateBetweenAndReservationMemberId(Long themeId, LocalDate startDate,
                                                                            LocalDate endDate,
                                                                            Long memberId);

    boolean existsByTimeId(Long id);

    boolean existsByThemeId(Long id);

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    Optional<ReservationSlot> findByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    List<AvailableReservationTimeResponse> findBookedTimesByDateAndThemeId(LocalDate date,
                                                                           Long themeId);

    List<ReservationSlot> findAll();

    void deleteById(Long id);

    ReservationSlot save(ReservationSlot reservationSlot);
}
