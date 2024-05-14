package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.ReservationTime;
import roomescape.domain.dto.BookResponse;
import roomescape.domain.dto.BookResponses;
import roomescape.repository.ReservationTimeRepository;

@Service
public class BookService {
    private final ReservationTimeRepository reservationTimeRepository;

    public BookService(final ReservationTimeRepository reservationTimeRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
    }

    public BookResponses findAvailableBookList(final LocalDate date, final Long themeId) {
        List<ReservationTime> reservationReservationTimes = reservationTimeRepository.findByDateAndThemeId(date,
                themeId);
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        final List<BookResponse> bookResponses = reservationTimes.stream().map(timeSlot -> {
            Boolean alreadyBooked = reservationReservationTimes.stream()
                    .anyMatch(reservationTimeSlot -> reservationTimeSlot.getId().equals(timeSlot.getId()));
            return new BookResponse(timeSlot.getStartAt(), timeSlot.getId(), alreadyBooked);
        }).toList();
        return new BookResponses(bookResponses);
    }
}
