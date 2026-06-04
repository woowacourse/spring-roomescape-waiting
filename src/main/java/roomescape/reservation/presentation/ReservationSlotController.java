package roomescape.reservation.presentation;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservation.application.ReservationSlotService;
import roomescape.reservation.presentation.response.ReservationSlotResponse;

@RestController
@RequiredArgsConstructor
public class ReservationSlotController {

    private final ReservationSlotService reservationSlotService;

    @GetMapping("/reservation-slots")
    public ResponseEntity<List<ReservationSlotResponse>> getReservationTimeAvailability(
        @RequestParam Long themeId,
        @RequestParam Long dateId
    ) {
        List<ReservationSlotResponse> response = reservationSlotService.getReservationSlots(themeId, dateId);
        return ResponseEntity.ok(response);
    }
}
