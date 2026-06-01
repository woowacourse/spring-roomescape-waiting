package roomescape.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import roomescape.domain.common.UserName;
import roomescape.service.ReservationCommandService;
import roomescape.service.ReservationQueryService;
import roomescape.service.WaitingQueryService;
import roomescape.web.dto.request.ReservationRequest;
import roomescape.web.dto.request.ReservationUpdateRequest;
import roomescape.web.dto.response.ReservationResponse;

import java.net.URI;
import java.util.List;
import java.util.stream.Stream;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationCommandService reservationCommandService;
    private final ReservationQueryService reservationQueryService;
    private final WaitingQueryService waitingQueryService;

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getMyReservations(
            @RequestParam String name
    ) {
        List<ReservationResponse> responses = Stream.concat(
                reservationQueryService.getByName(name).stream().map(ReservationResponse::from),
                waitingQueryService.getByName(name).stream().map(ReservationResponse::from)
        ).toList();

        return ResponseEntity.ok(responses);
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(
            @Valid @RequestBody ReservationRequest request
    ) {
        ReservationResponse reservationResponse = ReservationResponse.from(
                reservationCommandService.create(ReservationRequest.toCommand(request)));

        Long savedId = reservationResponse.id();

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedId)
                .toUri();

        return ResponseEntity.created(location).body(reservationResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelReservation(
            @PathVariable Long id,
            @RequestParam String name
    ) {
        reservationCommandService.cancelByUser(id, UserName.from(name));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ReservationResponse> updateReservation(
            @PathVariable Long id,
            @RequestParam String name,
            @Valid @RequestBody ReservationUpdateRequest request
    ) {
        ReservationResponse response = ReservationResponse.from(
                reservationCommandService.updateByUser(id, UserName.from(name), ReservationUpdateRequest.toCommand(request)));
        return ResponseEntity.ok(response);
    }
}
