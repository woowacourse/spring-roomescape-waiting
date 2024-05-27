package roomescape.controller.admin.reservation;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import roomescape.domain.dto.ReservationResponse;
import roomescape.domain.dto.ReservationWaitingResponse;
import roomescape.domain.dto.ResponsesWrapper;
import roomescape.service.reservation.ReservationSearchService;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin/reservations")
public class AdminReservationSearchController {
    private final ReservationSearchService reservationSearchService;

    public AdminReservationSearchController(final ReservationSearchService reservationSearchService) {
        this.reservationSearchService = reservationSearchService;
    }

    @GetMapping
    public ResponseEntity<ResponsesWrapper<ReservationResponse>> getReservations() {
        return ResponseEntity.ok(reservationSearchService.findEntireReservations());
    }

    @GetMapping("waiting")
    public ResponseEntity<ResponsesWrapper<ReservationWaitingResponse>> getWaitingReservations() {
        return ResponseEntity.ok(reservationSearchService.findEntireWaitingReservations());
    }

    @GetMapping("/search")
    public ResponseEntity<ResponsesWrapper<ReservationResponse>> search(@RequestParam Long themeId, @RequestParam Long memberId, @RequestParam LocalDate dateFrom, @RequestParam LocalDate dateTo) {
        return ResponseEntity.ok(reservationSearchService.findReservations(themeId, memberId, dateFrom, dateTo));
    }
}
