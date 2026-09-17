package roomescape.controller;

import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.MyReservationResponse;
import roomescape.service.ReservationService;

@RestController
@Validated
public class MyReservationController {

    private static final String MEMBER_NAME_HEADER = "X-Member-Name";

    private final ReservationService reservationService;

    public MyReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/reservations-mine")
    public ResponseEntity<MyReservationResponse> getMyReservations(
            @NotBlank @RequestHeader(MEMBER_NAME_HEADER) String memberName
    ) {
        return ResponseEntity.ok(reservationService.findReservationBy(memberName));
    }
}
