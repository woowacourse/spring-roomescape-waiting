package roomescape.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.controller.dto.TimePatchRequest;
import roomescape.controller.dto.TimeRequest;
import roomescape.domain.TimeSlot;
import roomescape.exception.DuplicateTimeException;
import roomescape.exception.ResourceInUseException;
import roomescape.exception.ThemeNotFoundException;
import roomescape.exception.TimeSlotNotFoundException;
import roomescape.repository.ThemeRepository;
import roomescape.repository.TimeSlotRepository;
import roomescape.service.dto.AvailableTimeSlot;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;
    private final ThemeRepository themeRepository;

    public TimeSlotService(TimeSlotRepository timeSlotRepository, ThemeRepository themeRepository) {
        this.timeSlotRepository = timeSlotRepository;
        this.themeRepository = themeRepository;
    }

    public List<TimeSlot> allTimes() {
        return timeSlotRepository.findAll();
    }

    public TimeSlot findTimeSlotById(long id) {
        return timeSlotRepository.findById(id)
                .orElseThrow(() -> new TimeSlotNotFoundException(id));
    }

    @Transactional
    public TimeSlot saveTime(TimeRequest request) {
        checkDuplicatedStartAt(request.startAt());
        TimeSlot timeSlot = TimeSlot.transientOf(request.startAt());
        return timeSlotRepository.save(timeSlot);
    }

    @Transactional
    public void removeTime(long timeId) {
        try {
            findTimeSlotById(timeId);
            timeSlotRepository.deleteById(timeId);
        } catch (DataIntegrityViolationException e) {
            throw new ResourceInUseException("예약 시간");
        }
    }

    @Transactional
    public void putTime(long id, TimeRequest request) {
        TimeSlot exists = findTimeSlotById(id);
        TimeSlot timeSlot = new TimeSlot(id, request.startAt());
        if (!exists.equals(timeSlot)) {
            checkDuplicatedStartAt(request.startAt());
            timeSlotRepository.update(timeSlot);
        }
    }

    @Transactional
    public void patchTime(long id, TimePatchRequest request) {
        TimeSlot timeSlot = findTimeSlotById(id);
        checkDuplicatedStartAt(request.startAt());
        timeSlot.changeTime(request.startAt());
        timeSlotRepository.update(timeSlot);
    }

    public List<AvailableTimeSlot> findAvailableTimes(long themeId, LocalDate date) {
        themeRepository.findById(themeId)
                .orElseThrow(() -> new ThemeNotFoundException(themeId));
        return timeSlotRepository.findAvailableTimeSlots(themeId, date);
    }

    private void checkDuplicatedStartAt(LocalTime startAt) {
        if (timeSlotRepository.findByStartAt(startAt).isPresent()) {
            throw new DuplicateTimeException(startAt.toString());
        }
    }
}
