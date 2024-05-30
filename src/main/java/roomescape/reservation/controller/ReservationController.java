package roomescape.reservation.controller;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import roomescape.member.dto.MemberProfileInfo;
import roomescape.reservation.dto.MyReservationResponse;
import roomescape.reservation.dto.ReservationCreateRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.ReservationTimeAvailabilityResponse;
import roomescape.reservation.service.ReservationFacadeService;

@RestController
@RequestMapping("/reservations")
public class ReservationController {
    private final ReservationFacadeService reservationFacadeService;

    public ReservationController(ReservationFacadeService reservationFacadeService) {
        this.reservationFacadeService = reservationFacadeService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> findReservations() {
        List<ReservationResponse> reservationResponse = reservationFacadeService.findReservations();

        return ResponseEntity.ok(reservationResponse);
    }

    @GetMapping("/mine")
    public ResponseEntity<List<MyReservationResponse>> findReservationsByMember(MemberProfileInfo memberProfileInfo) {
        List<MyReservationResponse> response = reservationFacadeService.findReservationsByMember(memberProfileInfo);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/times/{themeId}")
    public ResponseEntity<List<ReservationTimeAvailabilityResponse>> findReservationTimes(
            @PathVariable long themeId,
            @RequestParam LocalDate date) {
        List<ReservationTimeAvailabilityResponse> timeAvailabilityReadResponse
                = reservationFacadeService.findReservationTimes(themeId, date);

        return ResponseEntity.ok(timeAvailabilityReadResponse);
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(ReservationCreateRequest request) {
        ReservationResponse reservationCreateResponse = reservationFacadeService.createReservation(request);

        URI uri = URI.create("/reservations/" + reservationCreateResponse.id());
        return ResponseEntity.created(uri)
                .body(reservationCreateResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable long id) {
        reservationFacadeService.deleteReservation(id);

        return ResponseEntity.noContent()
                .build();
    }
}
