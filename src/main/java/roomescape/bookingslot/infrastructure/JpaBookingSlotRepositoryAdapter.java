package roomescape.bookingslot.infrastructure;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.bookingslot.domain.BookingSlot;
import roomescape.bookingslot.domain.repository.BookingSlotRepository;
import roomescape.reservationtime.presentation.dto.response.AvailableReservationTimeResponse;

@Repository
public class JpaBookingSlotRepositoryAdapter implements BookingSlotRepository {

    private final JpaBookingSlotRepository jpaBookingSlotRepository;

    public JpaBookingSlotRepositoryAdapter(final JpaBookingSlotRepository jpaBookingSlotRepository) {
        this.jpaBookingSlotRepository = jpaBookingSlotRepository;
    }

    @Override
    public List<BookingSlot> findByThemeIdAndDateBetweenAndWaitingMemberId(final Long themeId,
                                                                           final LocalDate startDate,
                                                                           final LocalDate endDate,
                                                                           final Long memberId) {
        return jpaBookingSlotRepository.findByThemeIdAndDateBetweenAndWaitingsMemberId(themeId, startDate, endDate,
                memberId);
    }

    @Override
    public boolean existsByTimeId(final Long id) {
        return jpaBookingSlotRepository.existsByTimeId(id);
    }

    @Override
    public boolean existsByThemeId(final Long id) {
        return jpaBookingSlotRepository.existsByThemeId(id);
    }

    @Override
    public boolean existsByDateAndTimeIdAndThemeId(final LocalDate date, final Long timeId, final Long themeId) {
        return jpaBookingSlotRepository.existsByDateAndTimeIdAndThemeId(date, timeId, themeId);
    }

    @Override
    public Optional<BookingSlot> findByDateAndTimeIdAndThemeId(final LocalDate date, final Long timeId,
                                                               final Long themeId) {
        return jpaBookingSlotRepository.findByDateAndTimeIdAndThemeId(date, timeId, themeId);
    }

    @Override
    public List<AvailableReservationTimeResponse> findBookedTimesByDateAndThemeId(final LocalDate date,
                                                                                  final Long themeId) {
        return jpaBookingSlotRepository.findBookedTimesByDateAndThemeId(date, themeId);
    }

    @Override
    public List<BookingSlot> findAll() {
        return jpaBookingSlotRepository.findAll();
    }

    @Override
    public void deleteById(final Long id) {
        jpaBookingSlotRepository.deleteById(id);
    }

    @Override
    public BookingSlot save(final BookingSlot bookingSlot) {
        return jpaBookingSlotRepository.save(bookingSlot);
    }
}
