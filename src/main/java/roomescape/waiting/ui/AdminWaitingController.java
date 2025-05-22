package roomescape.waiting.ui;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.waiting.application.WaitingService;
import roomescape.waiting.application.dto.WaitingResponse;

@RestController
@RequestMapping("admin/waitings")
@AllArgsConstructor
public class AdminWaitingController {
    private final WaitingService waitingService;

    @PostMapping("/{id}/approve")
    public ResponseEntity<Void> approve(@PathVariable("id") Long id) {
        waitingService.approve(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<WaitingResponse>> findAll() {
        List<WaitingResponse> response = waitingService.findAll();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        waitingService.deleteByAdmin(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
