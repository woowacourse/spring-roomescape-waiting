package roomescape.presentation.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.WaitingService;
import roomescape.application.auth.dto.MemberIdDto;
import roomescape.application.dto.ReservationCreateServiceRequest;
import roomescape.application.dto.WaitingServiceResponse;
import roomescape.infrastructure.AuthenticatedMemberId;
import roomescape.presentation.controller.dto.UserReservationCreateRequest;

@RestController
@RequestMapping("/waiting")
public class WaitingController {

    private final WaitingService waitingService;

    public WaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping
    public ResponseEntity<WaitingServiceResponse> createWaiting(
            @Valid @RequestBody UserReservationCreateRequest request,
            @AuthenticatedMemberId MemberIdDto memberIdDto
    ) {
        ReservationCreateServiceRequest reservationRequest = ReservationCreateServiceRequest.of(
                request,
                memberIdDto.id()
        );
        WaitingServiceResponse waitingServiceResponse = waitingService.registerWaiting(reservationRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(waitingServiceResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWaiting(@PathVariable("id") Long id) {
        waitingService.deleteWaiting(id);
        return ResponseEntity.noContent().build();
    }
}
