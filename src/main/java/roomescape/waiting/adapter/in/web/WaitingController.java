package roomescape.waiting.adapter.in.web;

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
import roomescape.member.domain.AuthenticatedMember;
import roomescape.member.domain.LoginMember;
import roomescape.waiting.application.port.in.CancelWaitingUseCase;
import roomescape.waiting.application.port.in.CreateWaitingUseCase;
import roomescape.waiting.application.port.in.CreateWaitingUseCase;
import roomescape.waiting.application.dto.request.WaitingRequest;
import roomescape.waiting.application.dto.response.WaitingResponse;

@RestController
@RequestMapping("/api/user/waitings")
@RequiredArgsConstructor
public class WaitingController {

    private final CreateWaitingUseCase createWaitingUseCase;
    private final CancelWaitingUseCase cancelWaitingUseCase;

    @PostMapping
    public ResponseEntity<ApiResponse<WaitingResponse>> save(
            @RequestBody @Valid WaitingRequest body,
            @LoginMember AuthenticatedMember member
    ) {
        WaitingResponse response = createWaitingUseCase.save(body, member.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteByUser(
            @PathVariable @Positive long id,
            @LoginMember AuthenticatedMember member
    ) {
        cancelWaitingUseCase.deleteByIdForUser(id, member.id());
        return ResponseEntity.noContent().build();
    }
}
