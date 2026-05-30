package roomescape.reservation.presentation.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservation.application.dto.ReservationApplicationResult;
import roomescape.reservation.application.service.ReservationQueryService;
import roomescape.reservation.presentation.dto.ReservationApplicationResponse;

@RequiredArgsConstructor
@RequestMapping("/admin/reservations")
@RestController
public class AdminReservationController {

    private final ReservationQueryService reservationQueryService;

    @GetMapping
    public ResponseEntity<List<ReservationApplicationResponse>> findAll() {
        List<ReservationApplicationResult> results = reservationQueryService.findAll();

        List<ReservationApplicationResponse> responses = results.stream()
                .map(ReservationApplicationResponse::from)
                .toList();

        return ResponseEntity.ok(responses);
    }
}
