package roomescape.controller;

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
import roomescape.service.ReservationService;
import roomescape.service.dto.request.ReservationConditionRequest;
import roomescape.service.dto.request.ReservationRequest;
import roomescape.service.dto.request.UserReservationRequest;
import roomescape.service.dto.response.MyReservationEntryResponse;
import roomescape.service.dto.response.ReservationResponse;


@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getReservations() {
        return ResponseEntity.ok(reservationService.findAllReservations());
    }

    @GetMapping(params = {"themeId", "memberId", "dateFrom", "dateTo"})
    public ResponseEntity<List<ReservationResponse>> getReservations(
            @ModelAttribute ReservationConditionRequest request
    ) {
        return ResponseEntity.ok(reservationService.findAllReservationsByCondition(request));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<MyReservationEntryResponse>> getMyReservations(@MemberId Long id) {
        return ResponseEntity.ok(reservationService.findAllByMemberId(id));
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> postReservation(
            @RequestBody @Valid UserReservationRequest userReservationRequest,
            @MemberId Long id
    ) {
        ReservationRequest reservationRequest = userReservationRequest.toReservationRequest(id);
        ReservationResponse reservationResponse = reservationService.createReservation(reservationRequest, id);
        URI location = UriComponentsBuilder.newInstance()
                .path("/reservations/{id}")
                .buildAndExpand(reservationResponse.id())
                .toUri();

        return ResponseEntity.created(location)
                .body(reservationResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservations(@PathVariable Long id) {
        reservationService.deleteReservation(id);
        return ResponseEntity.noContent()
                .build();
    }
}
