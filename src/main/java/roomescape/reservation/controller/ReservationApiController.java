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
import roomescape.auth.Login;
import roomescape.member.dto.LoginMemberInToken;
import roomescape.reservation.dto.ReservationCreateRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.ReservationSearchRequest;
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

    @GetMapping("/reservations/search")
    public ResponseEntity<List<ReservationResponse>> findAllBySearchCond(
            @Valid @ModelAttribute ReservationSearchRequest reservationSearchRequest
    ) {
        List<ReservationResponse> reservationResponses = reservationService.findAllBySearch(reservationSearchRequest);

        return ResponseEntity.ok(reservationResponses);
    }

    @PostMapping(path = {"/reservations", "/admin/reservations"})
    public ResponseEntity<ReservationResponse> createMemberReservation(
            @Valid @RequestBody ReservationCreateRequest reservationCreateRequest,
            @Login LoginMemberInToken loginMemberInToken
    ) {
        Long id = reservationService.save(reservationCreateRequest, loginMemberInToken);
        ReservationResponse reservationResponse = reservationService.findById(id);

        return ResponseEntity.created(URI.create("/reservations/" + id)).body(reservationResponse);
    }


    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        reservationService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
