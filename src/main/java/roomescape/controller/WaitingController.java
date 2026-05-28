package roomescape.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.WaitingRequestDTO;
import roomescape.dto.WaitingResponseDTO;
import roomescape.service.WaitingService;

@RestController
@RequestMapping("/api/waiting")
public class WaitingController {

    private final WaitingService waitingService;

    public WaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping
    public ResponseEntity<Void> add(@Valid @RequestBody WaitingRequestDTO request) {
        WaitingResponseDTO saved = waitingService.addWaiting(request);
        return ResponseEntity.created(URI.create("/api/waiting/" + saved.id())).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable @Positive(message = "예약 대기 아이디는 1 이상이어야 합니다.") Long id) {
        waitingService.deleteWaiting(id);
        return ResponseEntity.noContent().build();
    }
}
