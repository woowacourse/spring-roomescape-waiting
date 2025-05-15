package roomescape.reservation.controller;

import static org.springframework.http.HttpStatus.NO_CONTENT;
import static roomescape.reservation.controller.response.ReservationSuccessCode.CANCEL_RESERVATION;
import static roomescape.reservation.controller.response.ReservationSuccessCode.RESERVE;
import static roomescape.reservation.controller.response.ReservationSuccessCode.SEARCH_RESERVATION;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.global.response.ApiResponse;
import roomescape.reservation.controller.request.ReserveByAdminRequest;
import roomescape.reservation.controller.response.ReservationResponse;
import roomescape.reservation.service.ReservationService;
import roomescape.reservation.service.command.ReserveCommand;

@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/reservations")
public class ReservationAdminApiController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ApiResponse<ReservationResponse>> reserve(
            @RequestBody @Valid ReserveByAdminRequest request
    ) {
        ReservationResponse response = reservationService.reserve(
                ReserveCommand.byAdmin(request));

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(RESERVE, response));
    }

    @GetMapping
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

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReservation(@PathVariable Long id) {
        reservationService.deleteById(id);

        return ResponseEntity
                .status(NO_CONTENT)
                .body(ApiResponse.success(CANCEL_RESERVATION));
    }
}
