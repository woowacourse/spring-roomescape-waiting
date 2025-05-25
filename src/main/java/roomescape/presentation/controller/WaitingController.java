package roomescape.presentation.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.WaitingService;
import roomescape.application.auth.dto.MemberIdDto;
import roomescape.application.dto.ReservationCreateServiceRequest;
import roomescape.application.dto.WaitingServiceResponse;
import roomescape.infrastructure.AuthenticatedMemberId;
import roomescape.presentation.controller.dto.UserReservationCreateRequest;

@RestController
public class WaitingController {

    private final WaitingService waitingService;

    public WaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping("/waiting")
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
}
