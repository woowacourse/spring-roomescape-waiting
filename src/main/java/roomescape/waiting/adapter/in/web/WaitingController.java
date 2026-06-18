package roomescape.waiting.adapter.in.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.common.api.ApiResponse;
import roomescape.member.domain.AuthenticatedMember;
import roomescape.member.domain.LoginMember;
import roomescape.waiting.application.dto.request.WaitingRequest;
import roomescape.waiting.application.dto.response.WaitingDetailFindResponse;
import roomescape.waiting.application.dto.response.WaitingResponse;
import roomescape.waiting.application.port.in.CancelWaitingUseCase;
import roomescape.waiting.application.port.in.CreateWaitingUseCase;
import roomescape.waiting.application.port.in.FindWaitingUseCase;

@RestController
@RequiredArgsConstructor
public class WaitingController {

    private final CreateWaitingUseCase createWaitingUseCase;
    private final CancelWaitingUseCase cancelWaitingUseCase;
    private final FindWaitingUseCase findWaitingUseCase;

    @PostMapping({"/api/user/waitings", "/waitings"})
    public ResponseEntity<ApiResponse<WaitingResponse>> save(
            @RequestBody @Valid WaitingRequest body,
            @LoginMember AuthenticatedMember member
    ) {
        WaitingResponse response = createWaitingUseCase.save(body, member.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @DeleteMapping({"/api/user/waitings/{id}", "/waitings/{id}"})
    public ResponseEntity<ApiResponse<Void>> deleteByUser(
            @PathVariable @Positive long id,
            @LoginMember AuthenticatedMember member
    ) {
        cancelWaitingUseCase.deleteByIdForUser(id, member.id());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/manager/waitings")
    public ResponseEntity<ApiResponse<List<WaitingDetailFindResponse>>> findWaitingDetails() {
        List<WaitingDetailFindResponse> responses = findWaitingUseCase.findWaitingDetails();
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(responses));
    }

    @DeleteMapping("/api/manager/waitings/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteByManager(
            @PathVariable @Positive long id
    ) {
        cancelWaitingUseCase.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
