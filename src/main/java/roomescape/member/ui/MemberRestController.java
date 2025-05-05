package roomescape.member.ui;

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
import roomescape.auth.domain.AuthRole;
import roomescape.auth.domain.RequiresRole;
import roomescape.member.application.MemberService;
import roomescape.member.domain.Member;
import roomescape.member.ui.dto.CreateMemberRequest;
import roomescape.member.ui.dto.MemberResponse.IdName;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberRestController {

    private final MemberService memberService;

    @PostMapping
    public ResponseEntity<Void> create(
            @RequestBody @Valid final CreateMemberRequest request
    ) {
        memberService.create(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .build();
    }

    @DeleteMapping("/{id}")
    @RequiresRole(authRoles = {AuthRole.ADMIN, AuthRole.MEMBER})
    public ResponseEntity<Void> delete(
            @PathVariable final Long id,
            final Member member
    ) {
        if (id == null || member == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .build();
        }
        if (!member.getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .build();
        }
        memberService.delete(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .build();
    }

    @GetMapping
    @RequiresRole(authRoles = {AuthRole.ADMIN})
    public ResponseEntity<List<IdName>> findAll() {
        final List<IdName> responses = memberService.findAllNames();

        return ResponseEntity.status(HttpStatus.OK)
                .body(responses);
    }
}
