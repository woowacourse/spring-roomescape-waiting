package roomescape.timeslot.ui;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.aop.RequiredRoles;
import roomescape.timeslot.application.TimeSlotFacade;
import roomescape.timeslot.application.dto.TimeSlotResponse;
import roomescape.user.domain.UserRole;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequiredRoles(UserRole.NORMAL)
@RequestMapping("/times")
public class TimeSlotController {

    private final TimeSlotFacade timeSlotFacade;

    @GetMapping
    public ResponseEntity<List<TimeSlotResponse>> getAll() {
        final List<TimeSlotResponse> timeSlotResponse = timeSlotFacade.getAll();
        return ResponseEntity.ok(timeSlotResponse);
    }
}
