package roomescape.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.auth.Login;
import roomescape.service.WaitingService;
import roomescape.service.dto.request.LoginMember;
import roomescape.service.dto.request.WaitingRequest;
import roomescape.service.dto.response.WaitingResponse;

@RestController
@RequestMapping("/waitings")
public class WaitingController {
    private final WaitingService waitingService;

    public WaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping
    public ResponseEntity<WaitingResponse> saveWaiting(
            @Login LoginMember member,
            @RequestBody @Valid WaitingRequest waitingRequest
    ){
        WaitingResponse waitingResponse = waitingService.saveWaiting(waitingRequest, member.id());
        return ResponseEntity.ok(waitingResponse);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteWaiting(@RequestParam(name="id") long id){
        waitingService.deleteWaiting(id);
        return ResponseEntity.noContent().build();

    }
}
