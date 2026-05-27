package roomescape.reservation.presentation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.common.api.ApiResponse;
import roomescape.reservation.application.ReservationService;
import roomescape.reservation.dto.request.ReservationUpdateRequest;
import roomescape.reservation.dto.response.ReservationDetailFindResponse;
import roomescape.reservation.dto.response.ReservationSaveResponse;

import java.util.List;

@RestController
@RequestMapping("/api/manager/reservations")
@RequiredArgsConstructor
public class ManagerReservationController {

    private final ReservationService reservationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReservationDetailFindResponse>>> findReservationDetails() {
        List<ReservationDetailFindResponse> responses = reservationService.findReservationDetails();
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(responses));
    }

    @DeleteMapping("/{reservationId}")
    public ResponseEntity<ApiResponse<Void>> deleteByManager(
            @PathVariable @Positive long reservationId
    ) {
        reservationService.deleteById(reservationId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.success(null));
    }

    @PatchMapping("/{reservationId}")
    public ResponseEntity<ApiResponse<ReservationSaveResponse>> updateByManager(
            @RequestBody @Valid ReservationUpdateRequest request,
            @PathVariable @Positive long reservationId
    ) {
        ReservationSaveResponse response = reservationService.update(request, reservationId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(response));
    }
}
