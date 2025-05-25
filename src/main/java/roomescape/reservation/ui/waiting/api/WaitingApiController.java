package roomescape.reservation.ui.waiting.api;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.member.application.dto.LoginMemberInfo;
import roomescape.reservation.application.waiting.dto.WaitingCreateCommand;
import roomescape.reservation.application.waiting.dto.WaitingInfo;
import roomescape.reservation.application.waiting.service.WaitingService;
import roomescape.reservation.ui.waiting.dto.WaitingCreateRequest;
import roomescape.reservation.ui.waiting.dto.WaitingResponse;

@RestController
@RequestMapping("/reservations/waitings")
public class WaitingApiController {

    private final WaitingService waitingService;

    public WaitingApiController(final WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping
    public ResponseEntity<WaitingResponse> create(
            @RequestBody @Valid final WaitingCreateRequest request,
            final LoginMemberInfo loginMemberInfo
    ) {
        final WaitingCreateCommand waitingCreateCommand = request.convertToCreateCommand(loginMemberInfo.id());
        final WaitingInfo waitingInfo = waitingService.createWaiting(waitingCreateCommand);
        final URI uri = URI.create("/reservations/waitings/" + waitingInfo.id());
        final WaitingResponse waitingResponse = new WaitingResponse(waitingInfo);
        return ResponseEntity.created(uri).body(waitingResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") final long id) {
        waitingService.cancelWaitingById(id);
        return ResponseEntity.noContent().build();
    }
}
