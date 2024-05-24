package roomescape.controller.reservation;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import roomescape.controller.member.dto.LoginMember;
import roomescape.controller.reservation.dto.CreateReservationRequest;
import roomescape.controller.reservation.dto.MyReservationResponse;
import roomescape.controller.reservation.dto.ReservationResponse;
import roomescape.controller.reservation.dto.ReservationSearchCondition;
import roomescape.service.ReservationService;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(final ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public List<ReservationResponse> getReservations() {
        return reservationService.getReservedReservations();
    }

    @GetMapping("/mine")
    public List<MyReservationResponse> getLoginMemberReservation(final LoginMember member) {
        return reservationService.getReservationsByMember(member);
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> addReservation(
            @RequestBody @Valid final CreateReservationRequest reservationRequest,
            @Valid final LoginMember loginMember) {
        final ReservationResponse reservation
                = reservationService.addReservation(reservationRequest, loginMember.id());

        final URI uri = UriComponentsBuilder.fromPath("/reservations/{id}")
                .buildAndExpand(reservation.id())
                .toUri();
        return ResponseEntity.created(uri).body(reservation);
    }

    @GetMapping(value = "/search", params = {"themeId", "memberId", "dateFrom", "dateTo"})
    public List<ReservationResponse> searchReservations(
            final ReservationSearchCondition request) {
        return reservationService.searchReservations(request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable("id") final Long id) {
        reservationService.deleteReservation(id);
        return ResponseEntity.noContent()
                .build();
    }
}
