package roomescape.member.presentation.controller.admin;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.common.security.annotation.RequireRole;
import roomescape.member.application.MemberApplicationService;
import roomescape.member.domain.MemberRole;
import roomescape.member.presentation.dto.response.MemberWebResponse;

@RestController
public class AdminMemberController {

    private static final String TOKEN = "token";

    private final MemberApplicationService memberApplicationService;

    public AdminMemberController(final MemberApplicationService memberApplicationService) {
        this.memberApplicationService = memberApplicationService;
    }

    @RequireRole(MemberRole.ADMIN)
    @GetMapping("/admin/members")
    public ResponseEntity<List<MemberWebResponse>> findAllRegular() {
        return ResponseEntity.ok(memberApplicationService.findAllRegular());
    }
}
