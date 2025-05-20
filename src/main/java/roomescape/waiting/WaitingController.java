package roomescape.waiting;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.AuthenticationPrincipal;
import roomescape.auth.dto.LoginMember;
import roomescape.waiting.dto.WaitingRequest;
import roomescape.waiting.dto.WaitingResponse;

@RestController
@RequestMapping("/waitings")
@AllArgsConstructor
public class WaitingController {

    private final WaitingService waitingService;

    @PostMapping
    public ResponseEntity<WaitingResponse> create(
            @RequestBody @Valid final WaitingRequest request,
            @AuthenticationPrincipal final LoginMember member
    ) {
        final WaitingResponse response = waitingService.create(request, member);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
