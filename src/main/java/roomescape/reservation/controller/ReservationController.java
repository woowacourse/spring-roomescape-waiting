package roomescape.reservation.controller;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import roomescape.auth.dto.LoginMember;
import roomescape.reservation.dto.response.BookedReservationTimeResponse;
import roomescape.reservation.dto.request.FilteringReservationRequest;
import roomescape.reservation.dto.response.MyReservationsResponse;
import roomescape.reservation.dto.request.ReservationCreateRequest;
import roomescape.reservation.dto.request.ReservationRequest;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.service.ReservationService;

@RequestMapping("/reservations")
@RestController
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(final ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> readAllReservations() {
        List<ReservationResponse> response = reservationService.getAll();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/times")
    public ResponseEntity<List<BookedReservationTimeResponse>> readAvailableReservationTimes(
            @RequestParam("date") final LocalDate date,
            @RequestParam("themeId") final Long themeId
    ) {
        List<BookedReservationTimeResponse> responses = reservationService.getAvailableTimes(date, themeId);

        return ResponseEntity.ok(responses);
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> create(
            @Valid @RequestBody final ReservationRequest request,
            final LoginMember loginMember
    ) {
        ReservationCreateRequest createRequest = ReservationCreateRequest.from(request, loginMember);
        ReservationResponse response = reservationService.create(createRequest);

        return ResponseEntity.created(URI.create("/reservations/" + response.id()))
                .body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") final Long id) {
        reservationService.delete(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/filtering")
    public ResponseEntity<List<ReservationResponse>> findAllByFilter(
            @ModelAttribute @Valid final FilteringReservationRequest request
    ) {
        final List<ReservationResponse> reservationResponses =
                reservationService.findReservationByFiltering(request);

        return ResponseEntity.ok(reservationResponses);
    }

    @GetMapping("/my")
    public ResponseEntity<List<MyReservationsResponse>> getMyReservations(@Valid LoginMember loginMember) {
        List<MyReservationsResponse> response = reservationService.getAllMemberReservations(loginMember);
        return ResponseEntity.ok(response);
    }
}
