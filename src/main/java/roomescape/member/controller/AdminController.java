package roomescape.member.controller;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.member.dto.request.CreateReservationRequest;
import roomescape.member.dto.response.CreateReservationResponse;
import roomescape.member.service.AdminService;
import roomescape.reservation.dto.response.ConfirmReservationResponse;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/reservations")
    public ResponseEntity<CreateReservationResponse> createReservationByAdmin(
            @Valid @RequestBody CreateReservationRequest createReservationRequest) {
        CreateReservationResponse reservation = adminService.createReservation(createReservationRequest);
        return ResponseEntity.created(URI.create("/reservations/" + reservation.id())).body(reservation);
    }

    @PostMapping("/waitings/{id}/confirmation")
    public ResponseEntity<ConfirmReservationResponse> confirmWaiting(@PathVariable Long id) {

        ConfirmReservationResponse confirmReservationResponse = adminService.confirmWaiting(id);
        return ResponseEntity.created(URI.create("/waitings/" + confirmReservationResponse.id()))
                .body(confirmReservationResponse);
    }
}
