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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservation.controller.dto.request.AdminReservationSaveRequest;
import roomescape.reservation.controller.dto.response.ReservationDeleteResponse;
import roomescape.reservation.controller.dto.response.ReservationResponse;
import roomescape.reservation.controller.dto.response.ReservationWaitingResponse;
import roomescape.reservation.service.AdminReservationService;
import roomescape.reservation.service.ReservationService;

@RestController
@RequestMapping("/admin/reservations")
public class AdminReservationController {

    private final AdminReservationService adminReservationService;
    private final ReservationService reservationService;

    public AdminReservationController(
            final AdminReservationService adminReservationService,
            final ReservationService reservationService
    ) {
        this.adminReservationService = adminReservationService;
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> save(
            @RequestBody @Valid final AdminReservationSaveRequest reservationSaveRequest
    ) {
        ReservationResponse reservationResponse = adminReservationService.save(reservationSaveRequest);
        return ResponseEntity.created(URI.create("/reservations/" + reservationResponse.id()))
                .body(reservationResponse);
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getByFilter(
            @RequestParam(value = "memberId", required = false) final Long memberId,
            @RequestParam(value = "themeId", required = false) final Long themeId,
            @RequestParam(value = "dateFrom", required = false) final LocalDate dateFrom,
            @RequestParam(value = "dateTo", required = false) final LocalDate dateTo
    ) {
        return ResponseEntity.ok(
                adminReservationService.getByFilter(memberId, themeId, dateFrom, dateTo)
        );
    }

    @GetMapping("/waiting")
    public ResponseEntity<List<ReservationWaitingResponse>> getAllWaitings() {
        return ResponseEntity.ok(adminReservationService.getAllWaitings());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ReservationDeleteResponse> delete(@PathVariable("id") final long id) {
        return ResponseEntity.ok().body(reservationService.delete(id));
    }
}
