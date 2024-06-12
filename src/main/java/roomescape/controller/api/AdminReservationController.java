package roomescape.controller.api;

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
import roomescape.controller.dto.CreateReservationRequest;
import roomescape.controller.dto.CreateReservationResponse;
import roomescape.controller.dto.FindReservationResponse;
import roomescape.controller.dto.FindWaitingReservationResponse;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.service.ReservationFindService;
import roomescape.service.ReservationService;
import roomescape.service.WaitingService;
import roomescape.system.argumentresolver.AuthenticationPrincipal;

@RestController
@RequestMapping("/admin/reservations")
public class AdminReservationController {

    private final ReservationService reservationService;
    private final ReservationFindService reservationFindService;
    private final WaitingService waitingService;

    public AdminReservationController(
        ReservationService reservationService,
        ReservationFindService reservationFindService,
        WaitingService waitingService
    ) {
        this.reservationService = reservationService;
        this.reservationFindService = reservationFindService;
        this.waitingService = waitingService;
    }

    @PostMapping
    public ResponseEntity<CreateReservationResponse> save(@Valid @RequestBody CreateReservationRequest request) {
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
        List<Reservation> reservations = reservationFindService.findAll();
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

        List<Reservation> reservations = reservationFindService.findAllBy(themeId, memberId, dateFrom, dateTo);
        List<FindReservationResponse> response = reservations.stream()
            .map(FindReservationResponse::from)
            .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/waiting")
    public ResponseEntity<List<FindWaitingReservationResponse>> findAllWaitingReservations() {
        List<Reservation> reservations = waitingService.findAllWaitingReservations();
        List<FindWaitingReservationResponse> responses = reservations.stream()
            .map(FindWaitingReservationResponse::from)
            .toList();
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/waiting/{id}")
    public ResponseEntity<Void> deleteWaitingReservation(@AuthenticationPrincipal Member member, @PathVariable Long id) {
        waitingService.deleteWaiting(member, id);
        return ResponseEntity.noContent().build();
    }
}
