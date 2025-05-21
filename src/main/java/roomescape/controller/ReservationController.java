package roomescape.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.Member;
import roomescape.dto.reservation.MemberReservationResponse;
import roomescape.dto.reservation.ReservationRequest;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.dto.reservation.WaitingReservationRequest;
import roomescape.service.reservation.ReservationService;

@RestController
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("reservations")
    public ResponseEntity<List<ReservationResponse>> getReservations() {
        return ResponseEntity.ok().body(reservationService.getAll());
    }

    @PostMapping("reservations")
    public ResponseEntity<ReservationResponse> createReservation(@Valid @RequestBody ReservationRequest request,
                                                                 Member member) {
        ReservationResponse response = reservationService.create(request, member);
        return ResponseEntity.created(URI.create("/reservations/" + response.id())).body(response);
    }

    @DeleteMapping("reservations/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        reservationService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("reservations-mine")
    public ResponseEntity<List<MemberReservationResponse>> getMyReservations(Member member) {
        return ResponseEntity.ok().body(reservationService.getReservationByMember(member));
    }

    @PostMapping("reservations/waiting")
    public ResponseEntity<ReservationResponse> createWaitingReservation(
            @Valid @RequestBody WaitingReservationRequest request,
            Member member) {
        ReservationResponse response = reservationService.createWaitingReservation(request, member);
        return ResponseEntity.created(URI.create("/reservations/" + response.id())).body(response);
    }

    @DeleteMapping("reservations/waiting/{id}")
    public ResponseEntity<Void> deleteWaitingReservation(@PathVariable Long id, Member member) {
        reservationService.deleteWaitingReservation(id, member);
        return ResponseEntity.noContent().build();
    }
}
