package roomescape.controller.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.dto.ReservationRequest;
import roomescape.domain.dto.ReservationResponse;
import roomescape.domain.dto.ReservationWaitingResponse;
import roomescape.domain.dto.ResponsesWrapper;
import roomescape.service.reservation.ReservationDeleteService;
import roomescape.service.reservation.ReservationRegisterService;
import roomescape.service.reservation.ReservationSearchService;

import java.net.URI;
import java.time.LocalDate;

@RestController
@RequestMapping("/admin/reservations")
public class AdminReservationController {
    private final ReservationDeleteService reservationDeleteService;
    private final ReservationRegisterService reservationRegisterService;
    private final ReservationSearchService reservationSearchService;

    public AdminReservationController(final ReservationDeleteService reservationDeleteService,
                                      final ReservationRegisterService reservationRegisterService,
                                      final ReservationSearchService reservationSearchService) {
        this.reservationDeleteService = reservationDeleteService;
        this.reservationRegisterService = reservationRegisterService;
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

    @PostMapping
    public ResponseEntity<ReservationResponse> register(@RequestBody ReservationRequest reservationRequest) {
        ReservationResponse reservationResponse = reservationRegisterService.register(reservationRequest);
        return ResponseEntity.created(URI.create("/reservations/" + reservationResponse.id())).body(reservationResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reservationDeleteService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<ResponsesWrapper<ReservationResponse>> search(@RequestParam Long themeId, @RequestParam Long memberId, @RequestParam LocalDate dateFrom, @RequestParam LocalDate dateTo) {
        return ResponseEntity.ok(reservationSearchService.findReservations(themeId, memberId, dateFrom, dateTo));
    }

    @DeleteMapping("/waiting/{id}")
    public ResponseEntity<Void> deleteWaiting(@PathVariable Long id) {
        reservationDeleteService.deleteWaitingById(id);
        return ResponseEntity.noContent().build();
    }
}
