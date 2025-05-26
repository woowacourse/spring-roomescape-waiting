package roomescape.reservation.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservation.dto.request.AdminReservationCreateRequest;
import roomescape.reservation.dto.request.ReservationSearchConditionRequest;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.service.AdminReservationFacade;

@RestController
@RequestMapping("/admin/reservations")
public class AdminReservationController {
    private final AdminReservationFacade adminReservationService;

    public AdminReservationController(AdminReservationFacade adminReservationService) {
        this.adminReservationService = adminReservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> create(
        @RequestBody @Valid AdminReservationCreateRequest adminReservationCreateRequest) {
        ReservationResponse response = adminReservationService.create(adminReservationCreateRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> readLists(
        @ModelAttribute ReservationSearchConditionRequest reservationSearchConditionRequest
    ) {
        List<ReservationResponse> reservations = adminReservationService.findByCondition(
            reservationSearchConditionRequest
        );
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/waitings")
    public ResponseEntity<List<ReservationResponse>> readWaitingLists() {
        List<ReservationResponse> waitingReservations = adminReservationService.findHighestPriorityWaitings();
        return ResponseEntity.ok(waitingReservations);
    }

    @PatchMapping("/{id}/waitings/approve")
    public ResponseEntity<Void> approveWaiting(@PathVariable Long id) {
        adminReservationService.approveWaiting(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/waitings/deny")
    public ResponseEntity<Void> denyWaiting(@PathVariable Long id) {
        adminReservationService.denyWaiting(id);
        return ResponseEntity.noContent().build();
    }
}
