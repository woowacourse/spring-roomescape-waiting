package roomescape.bookingslot.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.bookingslot.domain.BookingSlot;
import roomescape.reservationtime.presentation.dto.response.AvailableReservationTimeResponse;

public interface BookingSlotRepository {

    List<BookingSlot> findByThemeIdAndDateBetweenAndWaitingMemberId(Long themeId, LocalDate startDate,
                                                                    LocalDate endDate,
                                                                    Long memberId);

    boolean existsByTimeId(Long id);

    boolean existsByThemeId(Long id);

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    Optional<BookingSlot> findByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    List<AvailableReservationTimeResponse> findBookedTimesByDateAndThemeId(LocalDate date,
                                                                           Long themeId);

    List<BookingSlot> findAll();

    void deleteById(Long id);

    BookingSlot save(BookingSlot bookingSlot);
}
