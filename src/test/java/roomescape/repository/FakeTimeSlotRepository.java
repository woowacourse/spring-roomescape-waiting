package roomescape.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import roomescape.domain.TimeSlot;
import roomescape.repository.TimeSlotRepository.AvailableTimeSlotView;

public class FakeTimeSlotRepository extends AbstractFakeRepository<TimeSlot, Long> implements TimeSlotRepository {

    @Override
    protected Long getId(TimeSlot entity) {
        return entity.getId();
    }

    @Override
    protected TimeSlot withId(TimeSlot entity, Long id) {
        return new TimeSlot(id, entity.getStartAt());
    }

    @Override
    public Optional<TimeSlot> findByStartAt(LocalTime startAt) {
        return store.values().stream()
                .filter(t -> t.getStartAt().equals(startAt))
                .findFirst();
    }

    @Override
    public List<AvailableTimeSlotView> findAvailableSlotViews(long themeId, LocalDate date) {
        return List.of();
    }
}
