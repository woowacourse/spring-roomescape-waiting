package roomescape.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.auth.LoginMember;
import roomescape.auth.Role;
import roomescape.domain.Member;
import roomescape.dto.ReservationResult;
import roomescape.dto.StoreReservationResult;
import roomescape.dto.request.ReservationUpdateRequest;
import roomescape.dto.response.ReservationResponse;
import roomescape.dto.response.StoreReservationResponse;
import roomescape.service.ReservationService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/store/reservations")
public class AdminStoreReservationController {

    private final ReservationService reservationService;

    public AdminStoreReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public ResponseEntity<List<StoreReservationResponse>> getStoreReservations(
            @LoginMember(role = Role.MANAGER) Member manager) {
        List<StoreReservationResult> reservations = reservationService.findByStoreId(manager.getStoreId());
        return ResponseEntity.ok().body(StoreReservationResponse.fromAll(reservations));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ReservationResponse> updateReservationByManager(
            @PathVariable Long id,
            @Valid @RequestBody ReservationUpdateRequest reservationUpdateRequest,
            @LoginMember(role = Role.MANAGER) Member manager) {
        ReservationResult reservation = reservationService.updateByManager(
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
