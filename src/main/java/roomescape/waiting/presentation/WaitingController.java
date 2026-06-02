package roomescape.waiting.presentation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.common.api.ApiResponse;
import roomescape.member.AuthenticatedMember;
import roomescape.member.LoginMember;
import roomescape.waiting.application.WaitingService;
import roomescape.waiting.dto.request.WaitingRequest;
import roomescape.waiting.dto.response.WaitingResponse;

@RestController
@RequestMapping("/api/user/waitings")
@RequiredArgsConstructor
public class WaitingController {

    private final WaitingService waitingService;

    @PostMapping
    public ResponseEntity<ApiResponse<WaitingResponse>> save(
            @RequestBody @Valid WaitingRequest body,
            @LoginMember AuthenticatedMember member
    ) {
        WaitingResponse response = waitingService.save(body, member.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteByUser(
            @PathVariable @Positive long id,
            @LoginMember AuthenticatedMember member
    ) {
        waitingService.deleteByIdForUser(id, member.id());
        return ResponseEntity.noContent().build();
    }
}
