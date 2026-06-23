package roomescape.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.timeslot.TimeSlot;
import roomescape.exception.DuplicateException;
import roomescape.exception.NotFoundException;
import roomescape.exception.ResourceInUseException;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.theme.ThemeRepository;
import roomescape.domain.timeslot.TimeSlotRepository;
import roomescape.service.dto.AvailableTimeSlot;

@Service
@Transactional(readOnly = true)
public class TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;
    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public TimeSlotService(TimeSlotRepository timeSlotRepository, ThemeRepository themeRepository,
                           ReservationRepository reservationRepository) {
        this.timeSlotRepository = timeSlotRepository;
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<TimeSlot> findAllTimes() {
        return timeSlotRepository.findAll();
    }

    public TimeSlot getTimeSlotById(long id) {
        return timeSlotRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("해당 시간대를 찾을 수 없습니다."));
    }

    @Transactional
    public TimeSlot saveTime(LocalTime startAt) {
        checkDuplicatedStartAt(startAt);
        TimeSlot timeSlot = new TimeSlot(startAt);
        return timeSlotRepository.save(timeSlot);
    }

    @Transactional
    public void removeTime(long timeId) {
        getTimeSlotById(timeId);
        validateTimeSlotDeletable(timeId);
        timeSlotRepository.deleteById(timeId);
    }

    public List<AvailableTimeSlot> getAvailableTimes(long themeId, LocalDate date) {
        themeRepository.findById(themeId)
                .orElseThrow(() -> new NotFoundException("해당 테마를 찾을 수 없습니다."));
        return timeSlotRepository.findAvailableTimeSlots(themeId, date);
    }

    private void checkDuplicatedStartAt(LocalTime startAt) {
        if (timeSlotRepository.findByStartAt(startAt).isPresent()) {
            throw new DuplicateException("이미 등록된 예약 시간대입니다. (" + startAt + ")");
        }
    }

    private void validateTimeSlotDeletable(long timeId) {
        if (reservationRepository.existsByTimeId(timeId)) {
            throw new ResourceInUseException("예약 시간");
        }
    }
}
