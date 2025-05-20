package roomescape.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.ArrayList;
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
import roomescape.service.reservation.ReservationService;
import roomescape.service.waiting.WaitingService;

@RestController
public class ReservationController {

    private final ReservationService reservationService;
    private final WaitingService waitingService;

    public ReservationController(ReservationService reservationService, WaitingService waitingService) {
        this.reservationService = reservationService;
        this.waitingService = waitingService;
    }

    @GetMapping("reservations")
    public ResponseEntity<List<ReservationResponse>> getReservations() {
        return ResponseEntity.ok().body(reservationService.getAll());
    }

    @PostMapping("reservations")
    public ResponseEntity<ReservationResponse> createReservation(@Valid @RequestBody ReservationRequest request,
                                                                 Member member) {
        ReservationResponse response = reservationService.create(request, member);
        return ResponseEntity.created(URI.create("/reservations" + response.id())).body(response);
    }

    @DeleteMapping("reservations/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        reservationService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("reservations-mine")
    public ResponseEntity<List<MemberReservationResponse>> getMyReservations(Member member) {
        List<MemberReservationResponse> memberReservationResponses = reservationService.getReservationByMember(member);
        List<MemberReservationResponse> memberWaitingResponses = waitingService.getWaitingByMember(member);

        List<MemberReservationResponse> combined = new ArrayList<>();
        combined.addAll(memberReservationResponses);
        combined.addAll(memberWaitingResponses);

        return ResponseEntity.ok().body(combined);
    }
}
