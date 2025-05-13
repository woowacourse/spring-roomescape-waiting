package roomescape.reservation.presentation;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.login.presentation.dto.SearchCondition;
import roomescape.auth.login.presentation.dto.annotation.LoginAdmin;
import roomescape.auth.login.presentation.dto.LoginAdminInfo;
import roomescape.reservation.presentation.dto.AdminReservationRequest;
import roomescape.reservation.presentation.dto.ReservationRequest;
import roomescape.reservation.presentation.dto.ReservationResponse;
import roomescape.reservation.service.ReservationService;

@RestController
public class AdminReservationController {

    private final ReservationService reservationService;

    public AdminReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/admin/reservations")
    public ResponseEntity<ReservationResponse> createReservation(@RequestBody AdminReservationRequest request)
    {
        ReservationResponse response = reservationService.createReservation(
                new ReservationRequest(request.date(), request.timeId(), request.themeId()),
                request.memberId()
        );

        return ResponseEntity.created(URI.create("/admin/reservation")).body(response);
    }


    @GetMapping("/user-reservation")
    public ResponseEntity<List<ReservationResponse>> reservationFilter(@ModelAttribute SearchCondition condition) {
        List<ReservationResponse> responses = reservationService.searchReservationWithCondition(condition);

        return ResponseEntity.ok().body(responses);
    }
}
