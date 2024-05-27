package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;
import roomescape.dto.response.BookResponse;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.TimeSlotRepository;

@Service
@Transactional
public class BookService {

    private final ReservationRepository reservationRepository;
    private final ThemeRepository themeRepository;
    private final TimeSlotRepository timeSlotRepository;

    public BookService(ReservationRepository reservationRepository,
                       ThemeRepository themeRepository,
                       TimeSlotRepository timeSlotRepository) {
        this.reservationRepository = reservationRepository;
        this.themeRepository = themeRepository;
        this.timeSlotRepository = timeSlotRepository;
    }

    public List<BookResponse> findAvailableBookList(LocalDate date, Long themeId) {
        Theme theme = themeRepository.getThemeById(themeId);
        List<TimeSlot> timeSlots = timeSlotRepository.findAll();
        List<Reservation> reservations = reservationRepository.findAllByDateAndTheme(date, theme);
        return timeSlots.stream()
                .map(timeSlot -> new BookResponse(
                        timeSlot.getStartAt(),
                        timeSlot.getId(),
                        isAlreadyBooked(timeSlot, reservations)))
                .toList();
    }

    private boolean isAlreadyBooked(TimeSlot timeSlot, List<Reservation> reservations) {
        return reservations.stream()
                .anyMatch(reservation -> reservation.hasSameTime(timeSlot));
    }
}
