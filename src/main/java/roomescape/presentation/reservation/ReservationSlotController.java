package roomescape.presentation.reservation;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.reservation.ReservationSlotService;
import roomescape.application.reservation.response.ReservationSlotsResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reservation-slots")
public class ReservationSlotController {

    private final ReservationSlotService reservationSlotService;

    @GetMapping
    public ResponseEntity<ReservationSlotsResponse> getReservationSlots(
            @RequestParam Long themeId,
            @RequestParam LocalDate date
    ) {
        return ResponseEntity.ok(reservationSlotService.getReservationSlots(themeId, date));
    }
}
