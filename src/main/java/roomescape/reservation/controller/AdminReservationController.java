package roomescape.reservation.controller;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import roomescape.reservation.dto.ReservationConditionSearchRequest;
import roomescape.reservation.dto.ReservationCreateRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.service.ReservationFacadeService;

@RestController
@RequestMapping("/admin/reservations")
public class AdminReservationController {
    private final ReservationFacadeService reservationFacadeService;

    public AdminReservationController(ReservationFacadeService reservationFacadeService) {
        this.reservationFacadeService = reservationFacadeService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<ReservationResponse>> findReservationsInCondition(
            @RequestParam("themeId") long themeId,
            @RequestParam("memberId") long memberId,
            @RequestParam("dateFrom") LocalDate dateFrom,
            @RequestParam("dateTo") LocalDate dateTo
    ) {
        ReservationConditionSearchRequest request
                = new ReservationConditionSearchRequest(memberId, themeId, dateFrom, dateTo);
        List<ReservationResponse> reservationResponse = reservationFacadeService.findReservationsInCondition(request);

        return ResponseEntity.ok(reservationResponse);
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(@RequestBody ReservationCreateRequest request) {
        ReservationResponse reservationCreateResponse = reservationFacadeService.createReservation(request);

        URI uri = URI.create("/admin/reservations/" + reservationCreateResponse.id());
        return ResponseEntity.created(uri)
                .body(reservationCreateResponse);
    }
}
