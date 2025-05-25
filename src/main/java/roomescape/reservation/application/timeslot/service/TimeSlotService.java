package roomescape.reservation.application.timeslot.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.common.exception.RoomescapeException;
import roomescape.reservation.application.timeslot.dto.TimeSlotAvailabilityInfo;
import roomescape.reservation.application.timeslot.dto.TimeSlotCreateCommand;
import roomescape.reservation.application.timeslot.dto.TimeSlotInfo;
import roomescape.reservation.domain.reservation.Reservation;
import roomescape.reservation.domain.reservation.ReservationRepository;
import roomescape.reservation.domain.timeslot.TimeSlot;
import roomescape.reservation.domain.timeslot.TimeSlotRepository;

@Service
public class TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;
    private final ReservationRepository reservationRepository;

    public TimeSlotService(final TimeSlotRepository timeSlotRepository,
                                  final ReservationRepository reservationRepository) {
        this.timeSlotRepository = timeSlotRepository;
        this.reservationRepository = reservationRepository;
    }

    public TimeSlotInfo createTimeSlot(final TimeSlotCreateCommand command) {
        if (timeSlotRepository.existsByStartAt(command.startAt())) {
            throw new RoomescapeException("이미 존재하는 시간입니다.");
        }
        final TimeSlot timeSlot = command.convertToEntity();
        final TimeSlot savedTimeSlot = timeSlotRepository.save(timeSlot);
        return new TimeSlotInfo(savedTimeSlot);
    }

    public List<TimeSlotInfo> getTimeSlots() {
        return timeSlotRepository.findAll().stream()
                .map(TimeSlotInfo::new)
                .toList();
    }

    public void deleteTimeSlotById(final long id) {
        if (reservationRepository.existsByTimeId(id)) {
            throw new RoomescapeException("예약이 존재하는 시간은 삭제할 수 없습니다.");
        }
        timeSlotRepository.deleteById(id);
    }

    public List<TimeSlotAvailabilityInfo> findAvailableTimes(final LocalDate date, final long themeId) {
        final List<TimeSlot> timeSlots = timeSlotRepository.findAll();
        final List<Reservation> reservations = reservationRepository.findAllByDateAndThemeId(date, themeId);
        return timeSlots.stream()
                .map(time ->
                        new TimeSlotAvailabilityInfo(time.id(), time.startAt(), isAlreadyBooked(time, reservations)))
                .toList();
    }

    private boolean isAlreadyBooked(final TimeSlot timeSlot, final List<Reservation> reservations) {
        return reservations.stream().anyMatch(reservation -> reservation.isSameTime(timeSlot));
    }
}
