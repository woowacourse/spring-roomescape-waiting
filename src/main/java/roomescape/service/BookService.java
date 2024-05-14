package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.ReservationTime;
import roomescape.domain.dto.BookResponse;
import roomescape.domain.dto.BookResponses;
import roomescape.repository.ReservationDao;
import roomescape.repository.TimeDao;

@Service
public class BookService {
    private final TimeDao timeDao;

    public BookService(final ReservationDao reservationDao, final TimeDao timeDao) {
        this.timeDao = timeDao;
    }

    public BookResponses findAvaliableBookList(final LocalDate date, final Long themeId) {
        List<ReservationTime> reservationReservationTimes = timeDao.findByDateAndThemeId(date, themeId);
        List<ReservationTime> reservationTimes = timeDao.findAll();
        final List<BookResponse> bookResponses = reservationTimes.stream().map(timeSlot -> {
            Boolean alreadyBooked = reservationReservationTimes.stream()
                    .anyMatch(reservationTimeSlot -> reservationTimeSlot.getId().equals(timeSlot.getId()));
            return new BookResponse(timeSlot.getStartAt(), timeSlot.getId(), alreadyBooked);
        }).toList();
        return new BookResponses(bookResponses);
    }
}
