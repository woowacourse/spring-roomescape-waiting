package roomescape.reservation.controller;

import jakarta.validation.Valid;
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
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.ReservationTimeAvailabilityResponse;
import roomescape.reservation.service.ReservationService;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(@Valid ReservationRequest reservationRequest) {
        ReservationResponse reservationCreateResponse = reservationService.addReservation(reservationRequest);
        URI uri = URI.create("/reservations/" + reservationCreateResponse.id());
        return ResponseEntity.created(uri)
                .body(reservationCreateResponse);
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> reservaionList() {
        return ResponseEntity.ok(reservationService.findReservations());
    }

    @GetMapping("/mine")
    public ResponseEntity<List<MyReservationResponse>> myReservationList(MemberProfileInfo memberProfileInfo) {
        List<MyReservationResponse> reservationsByMemberId = reservationService.findReservationByMemberId(
                memberProfileInfo.id());
        return ResponseEntity.ok(reservationsByMemberId);
    }

    @GetMapping("/times/{themeId}")
    public ResponseEntity<List<ReservationTimeAvailabilityResponse>> reservationTimeList(@PathVariable long themeId,
            @RequestParam LocalDate date) {
        List<ReservationTimeAvailabilityResponse> timeAvailabilityReadResponse = reservationService.findTimeAvailability(
                themeId, date);
        return ResponseEntity.ok(timeAvailabilityReadResponse);
    }

    @DeleteMapping("/{reservationId}")
    public ResponseEntity<Void> deleteReservation(@PathVariable long reservationId) {
        reservationService.removeReservations(reservationId);
        return ResponseEntity.noContent()
                .build();
    }

}
