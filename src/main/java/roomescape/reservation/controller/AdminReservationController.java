package roomescape.reservation.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.global.auth.annotation.RoleRequired;
import roomescape.member.entity.RoleType;
import roomescape.reservation.dto.request.ReservationAdminCreateRequest;
import roomescape.reservation.dto.request.ReservationReadFilteredRequest;
import roomescape.reservation.dto.response.ReservationAdminCreateResponse;
import roomescape.reservation.dto.response.ReservationReadFilteredResponse;
import roomescape.reservation.dto.response.ReservationReadResponse;
import roomescape.reservation.service.ReservationService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/reservations")
public class AdminReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @RoleRequired(roleType = RoleType.ADMIN)
    public ResponseEntity<ReservationAdminCreateResponse> createReservationByAdmin(
            @RequestBody @Valid ReservationAdminCreateRequest request
    ) {
        ReservationAdminCreateResponse response = reservationService.createReservationByAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @RoleRequired(roleType = RoleType.ADMIN)
    public ResponseEntity<List<ReservationReadResponse>> getAllReservations() {
        List<ReservationReadResponse> responses = reservationService.getAllReservations();
        return ResponseEntity.ok().body(responses);
    }

    @GetMapping("/filtered")
    @RoleRequired(roleType = RoleType.ADMIN)
    public ResponseEntity<List<ReservationReadFilteredResponse>> getFilteredReservationsByAdmin(
            @ModelAttribute @Valid ReservationReadFilteredRequest request
    ) {
        List<ReservationReadFilteredResponse> responses = reservationService.getFilteredReservations(request);
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{id}")
    @RoleRequired(roleType = RoleType.ADMIN)
    public ResponseEntity<Void> deleteReservationByAdmin(
            @PathVariable("id") long id
    ) {
        reservationService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }
}
