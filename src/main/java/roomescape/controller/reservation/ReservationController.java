package roomescape.controller.reservation;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import roomescape.infrastructure.MemberId;
import roomescape.service.reservation.ReservationService;
import roomescape.service.reservation.dto.ReservationConditionRequest;
import roomescape.service.reservation.dto.ReservationRequest;
import roomescape.service.reservation.dto.UserReservationRequest;
import roomescape.service.reservation.dto.MyReservationResponse;
import roomescape.service.reservation.dto.ReservationResponse;


@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> postReservation(
            @RequestBody @Valid UserReservationRequest userReservationRequest,
            @MemberId Long memberId
    ) {
        ReservationRequest reservationRequest = userReservationRequest.toReservationRequest(memberId);
        ReservationResponse reservationResponse = reservationService.createReservation(reservationRequest);

        URI location = UriComponentsBuilder.newInstance()
                .path("/reservations/{id}")
                .buildAndExpand(reservationResponse.id())
                .toUri();

        return ResponseEntity.created(location)
                .body(reservationResponse);
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getReservations() {
        return ResponseEntity.ok(reservationService.findAllReservations());
    }

    @GetMapping(params = {"themeId", "memberId", "dateFrom", "dateTo"})
    public ResponseEntity<List<ReservationResponse>> getReservationsByCondition(
            @ModelAttribute ReservationConditionRequest request
    ) {
        return ResponseEntity.ok(reservationService.findAllReservationsByCondition(request));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<MyReservationResponse>> getMyReservation(@MemberId Long memberId) {
        return ResponseEntity.ok(reservationService.findAllByMemberId(memberId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        reservationService.deleteReservation(id);
        return ResponseEntity.noContent()
                .build();
    }
}
