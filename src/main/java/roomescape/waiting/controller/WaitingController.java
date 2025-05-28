package roomescape.waiting.controller;

import static org.springframework.http.HttpStatus.NO_CONTENT;
import static roomescape.waiting.controller.response.WaitingSuccessCode.CANCEL_WAITING_BY_USER;
import static roomescape.waiting.controller.response.WaitingSuccessCode.WAIT;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @PostMapping("/waitings")
    public ResponseEntity<ApiResponse<WaitingResponse>> wait(
            @Valid @RequestBody WaitingRequest request,
            @Authenticated Long memberId
    ) {
        WaitingResponse response = waitingService.wait(request, memberId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(WAIT, response));
    }

    @DeleteMapping("/waitings/{id}")
    public ResponseEntity<ApiResponse<Void>> cancel(
            @PathVariable Long id
    ) {
        waitingService.cancel(id);

        return ResponseEntity
                .status(NO_CONTENT)
                .body(ApiResponse.success(CANCEL_WAITING_BY_USER));
    }
}
