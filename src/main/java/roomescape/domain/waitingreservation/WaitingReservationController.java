package roomescape.domain.waitingreservation;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.waitingreservation.dto.WaitingReservationCreationRequest;
import roomescape.domain.waitingreservation.dto.WaitingReservationCreationResponse;
import roomescape.domain.waitingreservation.dto.WaitingReservationWithRankResponse;

@RestController
@RequestMapping("/waiting-reservations")
@RequiredArgsConstructor
public class WaitingReservationController {

    private final WaitingReservationService waitingReservationService;

    @PostMapping
    public ResponseEntity<WaitingReservationCreationResponse> createWaitingReservation(
        @Valid @RequestBody WaitingReservationCreationRequest request) {
        WaitingReservationCreationResponse response = waitingReservationService.createWaitingReservation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelWaitingReservation(@PathVariable Long id) {
        waitingReservationService.cancelWaitingReservation(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<WaitingReservationWithRankResponse>> getWaitingReservations(
        @RequestParam Long memberId) {
        List<WaitingReservationWithRankResponse> response =
            waitingReservationService.getWaitingReservationsWithRankByMemberId(memberId);
        return ResponseEntity.ok(response);
    }
}
