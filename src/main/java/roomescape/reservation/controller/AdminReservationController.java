package roomescape.reservation.controller;

import jakarta.validation.Valid;
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
import roomescape.reservation.controller.dto.request.AdminReservationSaveRequest;
import roomescape.reservation.controller.dto.response.ReservationResponse;
import roomescape.reservation.service.ReservationService;

@RestController
@RequestMapping("/admin/reservations")
public class AdminReservationController {

    private final ReservationService reservationService;

    public AdminReservationController(final ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> save(
            @RequestBody @Valid final AdminReservationSaveRequest reservationSaveRequest
    ) {
        ReservationResponse reservationResponse = reservationService.save(reservationSaveRequest);
        return ResponseEntity.created(URI.create("/reservations/" + reservationResponse.id()))
                .body(reservationResponse);
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> findByFilter(
            @RequestParam(required = false) final Long memberId,
            @RequestParam(required = false) final Long themeId,
            @RequestParam(required = false) final LocalDate dateFrom,
            @RequestParam(required = false) final LocalDate dateTo
    ) {
        List<ReservationResponse> reservationResponses =
                reservationService.findByFilter(memberId, themeId, dateFrom, dateTo);
        return ResponseEntity.ok(reservationResponses);
    }
}
