package roomescape.repository;

import roomescape.domain.TimeSlot;
import roomescape.service.dto.AvailableTimeSlot;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface TimeSlotRepository {

    List<TimeSlot> findAll();

    Optional<TimeSlot> findById(long id);

    Optional<TimeSlot> findByStartAt(LocalTime startAt);

    List<AvailableTimeSlot> findAvailableTimeSlots(long themeId, LocalDate date);

    TimeSlot save(TimeSlot timeSlot);

    void deleteById(long id);

    TimeSlot update(TimeSlot timeSlot);
}
