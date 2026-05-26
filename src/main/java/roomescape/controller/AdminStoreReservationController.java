package roomescape.controller;

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
import roomescape.auth.Role;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.dto.request.ReservationUpdateRequest;
import roomescape.dto.response.ReservationResponse;
import roomescape.service.ReservationService;

@RestController
@RequestMapping("/api/v1/admin/store/reservations")
public class AdminStoreReservationController {

    private final ReservationService reservationService;

    public AdminStoreReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getStoreReservations(
            @LoginMember(role = Role.MANAGER) Member manager) {
        List<Reservation> reservations = reservationService.findByStoreId(manager.getStoreId());
        return ResponseEntity.ok().body(ReservationResponse.fromAll(reservations));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ReservationResponse> updateReservationByManager(
            @PathVariable Long id,
            @Valid @RequestBody ReservationUpdateRequest reservationUpdateRequest,
            @LoginMember(role = Role.MANAGER) Member manager) {
        Reservation reservation = reservationService.updateByManager(
                id, reservationUpdateRequest.date(), reservationUpdateRequest.timeId(), manager
        );
        return ResponseEntity.ok().body(ReservationResponse.from(reservation));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservationByManager(
            @PathVariable Long id,
            @LoginMember(role = Role.MANAGER) Member manager) {
        reservationService.deleteByManager(id, manager);
        return ResponseEntity.noContent().build();
    }
}
