package roomescape.domain.reservationdate;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.reservationdate.dto.ReservationDateResponse;

@RestController
@RequiredArgsConstructor
public class ReservationDateController {

    private final ReservationDateService reservationDateService;

    @GetMapping("/reservation-dates")
    public ResponseEntity<List<ReservationDateResponse>> getAllReservationDates() {
        List<ReservationDateResponse> responses = reservationDateService.getAllAvailableReservationDate();
        return ResponseEntity.ok(responses);
    }
}
