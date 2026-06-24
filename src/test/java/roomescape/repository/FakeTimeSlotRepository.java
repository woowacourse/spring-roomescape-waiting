package roomescape.repository;

import roomescape.domain.timeslot.TimeSlot;
import roomescape.domain.timeslot.TimeSlotRepository;
import roomescape.service.dto.AvailableTimeSlot;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FakeTimeSlotRepository implements TimeSlotRepository {

    private final Map<Long, TimeSlot> storage = new HashMap<>();
    private long sequence = 1L;

    @Override
    public List<TimeSlot> findAll() {
        return List.copyOf(storage.values());
    }

    @Override
    public Optional<TimeSlot> findById(long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<AvailableTimeSlot> findAvailableTimeSlots(long themeId, LocalDate date) {
        return List.of();
    }

    @Override
    public TimeSlot save(TimeSlot timeSlot) {
        long id = sequence++;
        TimeSlot savedTimeSlot = new TimeSlot(id, timeSlot.getStartAt());
        storage.put(id, savedTimeSlot);
        return savedTimeSlot;
    }

    @Override
    public void deleteById(long id) {
        storage.remove(id);
    }

    @Override
    public Optional<TimeSlot> findByStartAt(LocalTime startAt) {
        return storage.values().stream()
                .filter(timeSlot -> timeSlot.getStartAt().equals(startAt))
                .findFirst();
    }
}
