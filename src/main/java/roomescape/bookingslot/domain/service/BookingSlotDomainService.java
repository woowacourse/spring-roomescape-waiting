package roomescape.bookingslot.domain.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.member.domain.Member;
import roomescape.bookingslot.domain.BookingSlot;
import roomescape.bookingslot.domain.repository.BookingSlotRepository;
import roomescape.bookingslot.exception.ReservationAlreadyExistsException;
import roomescape.bookingslot.exception.ReservationNotFoundException;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.presentation.dto.response.AvailableReservationTimeResponse;
import roomescape.theme.domain.Theme;

@Service
public class BookingSlotDomainService {

    private final BookingSlotRepository bookingSlotRepository;

    public BookingSlotDomainService(final BookingSlotRepository bookingSlotRepository) {
        this.bookingSlotRepository = bookingSlotRepository;
    }

    public void delete(Long id) {
        bookingSlotRepository.deleteById(id);
    }

    public void checkIfReservationDoesNotExists(final LocalDate date, final Long timeId, final Long themeId) {
        if (bookingSlotRepository.existsByDateAndTimeIdAndThemeId(date, timeId, themeId)) {
            throw new ReservationAlreadyExistsException("해당 시간에 이미 예약이 존재합니다.");
        }
    }

    public BookingSlot getReservationByDateAndTimeAndTheme(final LocalDate date, final Long timeId,
                                                           final Long themeId) {
        return bookingSlotRepository.findByDateAndTimeIdAndThemeId(date, timeId, themeId)
                .orElseThrow(() -> new ReservationNotFoundException("해당 시간에 예약이 존재하지 않습니다."));
    }

    public List<BookingSlot> findFilteredReservations(final Long themeId, final Long memberId,
                                                      final LocalDate startDate, final LocalDate endDate) {
        if ((themeId == null) || (memberId == null) || (startDate == null) || (endDate == null)) {
            return bookingSlotRepository.findAll();
        }
        return bookingSlotRepository.findByThemeIdAndDateBetweenAndWaitingMemberId(themeId, startDate, endDate,
                memberId);
    }

    public BookingSlot save(final Member member, final LocalDate date, final ReservationTime time, final Theme theme,
                            final LocalDateTime now) {
        return bookingSlotRepository.save(
                BookingSlot.createUpcomingReservation(member, date, time, theme, now));
    }

    public boolean existsByTimeId(final Long timeId) {
        return bookingSlotRepository.existsByTimeId(timeId);
    }

    public List<AvailableReservationTimeResponse> findBookedTimesByDateAndThemeId(final LocalDate date,
                                                                                  final Long themeId) {
        return bookingSlotRepository.findBookedTimesByDateAndThemeId(date, themeId);
    }

    public boolean existsByThemeId(final Long themeId) {
        return bookingSlotRepository.existsByThemeId(themeId);
    }
}
