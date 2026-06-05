package roomescape.reservation.adapter.in.web;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.common.api.ApiResponse;
import roomescape.reservation.application.port.in.CancelReservationUseCase;
import roomescape.reservation.application.port.in.CreateReservationUseCase;
import roomescape.reservation.application.port.in.FindReservationUseCase;
import roomescape.reservation.application.port.in.FindReservationUseCase;
import roomescape.reservation.application.dto.response.ReservationDetailFindResponse;

import java.util.List;

@RestController
@RequestMapping("/api/manager/reservations")
@RequiredArgsConstructor
public class ManagerReservationController {

    private final FindReservationUseCase findReservationUseCase;
    private final CancelReservationUseCase cancelReservationUseCase;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReservationDetailFindResponse>>> findReservationDetails() {
        List<ReservationDetailFindResponse> responses = findReservationUseCase.findReservationDetails();
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(responses));
    }

    @DeleteMapping("/{reservationId}")
    public ResponseEntity<ApiResponse<Void>> deleteByManager(
            @PathVariable @Positive long reservationId
    ) {
        cancelReservationUseCase.deleteById(reservationId);
        return ResponseEntity.noContent().build();
    }

}
