package roomescape.controller;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import roomescape.annotation.CheckRole;
import roomescape.dto.request.CreateWaitingRequest;
import roomescape.dto.request.LoginMemberRequest;
import roomescape.dto.response.WaitingResponse;
import roomescape.entity.WaitingReservation;
import roomescape.global.Role;
import roomescape.service.ReservationService;

@RestController
@RequestMapping("/waiting")
public class WaitingController {

    private final ReservationService waitingService;

    public WaitingController(ReservationService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping
    @CheckRole(value = {Role.ADMIN, Role.USER})
    public ResponseEntity<WaitingResponse> addWaiting(
            @RequestBody @Valid CreateWaitingRequest request,
            LoginMemberRequest loginMemberRequest
    ) {
        WaitingReservation waitingReservation = waitingService.addWaiting(request, loginMemberRequest);
        WaitingResponse response = WaitingResponse.from(waitingReservation);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @DeleteMapping("/{id}")
    @CheckRole(value = {Role.ADMIN, Role.USER})
    public ResponseEntity<Void> deleteWaiting(@PathVariable("id") Long id) {
        waitingService.deleteWaiting(id);
        return ResponseEntity.noContent().build();

    }
}
