package roomescape.controller.api;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.CreateReservationRequest;
import roomescape.controller.dto.CreateReservationResponse;
import roomescape.controller.dto.FindReservationResponse;
import roomescape.controller.dto.FindReservationStandbyResponse;
import roomescape.controller.dto.SearchReservationFilterRequest;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.global.argumentresolver.AuthenticationPrincipal;
import roomescape.service.ReservationService;

@RestController
@RequestMapping("/admin/reservations")
public class AdminReservationController {

    private final ReservationService reservationService;

    public AdminReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<CreateReservationResponse> save(@Valid @RequestBody CreateReservationRequest request) {
        Reservation reservation = reservationService.reserve(
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
        reservationService.deleteById(id);
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

    @GetMapping("/standby")
    public ResponseEntity<List<FindReservationStandbyResponse>> findAllStandby() {
        List<Reservation> reservations = reservationService.findAllStandby();
        List<FindReservationStandbyResponse> response = reservations.stream()
            .map(FindReservationStandbyResponse::from)
            .toList();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/standby/{id}")
    public ResponseEntity<Void> deleteStandby(@PathVariable Long id, @AuthenticationPrincipal Member member) {
        reservationService.deleteStandby(id, member);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<FindReservationResponse>> find(SearchReservationFilterRequest request) {
        List<Reservation> reservations = reservationService.findAllBy(
            request.themeId(), request.memberId(), request.dateFrom(), request.dateTo());
        List<FindReservationResponse> response = reservations.stream()
            .map(FindReservationResponse::from)
            .toList();

        return ResponseEntity.ok(response);
    }
}
