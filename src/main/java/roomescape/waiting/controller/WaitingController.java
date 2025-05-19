package roomescape.waiting.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.member.dto.MemberResponse;
import roomescape.member.login.authentication.AuthenticationPrincipal;
import roomescape.waiting.dto.WaitingRequest;
import roomescape.waiting.dto.WaitingResponse;
import roomescape.waiting.service.WaitingService;

@RestController
@RequestMapping("waitings")
@AllArgsConstructor
public class WaitingController {
    private final WaitingService waitingService;

    @PostMapping
    public ResponseEntity<WaitingResponse> add(
            @AuthenticationPrincipal MemberResponse memberResponse,
            @RequestBody WaitingRequest request
    ) {
        WaitingResponse response = waitingService.add(
                memberResponse.id(),
                request
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
