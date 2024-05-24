package roomescape.controller.reservation;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import roomescape.controller.member.dto.LoginMember;
import roomescape.controller.reservation.dto.CreateReservationDto;
import roomescape.controller.reservation.dto.ReservationResponse;
import roomescape.controller.reservation.dto.UserCreateReservationRequest;
import roomescape.domain.Status;
import roomescape.service.ReservationService;

@RestController
@RequestMapping("/reservations/waiting")
public class WaitingReservationController {

    private final ReservationService reservationService;

    public WaitingReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public List<ReservationResponse> getWaitingReservations() {
        return reservationService.getWaitingReservations();
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> addWaitingReservation(
            @RequestBody @Valid final UserCreateReservationRequest userRequest,
            @Valid final LoginMember loginMember) {
        final CreateReservationDto reservationDto = new CreateReservationDto(
                loginMember.id(), userRequest.themeId(), userRequest.date(),
                userRequest.timeId(), Status.WAITING);
        final ReservationResponse reservation = reservationService.addReservation(reservationDto);

        final URI uri = UriComponentsBuilder.fromPath("/reservations/waiting/{id}")
                .buildAndExpand(reservation.id())
                .toUri();
        return ResponseEntity.created(uri).body(reservation);
    }

    @PatchMapping("/{id}")
    public ReservationResponse changeWaitingReservationToReserved(
            @PathVariable("id") final Long id) {
        return reservationService.reserveWaitingReservation(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable("id") final Long id) {
        reservationService.deleteReservation(id);
        return ResponseEntity.noContent()
                .build();
    }
}
