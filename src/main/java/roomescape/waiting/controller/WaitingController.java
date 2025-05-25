package roomescape.waiting.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.global.auth.annotation.AuthenticationPrincipal;
import roomescape.global.auth.annotation.RoleRequired;
import roomescape.global.auth.dto.LoginMember;
import roomescape.member.entity.RoleType;
import roomescape.waiting.dto.request.WaitingCreateRequest;
import roomescape.waiting.dto.response.WaitingCreateResponse;
import roomescape.waiting.dto.response.WaitingReadResponse;
import roomescape.waiting.service.WaitingService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/waitings")
public class WaitingController {

    private final WaitingService waitingService;

    @PostMapping
    @RoleRequired(roleType = {RoleType.ADMIN, RoleType.USER})
    public ResponseEntity<WaitingCreateResponse> waitReservation(
            @AuthenticationPrincipal LoginMember loginMember,
            @RequestBody @Valid WaitingCreateRequest request
    ) {
        WaitingCreateResponse response = waitingService.createWaiting(loginMember, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @RoleRequired(roleType = RoleType.ADMIN)
    public ResponseEntity<List<WaitingReadResponse>> getAllWaitings() {
        List<WaitingReadResponse> responses = waitingService.getWaitings();
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/accept/{id}")
    @RoleRequired(roleType = RoleType.ADMIN)
    public ResponseEntity<Void> acceptWaiting(@PathVariable Long id) {
        waitingService.acceptWaiting(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @RoleRequired(roleType = {RoleType.ADMIN, RoleType.USER})
    public ResponseEntity<Void> deleteWaitingById(
            @PathVariable Long id,
            @AuthenticationPrincipal LoginMember loginMember
    ) {
        waitingService.deleteWaiting(id, loginMember);
        return ResponseEntity.noContent().build();
    }
}
