package roomescape.reservationtime.presentation.controller.guest;

import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservationtime.application.ReservationTimeApplicationService;
import roomescape.reservationtime.presentation.dto.response.AvailableReservationTimeWebResponse;
import roomescape.reservationtime.presentation.dto.response.ReservationTimeWebResponse;

@RestController
public class GuestReservationTimeController {

    private final ReservationTimeApplicationService reservationTimeApplicationService;

    public GuestReservationTimeController(final ReservationTimeApplicationService reservationTimeApplicationService) {
        this.reservationTimeApplicationService = reservationTimeApplicationService;
    }

    @GetMapping("/times")
    public ResponseEntity<List<ReservationTimeWebResponse>> findAll() {
        return ResponseEntity.ok(reservationTimeApplicationService.findAll());
    }

    @GetMapping("/times/available")
    public ResponseEntity<List<AvailableReservationTimeWebResponse>> findAvailable(
            @RequestParam("date") LocalDate date,
            @RequestParam("themeId") Long themeId
    ) {
        return ResponseEntity.ok(reservationTimeApplicationService.findAvailable(date, themeId));
    }
}
