package roomescape.controller.reservationtime;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.reservationtime.dto.AvailableReservationTimeRequest;
import roomescape.controller.reservationtime.dto.ReservationTimeResponse;
import roomescape.service.reservationtime.ReservationTimeService;

@RestController
@RequestMapping("/themes/{themeId}/times")
public class ReservationTimeController {

    private final ReservationTimeService reservationTimeService;

    public ReservationTimeController(final ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @GetMapping("/available")
    public ResponseEntity<List<ReservationTimeResponse>> getAvailableTimes(
            @PathVariable final Long themeId,
            @Valid @ModelAttribute final AvailableReservationTimeRequest availableReservationTimeRequest
    ) {
        return ResponseEntity.ok()
                .body(reservationTimeService.findAvailableTimes(
                                availableReservationTimeRequest.date(),
                                themeId,
                                LocalDateTime.now()
                        ).stream()
                        .map(ReservationTimeResponse::from)
                        .toList());
    }
}
