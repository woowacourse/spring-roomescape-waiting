package roomescape.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.response.BookingResponse;
import roomescape.service.WaitingService;
import roomescape.service.dto.result.WaitingResult;

@RestController
@RequestMapping("/admin/waitings")
public class AdminWaitingController {

    private final WaitingService waitingService;

    public AdminWaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @GetMapping
    public List<BookingResponse> getAllWaitings() {
        List<WaitingResult> waitingResults = waitingService.getAll();
        return BookingResponse.fromWaitings(waitingResults);
    }

    @DeleteMapping("/{waitingId}")
    public ResponseEntity<Void> deleteWaiting(@PathVariable("waitingId") Long waitingId) {
        waitingService.denyWaitingByIdForAdmin(waitingId);
        return ResponseEntity.noContent().build();
    }
}
