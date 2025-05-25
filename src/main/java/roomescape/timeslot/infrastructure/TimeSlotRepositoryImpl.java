package roomescape.timeslot.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.timeslot.domain.ReservationTime;
import roomescape.timeslot.domain.TimeSlot;
import roomescape.timeslot.domain.TimeSlotId;
import roomescape.timeslot.domain.TimeSlotRepository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TimeSlotRepositoryImpl implements TimeSlotRepository {

    private final JpaTimeSlotRepository jpaTimeSlotRepository;

    @Override
    public boolean existsById(final TimeSlotId id) {
        return jpaTimeSlotRepository.existsById(id.getValue());
    }

    @Override
    public boolean existsByStartAt(final ReservationTime startAt) {
        return jpaTimeSlotRepository.existsByStartAt(startAt);
    }

    @Override
    public Optional<TimeSlot> findById(final TimeSlotId id) {
        return jpaTimeSlotRepository.findById(id.getValue());
    }

    @Override
    public List<TimeSlot> findAll() {
        return jpaTimeSlotRepository.findAll();
    }

    @Override
    public TimeSlot save(final TimeSlot timeSlot) {
        return jpaTimeSlotRepository.save(timeSlot);
    }

    @Override
    public void deleteById(final TimeSlotId id) {
        jpaTimeSlotRepository.deleteById(id.getValue());
    }
}
