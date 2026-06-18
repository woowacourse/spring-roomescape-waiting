package roomescape.reservation.web;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.LoginMember;
import roomescape.auth.service.ReservationAuthorizationService;
import roomescape.member.Member;
import roomescape.reservation.Reservation;
import roomescape.reservation.service.AdminReservationService;

@RestController
@RequestMapping("/manager/reservations")
public class ManagerReservationController {
    private final AdminReservationService reservationService;
    private final ReservationAuthorizationService authorizationService;

    public ManagerReservationController(
            AdminReservationService reservationService,
            ReservationAuthorizationService authorizationService
    ) {
        this.reservationService = reservationService;
        this.authorizationService = authorizationService;
    }

    @GetMapping
    public ResponseEntity<List<AdminReservationResponseDto>> findAll(@LoginMember Member manager) {
        List<AdminReservationResponseDto> responses = reservationService.findAllByStoreId(manager.getStoreId())
                .stream()
                .map(AdminReservationResponseDto::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<AdminReservationResponseDto> patch(
            @PathVariable Long id,
            @LoginMember Member manager,
            @Valid @RequestBody ReservationPatchDto request
    ) {
        authorizationService.validateManagerCanAccess(manager, id);
        Reservation updated = reservationService.update(id, request);
        return ResponseEntity.ok(AdminReservationResponseDto.from(updated));
    }

    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable Long id, @LoginMember Member manager) {
        authorizationService.validateManagerCanAccess(manager, id);
        reservationService.cancelByAdmin(id);
        return ResponseEntity.noContent().build();
    }
}
