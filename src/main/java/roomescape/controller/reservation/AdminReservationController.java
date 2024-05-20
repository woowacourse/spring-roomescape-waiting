package roomescape.controller.reservation;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import roomescape.controller.reservation.dto.CreateReservationRequest;
import roomescape.controller.reservation.dto.ReservationResponse;
import roomescape.controller.reservation.dto.WaitingReservationResponse;
import roomescape.domain.Reservation;
import roomescape.service.ReservationService;

import java.net.URI;
import java.util.List;

@RestController
public class AdminReservationController {

    private final ReservationService reservationService;

    public AdminReservationController(final ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/admin/reservations")
    public ResponseEntity<ReservationResponse> addReservation(
            @RequestBody @Valid final CreateReservationRequest request) {

        final Reservation reservation = reservationService.addReservation(request);
        final URI uri = UriComponentsBuilder.fromPath("/reservations/{id}")
                .buildAndExpand(reservation.getId())
                .toUri();

        return ResponseEntity.created(uri)
                .body(ReservationResponse.from(reservation));
    }

    //TODO 만들 기능 중에 예약 대기는 시간이 지나면 자동으로 취소
    @GetMapping("/admin/waiting-all")
    public List<WaitingReservationResponse> getWaitingReservations() {
        final List<Reservation> reservations = reservationService.findAllWaiting();
        return reservations.stream()
                .map(WaitingReservationResponse::from)
                .toList();
    }
}
