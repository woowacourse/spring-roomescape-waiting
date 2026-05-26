package roomescape.domain.reservationslot;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.reservationslot.dto.CreateReservationSlotRequest;
import roomescape.domain.reservationslot.dto.CreateReservationSlotResponse;
import roomescape.domain.reservationslot.dto.UpdateReservationRequest;
import roomescape.domain.reservation.dto.ReservationResponse;

@Validated
@RestController
@RequiredArgsConstructor
public class ReservationSlotController {

    private final ReservationSlotService reservationService;

    @PostMapping("/reservations")
    public ResponseEntity<CreateReservationSlotResponse> createReservation(
        @Valid @RequestBody CreateReservationSlotRequest request
    ) {
        CreateReservationSlotResponse response = reservationService.createReservation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/reservations")
    public ResponseEntity<ReservationResponse> getUserReservations(
        @RequestParam
        @NotBlank(message = "예약자 이름은 필수 입력값 입니다.")
        String name
    ) {
        ReservationResponse response = reservationService.getUserReservations(name);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> cancelUserReservation(
        @PathVariable Long id
    ) {
        reservationService.cancelUserReservation(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/reservations/{id}")
    public ResponseEntity<Void> updateReservation(
        @PathVariable Long id,
        @RequestBody UpdateReservationRequest request
    ) {
        reservationService.updateReservation(id, request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
