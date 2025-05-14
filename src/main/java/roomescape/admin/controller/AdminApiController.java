package roomescape.admin.controller;

import static roomescape.reservation.controller.response.ReservationSuccessCode.RESERVE;
import static roomescape.reservation.controller.response.ReservationSuccessCode.SEARCH_RESERVATION;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.admin.controller.request.ReserveByAdminRequest;
import roomescape.global.response.ApiResponse;
import roomescape.reservation.controller.response.ReservationResponse;
import roomescape.reservation.service.ReservationService;
import roomescape.reservation.service.command.ReserveCommand;

@RequiredArgsConstructor
@RestController
@RequestMapping("/admin")
public class AdminApiController {

    private final ReservationService reservationService;

    @PostMapping("/reservations")
    public ResponseEntity<ApiResponse<ReservationResponse>> reserve(
            @RequestBody @Valid ReserveByAdminRequest request
    ) {
        ReservationResponse response = reservationService.reserve(
                ReserveCommand.byAdmin(request));

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(RESERVE, response));
    }

    @GetMapping("/reservations/search")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> searchReservations(
            @RequestParam(required = false) Long themeId,
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to
    ) {
        List<ReservationResponse> responses = reservationService.getFilteredReservations(themeId, memberId, from, to);
        return ResponseEntity.ok(
                ApiResponse.success(SEARCH_RESERVATION, responses));
    }
}
