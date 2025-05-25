package roomescape.reservation.infrastructure.jpa.timeslot;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.timeslot.TimeSlot;
import roomescape.reservation.domain.timeslot.TimeSlotRepository;

@Repository
public class TimeSlotRepositoryImpl implements TimeSlotRepository {

    private final TimeSlotJpaRepository timeSlotJpaRepository;

    public TimeSlotRepositoryImpl(final TimeSlotJpaRepository timeSlotJpaRepository) {
        this.timeSlotJpaRepository = timeSlotJpaRepository;
    }

    @Override
    public boolean existsByStartAt(final LocalTime startAt) {
        return timeSlotJpaRepository.existsByStartAt(startAt);
    }

    @Override
    public TimeSlot save(final TimeSlot timeSlot) {
        return timeSlotJpaRepository.save(timeSlot);
    }

    @Override
    public void deleteById(final long id) {
        timeSlotJpaRepository.deleteById(id);
    }

    @Override
    public List<TimeSlot> findAll() {
        return timeSlotJpaRepository.findAll();
    }

    @Override
    public Optional<TimeSlot> findById(final long id) {
        return timeSlotJpaRepository.findById(id);
    }
}
