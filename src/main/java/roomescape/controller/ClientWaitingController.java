package roomescape.controller;

import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.Member;
import roomescape.domain.dto.WaitingRequest;
import roomescape.domain.dto.WaitingResponse;
import roomescape.service.WaitingService;

@RestController
@RequestMapping("/waiting")
public class ClientWaitingController {
    private final WaitingService waitingService;

    public ClientWaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping
    public ResponseEntity<WaitingResponse> create(@RequestBody WaitingRequest waitingRequest,
                                                  Member member) {
        WaitingResponse response = waitingService.create(waitingRequest.with(member.getId()));
        return ResponseEntity.created(URI.create("/waiting/" + response.id())).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Member member){
        waitingService.delete(id, member);
        return ResponseEntity.noContent().build();
    }
}
