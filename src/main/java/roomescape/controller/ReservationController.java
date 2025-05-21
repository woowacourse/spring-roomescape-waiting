package roomescape.controller;

import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.config.annotation.RequiredAccessToken;
import roomescape.dto.business.AccessTokenContent;
import roomescape.dto.business.ReservationCreationContent;
import roomescape.dto.request.ReservationCreationRequest;
import roomescape.dto.response.ReservationResponse;
import roomescape.service.ReservationService;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public List<ReservationResponse> findAllReservations() {
        return reservationService.findAllReservations();
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> addReservation(
            @RequestBody ReservationCreationRequest request,
            @RequiredAccessToken AccessTokenContent accessTokenContent
    ) {
        ReservationCreationContent creationContent = new ReservationCreationContent(request);
        ReservationResponse reservationResponse = reservationService.addReservation(accessTokenContent.id(),
                creationContent);
        return ResponseEntity.status(HttpStatus.CREATED)
                .location(URI.create("/reservation/" + reservationResponse.id()))
                .body(reservationResponse);
    }

    @DeleteMapping("/{reservationId}")
    public ResponseEntity<Void> deleteReservationById(
            @PathVariable("reservationId") Long id
    ) {
        reservationService.deleteReservationById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/mine")
    public List<ReservationResponse> findAllReservationsByMember(
            @RequiredAccessToken AccessTokenContent accessTokenContent
    ) {
        return reservationService.findAllReservationsByMember(accessTokenContent.id());
    }
}
