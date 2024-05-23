package roomescape.waiting.controller;

import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.dto.LoggedInMember;
import roomescape.waiting.dto.WaitingCreateRequest;
import roomescape.waiting.dto.WaitingResponse;
import roomescape.waiting.service.WaitingService;

@RestController
@RequestMapping("/waitings")
public class WaitingController {
    private final WaitingService service;

    public WaitingController(WaitingService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<WaitingResponse> createWaiting(
            @RequestBody WaitingCreateRequest request,
            LoggedInMember member) {
        WaitingResponse response = service.createWaiting(request, member.id());

        URI location = URI.create("/waitings/" + response.id());
        return ResponseEntity.created(location)
                .body(response);
    }
}
