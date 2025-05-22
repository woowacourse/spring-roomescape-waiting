package roomescape.reservation.waiting;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.auth.AuthenticationPrincipal;
import roomescape.auth.dto.LoginMember;

@RestController
@RequestMapping("/waitings")
@AllArgsConstructor
public class WaitingController {

    private final WaitingService waitingService;

    @PostMapping
    public ResponseEntity<WaitingResponse> create(
            @RequestBody @Valid final WaitingRequest request,
            @AuthenticationPrincipal final LoginMember member
    ) {
        final WaitingResponse response = waitingService.create(request, member);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(
            @PathVariable("id") final Long reservationId,
            @AuthenticationPrincipal final LoginMember member
    ) {
        waitingService.deleteByReservationId(reservationId, member);
        return ResponseEntity.noContent().build();
    }
}
