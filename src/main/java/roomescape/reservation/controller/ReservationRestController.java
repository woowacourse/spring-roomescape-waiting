package roomescape.reservation.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.AvailableReservationTimeRequest;
import roomescape.reservation.dto.AvailableReservationTimeResponse;
import roomescape.reservation.dto.CreateReservationRequest;
import roomescape.reservation.dto.CreateReservationResponse;
import roomescape.reservation.service.ReservationService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reservations")
public class ReservationRestController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<CreateReservationResponse> createReservation(
            @RequestBody final CreateReservationRequest createReservationRequest,
            final Member member
    ) {
        final Long id = reservationService.save(
                member,
                createReservationRequest.date(),
                createReservationRequest.timeId(),
                createReservationRequest.themeId()
        );
        final Reservation found = reservationService.getById(id);
        return ResponseEntity.status(HttpStatus.CREATED).body(CreateReservationResponse.from(found));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable final Long id) {
        reservationService.deleteById(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<CreateReservationResponse>> getReservations() {
        final List<Reservation> reservations = reservationService.findAll();
        final List<CreateReservationResponse> createReservationResponse = reservations.stream()
                .map(CreateReservationResponse::from)
                .toList();

        return ResponseEntity.ok(createReservationResponse);
    }

    @GetMapping("/available-times")
    public ResponseEntity<List<AvailableReservationTimeResponse>> getAvailableReservationTimes(
            @ModelAttribute final AvailableReservationTimeRequest request) {

        return ResponseEntity.ok(
                reservationService.findAvailableReservationTimes(request.date(), request.themeId()).stream()
                        .map(availableReservationTime -> new AvailableReservationTimeResponse(
                                availableReservationTime.id(),
                                availableReservationTime.startAt(),
                                availableReservationTime.alreadyBooked()
                        ))
                        .toList()
        );
    }
}
