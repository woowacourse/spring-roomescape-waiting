package roomescape.reservation.time.presentation;

import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.global.auth.Auth;
import roomescape.member.domain.Role;
import roomescape.reservation.time.application.ReservationTimeService;
import roomescape.reservation.time.presentation.dto.AvailableReservationTimeResponse;
import roomescape.reservation.time.presentation.dto.ReservationTimeResponse;

@RestController
@RequestMapping("/times")
public class ReservationTimeController {

    private final ReservationTimeService reservationTimeService;

    public ReservationTimeController(final ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @Auth(Role.USER)
    @GetMapping
    public ResponseEntity<List<ReservationTimeResponse>> getReservationTimes(
    ) {
        return ResponseEntity.ok().body(
                reservationTimeService.getReservationTimes()
        );
    }

    @Auth(Role.USER)
    @GetMapping("/available")
    public ResponseEntity<List<AvailableReservationTimeResponse>> getReservationTimes(
            @RequestParam LocalDate date,
            @RequestParam Long themeId
    ) {
        return ResponseEntity.ok().body(
                reservationTimeService.getReservationTimes(date, themeId)
        );
    }
}
