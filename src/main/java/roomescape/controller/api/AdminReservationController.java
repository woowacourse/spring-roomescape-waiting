package roomescape.controller.api;

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
import roomescape.controller.dto.CreateReservationRequest;
import roomescape.controller.dto.CreateReservationResponse;
import roomescape.controller.dto.FindReservationResponse;
import roomescape.domain.reservation.Reservation;
import roomescape.service.ReservationService;

@RestController
@RequestMapping("/admin/reservations")
public class AdminReservationController {

    private final ReservationService reservationService;

    public AdminReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<CreateReservationResponse> save(@RequestBody CreateReservationRequest request) {
        Reservation reservation = reservationService.save(
            request.memberId(),
            request.date(),
            request.timeId(),
            request.themeId()
        );

        return ResponseEntity.created(URI.create("/reservations/" + reservation.getId()))
            .body(CreateReservationResponse.from(reservation));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reservationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<FindReservationResponse>> findAll() {
        List<Reservation> reservations = reservationService.findAll();
        List<FindReservationResponse> createReservationResponse = reservations.stream()
            .map(FindReservationResponse::from)
            .toList();

        return ResponseEntity.ok(createReservationResponse);
    }

    @GetMapping("/search")
    public ResponseEntity<List<FindReservationResponse>> find(
        @RequestParam Long themeId,
        @RequestParam Long memberId,
        @RequestParam LocalDate dateFrom,
        @RequestParam LocalDate dateTo) {

        List<Reservation> reservations = reservationService.findAllBy(themeId, memberId, dateFrom, dateTo);
        List<FindReservationResponse> response = reservations.stream()
            .map(FindReservationResponse::from)
            .toList();

        return ResponseEntity.ok(response);
    }
}
