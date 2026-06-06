package roomescape.reservation.presentation.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservation.application.service.WaitingQueryService;
import roomescape.reservation.application.service.WaitingService;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.presentation.dto.WaitingResponse;

@RequiredArgsConstructor
@RequestMapping("/waitings")
@RestController
public class WaitingController {

    private final WaitingService waitingService;
    private final WaitingQueryService waitingQueryService;

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestParam String name
    ) {
        waitingService.delete(id, name);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<WaitingResponse>> findAll(@RequestParam String name) {
        return ResponseEntity.ok(waitingQueryService.findAllByName(name));
    }
}
