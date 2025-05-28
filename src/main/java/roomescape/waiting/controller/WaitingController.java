package roomescape.waiting.controller;

import static roomescape.waiting.controller.response.WaitingSuccessCode.WAIT;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.web.resolver.Authenticated;
import roomescape.global.response.ApiResponse;
import roomescape.waiting.controller.request.WaitingRequest;
import roomescape.waiting.controller.response.WaitingResponse;
import roomescape.waiting.service.WaitingService;

@RequiredArgsConstructor
@RestController
public class WaitingController {

    private final WaitingService waitingService;

    @PostMapping("/waiting")
    public ResponseEntity<ApiResponse<WaitingResponse>> wait(
            @Valid @RequestBody WaitingRequest request,
            @Authenticated Long memberId
    ) {
        WaitingResponse response = waitingService.wait(request, memberId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(WAIT, response));
    }
}
