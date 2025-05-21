package roomescape.waiting.controller;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static roomescape.waiting.controller.response.WaitingSuccessCode.DELETE_WAITING_SUCCESS_CODE;
import static roomescape.waiting.controller.response.WaitingSuccessCode.WAITING_SUCCESS_CODE;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.web.resolver.Authenticated;
import roomescape.global.response.ApiResponse;
import roomescape.reservation.controller.request.ReserveByUserRequest;
import roomescape.reservation.controller.response.ReservationResponse;
import roomescape.reservation.service.command.ReserveCommand;
import roomescape.waiting.service.WaitingService;

@RestController
@RequestMapping("/reservations/waiting")
public class WaitingApiController {

    private final WaitingService waitingService;

    @Autowired
    public WaitingApiController(final WaitingService waitingService) {
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
                .body(ApiResponse.success(WAITING_SUCCESS_CODE, response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> cancelWaiting(
            @PathVariable Long id,
            @Authenticated final Long memberId
    ) {
        waitingService.deleteByUser(id, memberId);

        return ResponseEntity
                .status(NO_CONTENT)
                .body(ApiResponse.success(DELETE_WAITING_SUCCESS_CODE));
    }
}
