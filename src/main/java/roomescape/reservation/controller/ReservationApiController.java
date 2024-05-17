package roomescape.reservation.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.dto.LoginMember;
import roomescape.reservation.dto.MemberReservationResponse;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.ReservationSaveRequest;
import roomescape.reservation.dto.ReservationSearchCondRequest;
import roomescape.reservation.service.ReservationService;

@RestController
public class ReservationApiController {

    private final ReservationService reservationService;

    public ReservationApiController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationResponse>> findAll() {
        List<ReservationResponse> reservationResponses = reservationService.findAll();

        return ResponseEntity.ok(reservationResponses);
    }

    @GetMapping("/reservations/mine")
    public ResponseEntity<List<MemberReservationResponse>> findMemberReservations(LoginMember loginMember) {
        List<MemberReservationResponse> memberReservationResponses = reservationService.findMemberReservations(loginMember);

        return ResponseEntity.ok(memberReservationResponses);
    }

    @GetMapping("/reservations/search")
    public ResponseEntity<List<ReservationResponse>> findAllBySearchCond(
            @Valid @ModelAttribute ReservationSearchCondRequest reservationSearchCondRequest
    ) {
        List<ReservationResponse> reservationResponses = reservationService.findAllBySearchCond(reservationSearchCondRequest);

        return ResponseEntity.ok(reservationResponses);
    }

    @PostMapping(path = {"/reservations", "/admin/reservations"})
    public ResponseEntity<ReservationResponse> createMemberReservation(
            @Valid @RequestBody ReservationSaveRequest reservationSaveRequest,
            LoginMember loginMember
    ) {
        ReservationResponse reservationResponse = reservationService.save(reservationSaveRequest, loginMember);

        return ResponseEntity.created(URI.create("/reservations/" + reservationResponse.id())).body(reservationResponse);
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        reservationService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
