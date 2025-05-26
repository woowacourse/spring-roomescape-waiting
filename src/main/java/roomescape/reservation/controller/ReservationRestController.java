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
import roomescape.auth.dto.LoginMember;
import roomescape.reservation.dto.AvailableReservationTimeRequest;
import roomescape.reservation.dto.AvailableReservationTimeResponse;
import roomescape.reservation.dto.CreateReservationRequest;
import roomescape.reservation.dto.CreateReservationResponse;
import roomescape.reservation.dto.CreateWaitingRequest;
import roomescape.reservation.dto.CreateWaitingResponse;
import roomescape.reservation.dto.ReservationMineResponse;
import roomescape.reservation.service.facade.ReservationServiceFacade;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reservations")
public class ReservationRestController {

    private final ReservationServiceFacade reservationServiceFacade;

    @PostMapping
    public ResponseEntity<CreateReservationResponse> createReservation(
            @RequestBody final CreateReservationRequest createReservationRequest,
            final LoginMember loginMember
    ) {
        final CreateReservationResponse createReservationResponse = reservationServiceFacade.saveReservation(
                createReservationRequest,
                loginMember
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(createReservationResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable final Long id) {
        reservationServiceFacade.deleteById(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<CreateReservationResponse>> getReservations() {
        final List<CreateReservationResponse> createReservationResponse = reservationServiceFacade.findAll();

        return ResponseEntity.ok(createReservationResponse);
    }

    @GetMapping("/available-times")
    public ResponseEntity<List<AvailableReservationTimeResponse>> getAvailableReservationTimes(
            @ModelAttribute final AvailableReservationTimeRequest request) {

        final List<AvailableReservationTimeResponse> availableTimes = reservationServiceFacade.findAvailableTimes(
                request);

        return ResponseEntity.ok(availableTimes);
    }

    @GetMapping("/mine")
    public ResponseEntity<List<ReservationMineResponse>> getMyReservations(final LoginMember member) {

        final List<ReservationMineResponse> reservations = reservationServiceFacade.findMyReservations(member);

        return ResponseEntity.ok(reservations);
    }

    @PostMapping("/waitings")
    public ResponseEntity<CreateWaitingResponse> createWaitingReservation(
            @RequestBody final CreateWaitingRequest createWaitingRequest,
            final LoginMember loginMember
    ) {

        final CreateWaitingResponse waitingResponse = reservationServiceFacade.saveWaiting(createWaitingRequest,
                loginMember);

        return ResponseEntity.status(HttpStatus.CREATED).body(waitingResponse);
    }

    @DeleteMapping("/waitings/{id}")
    public ResponseEntity<Void> deleteWaitingReservation(@PathVariable final Long id) {

        reservationServiceFacade.deleteWaiting(id);

        return ResponseEntity.noContent().build();
    }
}
