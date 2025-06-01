package roomescape.timeslot.application.service;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.RoomescapeException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.timeslot.application.dto.TimeSlotAvailabilityInfo;
import roomescape.timeslot.application.dto.TimeSlotCreateCommand;
import roomescape.timeslot.application.dto.TimeSlotInfo;
import roomescape.timeslot.domain.TimeSlot;
import roomescape.timeslot.domain.TimeSlotRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;
    private final ReservationRepository reservationRepository;

    @Transactional
    public TimeSlotInfo createTimeSlot(final TimeSlotCreateCommand command) {
        if (timeSlotRepository.existsByStartAt(command.startAt())) {
            throw new RoomescapeException("이미 존재하는 시간입니다.");
        }
        final TimeSlot timeSlot = command.convertToEntity();
        final TimeSlot savedTimeSlot = timeSlotRepository.save(timeSlot);
        return new TimeSlotInfo(savedTimeSlot);
    }

    @Transactional
    public void deleteTimeSlotById(final long id) {
        if (reservationRepository.existsByTimeId(id)) {
            throw new RoomescapeException("예약이 존재하는 시간은 삭제할 수 없습니다.");
        }
        timeSlotRepository.deleteById(id);
    }

    public List<TimeSlotInfo> findTimeSlots() {
        return timeSlotRepository.findAll().stream()
                .map(TimeSlotInfo::new)
                .toList();
    }

    public List<TimeSlotAvailabilityInfo> findAvailableTimeSlots(final LocalDate date, final long themeId) {
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
