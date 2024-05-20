package roomescape.admin.controller;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.admin.dto.AdminReservationRequest;
import roomescape.admin.dto.ReservationFilterRequest;
import roomescape.auth.annotation.Auth;
import roomescape.member.role.MemberRole;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.service.ReservationService;

@RestController
@Auth(roles = MemberRole.ADMIN)
public class AdminController {

    private final ReservationService reservationService;

    public AdminController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/admin/reservations")
    public ResponseEntity<Void> reservationSave(@RequestBody AdminReservationRequest adminReservationRequest) {
        reservationService.addAdminReservation(adminReservationRequest);

        return ResponseEntity.created(URI.create("/admin/reservations/" + adminReservationRequest.memberId()))
                .build();
    }

    @GetMapping("/admin/reservations")
    public List<ReservationResponse> reservationFilteredList(
            @ModelAttribute ReservationFilterRequest reservationFilterRequest) {
        return reservationService.findFilteredReservations(reservationFilterRequest);
    }
}
