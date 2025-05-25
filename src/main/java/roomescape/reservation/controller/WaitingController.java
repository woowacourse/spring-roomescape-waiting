package roomescape.reservation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.global.auth.annotation.AuthenticationPrincipal;
import roomescape.global.auth.annotation.RoleRequired;
import roomescape.global.auth.dto.LoginMember;
import roomescape.member.entity.RoleType;
import roomescape.reservation.dto.request.WaitingCreateRequest;
import roomescape.reservation.dto.response.WaitingCreateResponse;
import roomescape.reservation.service.WaitingService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/waitings")
public class WaitingController {

    private final WaitingService waitingService;

    @PostMapping
    public ResponseEntity<WaitingCreateResponse> createWaiting(
            @AuthenticationPrincipal LoginMember loginMember,
            @RequestBody @Valid WaitingCreateRequest request
    ) {
        WaitingCreateResponse response = waitingService.createWaiting(loginMember.id(), request);
        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("/{id}")
    @RoleRequired(roleType = {RoleType.USER, RoleType.ADMIN})
    public ResponseEntity<Void> deleteWaiting(
            @AuthenticationPrincipal LoginMember loginMember,
            @PathVariable("id") long id
    ) {
        waitingService.deleteWaiting(loginMember.id(), id);
        return ResponseEntity.noContent().build();
    }
}
