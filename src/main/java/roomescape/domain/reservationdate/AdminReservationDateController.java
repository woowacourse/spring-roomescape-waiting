package roomescape.domain.reservationdate;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.reservationdate.dto.AdminReservationDateResponse;
import roomescape.domain.reservationdate.dto.ReservationDateCreationRequest;
import roomescape.domain.reservationdate.dto.ReservationDateCreationResponse;

@RestController
@RequiredArgsConstructor
public class AdminReservationDateController {

    private final ReservationDateService reservationDateService;

    @GetMapping("/admin/reservation-dates")
    public ResponseEntity<List<AdminReservationDateResponse>> getAllReservationDateForAdmin(
    ) {
        List<AdminReservationDateResponse> response = reservationDateService.getAllReservationDateForAdmin();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/reservation-dates")
    public ResponseEntity<ReservationDateCreationResponse> createReservationDate(
        @Valid @RequestBody ReservationDateCreationRequest createReservationDateRequest
    ) {
        ReservationDateCreationResponse response = reservationDateService
            .createReservationDate(createReservationDateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/admin/reservation-dates/{id}")
    public ResponseEntity<Void> deleteReservationDate(@PathVariable Long id) {
        reservationDateService.deleteReservationDate(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
