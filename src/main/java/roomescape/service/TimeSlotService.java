package roomescape.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.TimeSlot;
import roomescape.exception.DuplicateException;
import roomescape.exception.NotFoundException;
import roomescape.exception.ResourceInUseException;
import roomescape.repository.ThemeRepository;
import roomescape.repository.TimeSlotRepository;
import roomescape.service.dto.AvailableTimeSlot;

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
        try {
            findTimeSlotById(timeId);
            timeSlotRepository.deleteById(timeId);
        } catch (DataIntegrityViolationException e) {
            throw new ResourceInUseException("예약 시간");
        }
    }

    public List<AvailableTimeSlot> findAvailableTimes(long themeId, LocalDate date) {
        themeRepository.findById(themeId)
                .orElseThrow(() -> new NotFoundException("해당 테마를 찾을 수 없습니다."));
        return timeSlotRepository.findAvailableTimeSlots(themeId, date);
    }

    private void checkDuplicatedStartAt(LocalTime startAt) {
        if (timeSlotRepository.findByStartAt(startAt).isPresent()) {
            throw new DuplicateException("이미 등록된 예약대 시간입니다. (" + startAt + ")");
        }
    }
}
