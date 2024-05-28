package roomescape.reservation.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.reservation.dto.MemberReservationResponse;
import roomescape.reservation.dto.ReservationCreateRequest;
import roomescape.reservation.dto.ReservationSearchRequestParameter;
import roomescape.reservation.facade.ReservationFacadeService;

import java.util.List;

@RestController
@RequestMapping("/admin/reservations")
public class AdminReservationController {

    private final ReservationFacadeService reservationFacadeService;

    public AdminReservationController(ReservationFacadeService reservationFacadeService) {
        this.reservationFacadeService = reservationFacadeService;
    }

    @PostMapping
    public MemberReservationResponse createReservation(@Valid @RequestBody ReservationCreateRequest request) {
        return reservationFacadeService.createReservation(request);
    }

    @GetMapping
    public List<MemberReservationResponse> readReservations() {
        return reservationFacadeService.readReservations();
    }

    @GetMapping("/search")
    public List<MemberReservationResponse> searchReservations(ReservationSearchRequestParameter searchCondition) {
        return reservationFacadeService.searchReservations(searchCondition);
    }

    @GetMapping("/waiting")
    public List<MemberReservationResponse> readWaitingReservations() {
        return reservationFacadeService.readWaitingReservations();
    }

    @PutMapping("/waiting/{id}")
    public void confirmWaitingReservation(@PathVariable Long id) {
        reservationFacadeService.confirmWaitingReservation(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        reservationFacadeService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }
}
