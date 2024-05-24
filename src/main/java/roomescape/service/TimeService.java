package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.TimeSlot;
import roomescape.dto.request.TimeSlotRequest;
import roomescape.dto.response.TimeSlotResponse;
import roomescape.repository.TimeSlotRepository;
import roomescape.validation.TimeSlotValidator;

@Service
public class TimeService {

    private final TimeSlotRepository timeSlotRepository;
    private final TimeSlotValidator timeSlotValidator;

    public TimeService(TimeSlotRepository timeSlotRepository, TimeSlotValidator timeSlotValidator) {
        this.timeSlotRepository = timeSlotRepository;
        this.timeSlotValidator = timeSlotValidator;
    }

    public List<TimeSlotResponse> findAll() {
        return timeSlotRepository.findAll()
                .stream()
                .map(TimeSlotResponse::from)
                .toList();
    }

    public TimeSlotResponse create(TimeSlotRequest timeSlotRequest) {
        TimeSlot timeSlot = timeSlotRequest.toEntity();
        timeSlotValidator.validateDuplicatedTime(timeSlot.getStartAt());
        TimeSlot createdTimeSlot = timeSlotRepository.save(timeSlot);
        return TimeSlotResponse.from(createdTimeSlot);
    }

    public void delete(Long id) {
        TimeSlot timeSlot = timeSlotRepository.getTimeSlotById(id);
        timeSlotValidator.validateExistReservation(timeSlot);
        timeSlotRepository.deleteById(id);
    }
}
