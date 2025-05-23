package roomescape.timeslot.ui;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import roomescape.auth.aop.RequiredRoles;
import roomescape.timeslot.application.TimeSlotFacade;
import roomescape.timeslot.application.dto.TimeSlotResponse;
import roomescape.timeslot.domain.TimeSlotId;
import roomescape.timeslot.ui.dto.CreateTimeSlotWebRequest;
import roomescape.user.domain.UserRole;

import java.net.URI;

@RequiredArgsConstructor
@RestController
@RequiredRoles(UserRole.ADMIN)
@RequestMapping("/times")
public class AdminTimeSlotController {

    private final TimeSlotFacade timeSlotFacade;

    @PostMapping
    public ResponseEntity<TimeSlotResponse> create(
            @RequestBody final CreateTimeSlotWebRequest request) {
        final TimeSlotResponse response = timeSlotFacade.create(request.toServiceRequest());

        final URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.timeId())
                .toUri();

        return ResponseEntity.created(location)
                .body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable final Long id) {
        timeSlotFacade.delete(TimeSlotId.from(id));
        return ResponseEntity.noContent().build();
    }
}
