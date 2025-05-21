package roomescape.waiting.controller;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.annotation.LoginMemberId;
import roomescape.member.dto.MemberResponse;
import roomescape.waiting.dto.WaitingRequest;
import roomescape.waiting.dto.WaitingResponse;
import roomescape.waiting.service.WaitingService;

@RestController
@RequestMapping("waitings")
@AllArgsConstructor
public class WaitingController {
    private final WaitingService waitingService;

    @PostMapping
    public ResponseEntity<WaitingResponse> add(
            @LoginMemberId MemberResponse memberResponse,
            @RequestBody WaitingRequest request
    ) {
        WaitingResponse response = waitingService.add(
                memberResponse.id(),
                request
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<WaitingResponse>> findAll() {
        List<WaitingResponse> response = waitingService.findAll();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<Void> approve(@PathVariable("id") Long id) {
        waitingService.approve(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable("id") Long id) {
        waitingService.deleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
