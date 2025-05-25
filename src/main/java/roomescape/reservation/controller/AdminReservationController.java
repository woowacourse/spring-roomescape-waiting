package roomescape.reservation.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.reservation.dto.request.AdminReservationCreateRequest;
import roomescape.reservation.dto.request.ReservationSearchConditionRequest;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.service.AdminReservationFacade;

import java.util.List;

@RestController
@RequestMapping("/admin/reservations")
public class AdminReservationController {
    private final AdminReservationFacade adminReservationService;

    public AdminReservationController(AdminReservationFacade adminReservationService) {
        this.adminReservationService = adminReservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> create(
            @RequestBody @Valid AdminReservationCreateRequest adminReservationCreateRequest
    ) {
        ReservationResponse response = adminReservationService.create(adminReservationCreateRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> searchReservations(
            @ModelAttribute ReservationSearchConditionRequest reservationSearchConditionRequest
    ) {
        List<ReservationResponse> reservations = adminReservationService.findByCondition(reservationSearchConditionRequest);
        return ResponseEntity.ok(reservations);
    }
}
