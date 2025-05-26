package roomescape.reservation.ui;

import static roomescape.auth.domain.AuthRole.ADMIN;
import static roomescape.auth.domain.AuthRole.MEMBER;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.domain.MemberAuthInfo;
import roomescape.auth.domain.RequiresRole;
import roomescape.reservation.application.WaitingService;
import roomescape.reservation.ui.dto.request.CreateWaitingRequest;
import roomescape.reservation.ui.dto.response.WaitingResponse;
import roomescape.reservation.ui.dto.response.WaitingWithRankResponse;

@RestController
@RequestMapping("/waitings")
@RequiredArgsConstructor
public class WaitingRestController {

    private final WaitingService waitingService;

    @PostMapping
    @RequiresRole(authRoles = {ADMIN, MEMBER})
    public ResponseEntity<WaitingResponse> createWaiting(
            @RequestBody @Valid final CreateWaitingRequest.ForMember request,
            final MemberAuthInfo memberAuthInfo
    ) {
        final WaitingResponse response = waitingService.create(request, memberAuthInfo.id());

        return ResponseEntity.created(URI.create("/waitings/" + response.id()))
                .body(response);
    }

    @DeleteMapping("/{id}")
    @RequiresRole(authRoles = {ADMIN, MEMBER})
    public ResponseEntity<Void> deleteWaiting(
            @PathVariable final Long id,
            final MemberAuthInfo memberAuthInfo
    ) {
        waitingService.deleteIfOwner(id, memberAuthInfo.id());

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/mine")
    @RequiresRole(authRoles = {ADMIN, MEMBER})
    public ResponseEntity<List<WaitingWithRankResponse.ForMember>> findAllWaitingWithRankByMemberId(
            final MemberAuthInfo memberAuthInfo
    ) {
        return ResponseEntity.ok(waitingService.findAllWaitingWithRankByMemberId(memberAuthInfo.id()));
    }
}
