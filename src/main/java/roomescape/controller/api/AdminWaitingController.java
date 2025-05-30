package roomescape.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.dto.waiting.WaitingResponseDto;
import roomescape.service.waiting.WaitingCommandService;
import roomescape.service.waiting.WaitingQueryService;

import java.util.List;

@RestController
@RequestMapping("/admin/waitings")
public class AdminWaitingController {

    private final WaitingQueryService waitingQueryService;
    private final WaitingCommandService waitingCommandService;

    public AdminWaitingController(WaitingQueryService waitingQueryService, WaitingCommandService waitingCommandService) {
        this.waitingQueryService = waitingQueryService;
        this.waitingCommandService = waitingCommandService;
    }

    @GetMapping
    public ResponseEntity<List<WaitingResponseDto>> getWaitings() {
        List<WaitingResponseDto> allWaiting = waitingQueryService.findAllWaiting();
        return ResponseEntity.ok(allWaiting);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWaiting(@PathVariable Long id) {
        waitingCommandService.cancelWaiting(id);
        return ResponseEntity.noContent().build();
    }
}
