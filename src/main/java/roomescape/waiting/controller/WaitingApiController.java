package roomescape.waiting.controller;

import static org.springframework.http.HttpStatus.CREATED;
import static roomescape.waiting.controller.response.WaitingSuccessCode.WAITING_SUCCESS_CODE;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import roomescape.reservation.service.ReservationService;
import roomescape.reservation.service.command.ReserveCommand;
import roomescape.waiting.service.WaitingService;

@RestController
@RequestMapping("/reservations/waiting")
@RequiredArgsConstructor
public class WaitingApiController {

    private final WaitingService waitingService;
    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ApiResponse<ReservationResponse>> createWaiting(
            @RequestBody @Valid ReserveByUserRequest request,
            @Authenticated Long memberId
    ) {
        final ReservationResponse response = reservationService.waiting(
                ReserveCommand.byUser(request, memberId)
        );

        return ResponseEntity
                .status(CREATED)
                .body(ApiResponse.success(WAITING_SUCCESS_CODE, response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> cancelWaiting(
            @PathVariable Long id,
            @Authenticated Long memberId
    ) {
        waitingService.cancelWaiting(id, memberId);

        return ResponseEntity.noContent().build();
    }
}
