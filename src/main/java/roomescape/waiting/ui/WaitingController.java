package roomescape.waiting.ui;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.annotation.LoginMemberId;
import roomescape.common.response.ApiResponse;
import roomescape.waiting.application.WaitingService;
import roomescape.waiting.application.dto.WaitingRequest;
import roomescape.waiting.application.dto.WaitingResponse;

@RestController
@AllArgsConstructor
@RequestMapping("waitings")
public class WaitingController {
    private final WaitingService waitingService;

    @PostMapping
    public ResponseEntity<ApiResponse<WaitingResponse>> add(
            @LoginMemberId Long memberId,
            @Valid @RequestBody WaitingRequest request
    ) {
        WaitingResponse response = waitingService.create(memberId, request);
        ApiResponse<WaitingResponse> apiResponse = ApiResponse.createSuccess(response);
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("id") Long id, @LoginMemberId Long memberId) {
        waitingService.deleteByUser(id, memberId);
        ApiResponse<Void> apiResponse = ApiResponse.createSuccessWithNoData();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(apiResponse);
    }
}
