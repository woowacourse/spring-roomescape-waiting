package roomescape.domain.reservationtime;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.reservationtime.dto.ReservationTimeAvailabilityResponse;

@RestController
@RequiredArgsConstructor
public class ReservationTimeController {

    private final ReservationTimeService reservationTimeService;

    @GetMapping("/reservation-times/availability")
    public ResponseEntity<List<ReservationTimeAvailabilityResponse>> getReservationTimeAvailability(
        @RequestParam Long themeId,
        @RequestParam Long dateId
    ) {
        List<ReservationTimeAvailabilityResponse> response = reservationTimeService
            .getReservationTimeAvailability(themeId, dateId);
        return ResponseEntity.ok(response);
    }
}
