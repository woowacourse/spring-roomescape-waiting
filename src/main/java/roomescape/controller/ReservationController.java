package roomescape.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import roomescape.controller.dto.request.ReservationCreateRequest;
import roomescape.controller.dto.request.ReservationUpdateRequest;
import roomescape.controller.dto.response.ReservationResponse;
import roomescape.controller.dto.response.ReservationResponses;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationWithRank;
import roomescape.domain.reservation.Reservations;
import roomescape.service.ReservationCreateCommand;
import roomescape.service.ReservationService;
import roomescape.service.ReservationUpdateCommand;

import java.net.URI;
import java.util.List;

@RestController
public class ReservationController {
    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> create(
            @Valid @RequestBody ReservationCreateRequest request
    ) {
        Reservation reservation = reservationService.reserve(ReservationCreateCommand.from(request));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(reservation.getId())
                .toUri();

        return ResponseEntity.created(location).body(ReservationResponse.toDto(reservation));
    }

    @GetMapping("/reservations")
    public ResponseEntity<ReservationResponses> findList(
            @RequestParam(required = false) Long memberId
    ) {
        Reservations reservations = reservationService.findAll(memberId);
        return ResponseEntity.ok(ReservationResponses.toDto(reservations));
    }

    @GetMapping("/reservations/{id}")
    public ResponseEntity<ReservationResponse> find(@PathVariable long id) {
        ReservationWithRank reservationWithRank = reservationService.find(id);
        return ResponseEntity.ok(ReservationResponse.toDto(reservationWithRank));
    }

    @GetMapping("/reservations-mine")
    public ResponseEntity<ReservationResponses> findMine(@RequestParam Long memberId) {
        List<ReservationWithRank> reservations = reservationService.findMine(memberId);
        return ResponseEntity.ok(ReservationResponses.toDtoWithRank(reservations));
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable long id,
            @RequestParam Long memberId
    ) {
        reservationService.cancel(id, memberId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/reservations/{id}")
    public ResponseEntity<ReservationResponse> update(
            @Valid @RequestBody ReservationUpdateRequest request,
            @PathVariable long id
    ) {
        ReservationWithRank updated = reservationService.update(ReservationUpdateCommand.from(request), id);
        return ResponseEntity.ok(ReservationResponse.toDto(updated));
    }
}
