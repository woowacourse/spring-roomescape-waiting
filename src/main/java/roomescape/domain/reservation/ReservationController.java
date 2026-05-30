package roomescape.domain.reservation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
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
import roomescape.domain.reservation.dto.MyReservationsResponse;
import roomescape.domain.reservation.dto.ReservationFixRequest;
import roomescape.domain.reservation.dto.ReservationRequest;
import roomescape.domain.reservation.dto.ReservationResponse;
import roomescape.domain.reservationtime.dto.TimeResponse;

@Validated
@RestController
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> createReservation(
        @RequestBody @Valid ReservationRequest request
    ) {
        ReservationResponse response = reservationService.createReservation(request);
        URI location = URI.create("/reservations/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<TimeResponse>> getReservations(
        @RequestParam LocalDate date, @RequestParam Long themeId
    ) {
        List<TimeResponse> responses = reservationService.getReservations(date, themeId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/reservations/mine")
    public ResponseEntity<MyReservationsResponse> getMyReservations(
        @RequestParam
        @NotBlank(message = "이름은 필수 입력 값입니다.")
        @Size(max = 100, message = "이름은 100자 이하여야 합니다.")
        String name
    ) {
        MyReservationsResponse response = reservationService.getMyReservations(name);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> deleteReservation(
        @PathVariable Long id, @RequestParam String name
    ) {
        reservationService.deleteReservation(id, name);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/reservation/{id}")
    public ResponseEntity<Void> updateMyReservation(
        @PathVariable Long id,
        @RequestBody @Valid ReservationFixRequest request
    ) {
        reservationService.updateMyReservation(id, request);
        return ResponseEntity.noContent().build();
    }
}
