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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.member.dto.MemberProfileInfo;
import roomescape.reservation.dto.AdminReservationRequest;
import roomescape.reservation.dto.ReservationConditionSearchRequest;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.ReservationTimeAvailabilityResponse;
import roomescape.reservation.service.ReservationService;

@RestController
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> createReservation(@Valid ReservationRequest reservationRequest,
            MemberProfileInfo memberProfileInfo) {
        ReservationResponse reservationCreateResponse = reservationService.addReservation(reservationRequest,
                memberProfileInfo);
        URI uri = URI.create("/reservations/" + reservationCreateResponse.id());
        return ResponseEntity.created(uri)
                .body(reservationCreateResponse);
    }

    @PostMapping("/admin/reservations")
    public ResponseEntity<ReservationResponse> createReservation(
            @RequestBody AdminReservationRequest adminReservationRequest) {
        ReservationResponse reservationCreateResponse = reservationService.addReservation(adminReservationRequest);
        URI uri = URI.create("/reservations/" + reservationCreateResponse.id());
        return ResponseEntity.created(uri)
                .body(reservationCreateResponse);
    }

    @GetMapping("/reservations")
    public List<ReservationResponse> reservaionList() {
        return reservationService.findReservations();
    }

    @GetMapping("/admin/reservations/search")
    public List<ReservationResponse> reservationListInCondition(
            @RequestParam("themeId") long themeId,
            @RequestParam("memberId") long memberId,
            @RequestParam("dateFrom") LocalDate dateFrom,
            @RequestParam("dateTo") LocalDate dateTo
    ) {
        ReservationConditionSearchRequest request = new ReservationConditionSearchRequest(memberId, themeId, dateFrom,
                dateTo);
        return reservationService.findReservationsByConditions(request);
    }

    @GetMapping("/reservations/times/{themeId}")
    public ResponseEntity<List<ReservationTimeAvailabilityResponse>> reservationTimeList(@PathVariable long themeId,
            @RequestParam LocalDate date) {
        List<ReservationTimeAvailabilityResponse> timeAvailabilityReadResponse = reservationService.findTimeAvailability(
                themeId, date);
        return ResponseEntity.ok(timeAvailabilityReadResponse);
    }

    @DeleteMapping("/reservations/{reservationId}")
    public ResponseEntity<Void> deleteReservation(@PathVariable long reservationId) {
        reservationService.removeReservations(reservationId);
        return ResponseEntity.noContent()
                .build();
    }

}
