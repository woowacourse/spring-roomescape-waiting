package roomescape.timeslot.application;

import roomescape.timeslot.application.dto.CreateTimeSlotRequest;
import roomescape.timeslot.application.dto.TimeSlotResponse;
import roomescape.timeslot.domain.TimeSlotId;

import java.util.List;

public interface TimeSlotFacade {

    List<TimeSlotResponse> getAll();

    TimeSlotResponse create(CreateTimeSlotRequest request);

    void delete(TimeSlotId id);
}
