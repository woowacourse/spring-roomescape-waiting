package roomescape.presentation.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import roomescape.business.service.ReservationService;
import roomescape.presentation.dto.ReservationRequest;
import roomescape.presentation.dto.ReservationResponse;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ReservationService reservationService;

    public AdminController(final ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public String home() {
        return "admin/index";
    }

    @GetMapping("/reservation")
    public String reservations() {
        return "admin/reservation-new";
    }

    @GetMapping("/time")
    public String time() {
        return "admin/time";
    }

    @GetMapping("/theme")
    public String theme() {
        return "admin/theme";
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> createReservation(
            @RequestBody final ReservationRequest reservationRequest
    ) {
        final ReservationResponse reservationResponse = reservationService.insert(reservationRequest.date(),
                reservationRequest.memberId(),
                reservationRequest.timeId(),
                reservationRequest.themeId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationResponse);
    }
}
