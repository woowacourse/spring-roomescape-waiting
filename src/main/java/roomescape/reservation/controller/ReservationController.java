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
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.ReservationTimeAvailabilityResponse;
import roomescape.reservation.service.ReservationDetailService;
import roomescape.reservation.service.ReservationService;

@RestController
@RequestMapping("/reservations")
public class ReservationController {
    private final ReservationService reservationService;
    private final ReservationDetailService reservationDetailService;

    public ReservationController(ReservationService reservationService, ReservationDetailService reservationDetailService) {
        this.reservationService = reservationService;
        this.reservationDetailService = reservationDetailService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> findReservations() {
        List<ReservationResponse> response = reservationService.findReservations();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/mine")
    public ResponseEntity<List<MyReservationResponse>> findReservationsByMember(MemberProfileInfo memberProfileInfo) {
        List<MyReservationResponse> response = reservationService.findReservationByMemberId(memberProfileInfo.id());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/times/{themeId}")
    public ResponseEntity<List<ReservationTimeAvailabilityResponse>> findReservationTimes(
            @PathVariable long themeId,
            @RequestParam LocalDate date) {
        List<ReservationTimeAvailabilityResponse> timeAvailabilityReadResponse
                = reservationService.findTimeAvailability(themeId, date);
        return ResponseEntity.ok(timeAvailabilityReadResponse);
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(ReservationCreateRequest request) {
        Long detailId = reservationDetailService.findReservationDetailId(request);
        ReservationRequest reservationRequest = new ReservationRequest(request.memberId(), detailId);
        ReservationResponse reservationCreateResponse = reservationService.addReservation(reservationRequest);

        URI uri = URI.create("/reservations/" + reservationCreateResponse.id());
        return ResponseEntity.created(uri)
                .body(reservationCreateResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable long id) {
        reservationService.removeReservations(id);
        return ResponseEntity.noContent()
                .build();
    }
}
