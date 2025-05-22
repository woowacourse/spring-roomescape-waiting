package roomescape.controller;

import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.config.annotation.RequiredAccessToken;
import roomescape.dto.business.AccessTokenContent;
import roomescape.dto.business.WaitingCreationContent;
import roomescape.dto.request.WaitingCreationRequest;
import roomescape.dto.response.WaitingResponse;
import roomescape.service.WaitingService;

@RestController
@RequestMapping("/waiting")
public class WaitingController {

    private final WaitingService waitingService;

    public WaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping
    public ResponseEntity<WaitingResponse> addWaiting(
            @RequestBody WaitingCreationRequest request,
            @RequiredAccessToken AccessTokenContent token
    ) {
        WaitingCreationContent creationContent =
                new WaitingCreationContent(request.date(), request.themeId(), request.timeId(), token.id());
        WaitingResponse waitingResponse = waitingService.addWaiting(creationContent);
        return ResponseEntity.created(URI.create("/waiting/" + waitingResponse.id())).body(waitingResponse);
    }
}
