package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.exception.ExistedReservationException;
import roomescape.exception.ThemeNotFoundException;
import roomescape.exception.TimeSlotNotFoundException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.TimeSlot;
import roomescape.reservation.dto.request.ReservationTimeRequest;
import roomescape.reservation.dto.response.ReservationTimeResponse;
import roomescape.reservation.dto.response.TimeWithBookedResponse;
import roomescape.reservation.infrastructure.ReservationRepository;
import roomescape.reservation.infrastructure.ThemeRepository;
import roomescape.reservation.infrastructure.TimeSlotRepository;

@Service
public class TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;
    private final ReservationRepository reservationRepository;
    private final ThemeRepository themeRepository;

    public TimeSlotService(TimeSlotRepository timeSlotRepository,
                           ReservationRepository reservationRepository, ThemeRepository themeRepository) {
        this.timeSlotRepository = timeSlotRepository;
        this.reservationRepository = reservationRepository;
        this.themeRepository = themeRepository;
    }

    public List<ReservationTimeResponse> findAllTimes() {
        List<TimeSlot> timeSlotDaoAll = timeSlotRepository.findAll();

        return timeSlotDaoAll.stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    public ReservationTimeResponse createTime(ReservationTimeRequest reservationTimeRequest) {
        TimeSlot timeSlot = reservationTimeRequest.toTime();
        TimeSlot savedTimeSlot = timeSlotRepository.save(timeSlot);
        return ReservationTimeResponse.from(savedTimeSlot);
    }

    public void deleteTimeById(Long id) {
        if (timeSlotRepository.findById(id).isEmpty()) {
            throw new TimeSlotNotFoundException();
        }
        if (reservationRepository.findByTimeSlotId(id).size() > 0) {
            throw new ExistedReservationException();
        }
        timeSlotRepository.deleteById(id);
    }

    public List<TimeWithBookedResponse> findTimesByDateAndThemeIdWithBooked(LocalDate date, Long themeId) {
        Theme theme = themeRepository.findById(themeId).orElseThrow(ThemeNotFoundException::new);
        List<Reservation> reservations = reservationRepository.findByDateAndTheme(date, theme);

        List<TimeSlot> bookedTimeSlots = reservations.stream()
                .map(Reservation::getTimeSlot)
                .toList();
        List<TimeSlot> timeSlots = timeSlotRepository.findAll();

        return timeSlots.stream()
                .map(time -> TimeWithBookedResponse.of(time, bookedTimeSlots.contains(time)))
                .toList();
    }
}
