package roomescape.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import roomescape.controller.request.WaitingRequest;
import roomescape.controller.response.WaitingResponse;
import roomescape.model.Waiting;
import roomescape.model.member.LoginMember;
import roomescape.service.WaitingService;
import roomescape.service.dto.WaitingDto;

import java.net.URI;

@Validated
@RestController
@RequestMapping("/reservations/waiting")
public class WaitingController {

    private final WaitingService waitingService;

    public WaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping
    public ResponseEntity<WaitingResponse> addWaiting(@Valid @RequestBody WaitingRequest request, LoginMember member) {
        WaitingDto waitingDto = request.toDto(member.getId());
        Waiting waiting = waitingService.saveWaiting(waitingDto);
        WaitingResponse response = WaitingResponse.from(waiting);
        return ResponseEntity
                .created(URI.create("/reservations/waiting/" + waiting.getId()))
                .body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWaiting(@NotNull @Min(1) @PathVariable("id") Long id, LoginMember member) {
        waitingService.deleteOwnWaiting(id, member);
        return ResponseEntity.noContent().build();
    }
}
