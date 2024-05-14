package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.Reservation;
import roomescape.domain.TimeSlot;
import roomescape.dto.response.BookResponse;
import roomescape.repository.ReservationDao;
import roomescape.repository.TimeDao;

@Service
public class BookService {
    private final ReservationDao reservationDao;
    private final TimeDao timeDao;

    public BookService(final ReservationDao reservationDao, final TimeDao timeDao) {
        this.reservationDao = reservationDao;
        this.timeDao = timeDao;
    }

    public List<BookResponse> findAvaliableBookList(final LocalDate date, final Long themeId) {
        List<Reservation> reservations = reservationDao.findByDateAndThemeId(date, themeId);
        List<TimeSlot> timeSlots = timeDao.findAll();

        List<BookResponse> availableBooks = timeSlots.stream()
                .map(timeSlot -> new BookResponse(
                        timeSlot.getStartAt(),
                        timeSlot.getId(),
                        reservations.stream()
                                .anyMatch(reservation -> reservation.getTime().getId().equals(timeSlot.getId()))
                ))
                .toList();

        return availableBooks;
    }
}
