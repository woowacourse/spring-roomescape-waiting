package roomescape.reservation.controller;

import static org.springframework.http.HttpStatus.CREATED;
import static roomescape.reservation.controller.response.ReservationSuccessCode.RESERVE;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.web.resolver.Authenticated;
import roomescape.global.response.ApiResponse;
import roomescape.reservation.controller.request.ReserveByUserRequest;
import roomescape.reservation.controller.response.ReservationResponse;
import roomescape.reservation.service.ReservationWaitingService;
import roomescape.reservation.service.command.ReserveCommand;

@RestController
@RequestMapping("/reservations/waiting")
public class ReservationWaitingApiController {

    private final ReservationWaitingService waitingService;

    @Autowired
    public ReservationWaitingApiController(final ReservationWaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReservationResponse>> createWaiting(
            @RequestBody @Valid final ReserveByUserRequest request,
            @Authenticated final Long memberId
    ) {
        final ReservationResponse response = waitingService.waiting(
                ReserveCommand.byUser(request, memberId)
        );

        return ResponseEntity
                .status(CREATED)
                .body(ApiResponse.success(RESERVE, response));
    }
}
