package roomescape.reservation.controller;

import static org.springframework.http.HttpStatus.CREATED;
import static roomescape.reservation.controller.response.ReservationSuccessCode.GET_MY_RESERVATIONS;
import static roomescape.reservation.controller.response.ReservationSuccessCode.RESERVE;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.web.resolver.Authenticated;
import roomescape.global.response.ApiResponse;
import roomescape.reservation.controller.request.ReserveByUserRequest;
import roomescape.reservation.controller.response.MyReservationResponse;
import roomescape.reservation.controller.response.ReservationResponse;
import roomescape.reservation.service.ReservationQueryService;
import roomescape.reservation.service.ReservationService;
import roomescape.reservation.service.command.ReserveCommand;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationApiController {

    private final ReservationService reservationService;
    private final ReservationQueryService reservationQueryService;

    @PostMapping
    public ResponseEntity<ApiResponse<ReservationResponse>> createReservation(
            @RequestBody @Valid ReserveByUserRequest request,
            @Authenticated Long memberId
    ) {
        ReservationResponse response = reservationService.reserve(
                ReserveCommand.byUser(request, memberId)
        );

        return ResponseEntity
                .status(CREATED)
                .body(ApiResponse.success(RESERVE, response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<MyReservationResponse>>> getMyReservations(
            @Authenticated Long memberId
    ) {
        List<MyReservationResponse> responses = reservationQueryService.getAllReservations(memberId);
        return ResponseEntity.ok(
                ApiResponse.success(GET_MY_RESERVATIONS, responses)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReservation(@PathVariable Long id, @Authenticated Long memberId) {
        reservationService.deleteByUser(id, memberId);

        return ResponseEntity.noContent().build();
    }
}
