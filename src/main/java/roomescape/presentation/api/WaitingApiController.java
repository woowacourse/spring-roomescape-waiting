package roomescape.presentation.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.AuthRequired;
import roomescape.auth.Role;
import roomescape.business.application_service.reader.WaitingReader;
import roomescape.business.dto.WaitingDto;
import roomescape.business.model.vo.UserRole;
import roomescape.presentation.dto.response.WaitingResponse;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class WaitingApiController {

    private final WaitingReader waitingReader;

    @GetMapping("/waitings")
    @AuthRequired
    @Role(UserRole.ADMIN)
    public ResponseEntity<List<WaitingResponse>> getWaitings() {
        List<WaitingDto> dtos = waitingReader.getAll();
        List<WaitingResponse> responses = WaitingResponse.from(dtos);
        return ResponseEntity.ok(responses);
    }
}
