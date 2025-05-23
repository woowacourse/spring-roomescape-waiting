package roomescape.timeslot.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.timeslot.application.dto.CreateTimeSlotRequest;
import roomescape.timeslot.application.dto.TimeSlotResponse;
import roomescape.timeslot.application.service.ReservationTimeCommandService;
import roomescape.timeslot.application.service.ReservationTimeQueryService;
import roomescape.timeslot.domain.TimeSlotId;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TimeSlotFacadeImpl implements TimeSlotFacade {

    private final ReservationTimeQueryService reservationTimeQueryService;
    private final ReservationTimeCommandService reservationTimeCommandService;

    @Override
    public List<TimeSlotResponse> getAll() {
        return TimeSlotResponse.from(
                reservationTimeQueryService.getAll());
    }

    @Override
    public TimeSlotResponse create(final CreateTimeSlotRequest request) {
        return TimeSlotResponse.from(
                reservationTimeCommandService.create(request));
    }

    @Override
    public void delete(final TimeSlotId id) {
        reservationTimeCommandService.delete(id);
    }
}
