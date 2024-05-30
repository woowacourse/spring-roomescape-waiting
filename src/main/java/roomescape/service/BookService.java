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
@Transactional(readOnly = true)
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

    public List<BookResponse> findAvaliableBooks(LocalDate date, Long themeId) {
        Theme theme = findThemeById(themeId);
        List<Reservation> reservations = reservationRepository.findAllByDateAndTheme(date, theme);
        List<TimeSlot> timeSlots = timeSlotRepository.findAll();

        return timeSlots.stream()
                .map(timeSlot -> new BookResponse(
                        timeSlot.getStartAt(),
                        timeSlot.getId(),
                        reservations.stream()
                                .anyMatch(reservation -> reservation.getTime().getId().equals(timeSlot.getId()))
                ))
                .toList();
    }

    private Theme findThemeById(long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new IllegalArgumentException("[ERROR] 존재하지 않는 테마 입니다"));
    }
}
