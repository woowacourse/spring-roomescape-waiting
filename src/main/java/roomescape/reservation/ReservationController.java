package roomescape.reservation;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.dto.LoginMember;
import roomescape.config.AuthenticationPrincipal;
import roomescape.reservation.dto.MineReservationResponse;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ReservationResponse> create(
            @RequestBody @Valid final ReservationRequest request,
            @AuthenticationPrincipal final LoginMember member
    ) {
        final ReservationResponse response = reservationService.create(request, member);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/waiting")
    public ResponseEntity<ReservationResponse> createWaiting(
            @RequestBody @Valid final ReservationRequest request,
            @AuthenticationPrincipal final LoginMember member
    ) {
        final ReservationResponse response = reservationService.createWaiting(request, member);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/mine")
    public ResponseEntity<List<MineReservationResponse>> readMine(
            @AuthenticationPrincipal final LoginMember member
    ) {
        final List<MineReservationResponse> response = reservationService.readAllMine(member);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> readAll() {
        final List<ReservationResponse> response = reservationService.readAll();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/waiting")
    public ResponseEntity<List<ReservationResponse>> readAllWaiting() {
        final List<ReservationResponse> response = reservationService.readAllWaiting();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(
            @PathVariable("id") final Long id
    ) {
        reservationService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
