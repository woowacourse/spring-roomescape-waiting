package roomescape.reservation.waiting;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.reservation.waiting.dto.WaitingResponse;

import java.util.List;

@RestController
@RequestMapping("/admin/waitings")
@AllArgsConstructor
public class AdminWaitingController {

    private final WaitingService waitingService;

    @GetMapping
    public ResponseEntity<List<WaitingResponse>> readAll() {
        List<WaitingResponse> waitingResponses = waitingService.readAll();
        return ResponseEntity.ok(waitingResponses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(
            @PathVariable("id") final Long id
    ) {
        waitingService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
