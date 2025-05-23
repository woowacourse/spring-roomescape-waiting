package roomescape.timeslot.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.timeslot.domain.TimeSlot;
import roomescape.timeslot.domain.TimeSlotId;
import roomescape.timeslot.domain.TimeSlotRepository;
import roomescape.timeslot.domain.ReservationTime;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationTimeQueryService {

    private final TimeSlotRepository timeSlotRepository;

    public TimeSlot get(final TimeSlotId id) {
        return timeSlotRepository.findById(id)
                .orElseThrow(NoSuchElementException::new);
    }

    public List<TimeSlot> getAll() {
        return timeSlotRepository.findAll();
    }

    public boolean existsByStartAt(final ReservationTime startAt) {
        return timeSlotRepository.existsByStartAt(startAt);
    }

    public boolean existById(final TimeSlotId id) {
        return timeSlotRepository.existsById(id);
    }
}
