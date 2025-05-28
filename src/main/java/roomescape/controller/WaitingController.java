package roomescape.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.config.annotation.Authority;
import roomescape.config.annotation.RequiredAccessToken;
import roomescape.domain.Role;
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

    @GetMapping
    @Authority(Role.ADMIN)
    public List<WaitingResponse> findAllWaiting() {
        return waitingService.findAllWaiting();
    }

    @PostMapping
    @Authority(Role.GENERAL)
    public ResponseEntity<WaitingResponse> addWaiting(
            @Valid @RequestBody WaitingCreationRequest request,
            @RequiredAccessToken AccessTokenContent token
    ) {
        WaitingCreationContent creationContent =
                new WaitingCreationContent(request.date(), request.themeId(), request.timeId(), token.id());
        WaitingResponse waitingResponse = waitingService.addWaiting(creationContent);
        return ResponseEntity.created(URI.create("/waiting/" + waitingResponse.id())).body(waitingResponse);
    }

    @DeleteMapping("/mine/{id}")
    @Authority(Role.GENERAL)
    public ResponseEntity<Void> deleteMineWaitingById(
            @PathVariable("id") Long id,
            @RequiredAccessToken AccessTokenContent token
    ) {
        waitingService.deleteMineWaitingById(token.id(), id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Authority(Role.ADMIN)
    public ResponseEntity<Void> deleteWaitingById(
            @PathVariable("id") Long id
    ) {
        waitingService.deleteWaitingById(id);
        return ResponseEntity.noContent().build();
    }
}
