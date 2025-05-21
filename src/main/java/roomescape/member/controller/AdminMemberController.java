package roomescape.member.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.member.application.AdminMemberService;
import roomescape.member.application.dto.response.MemberServiceResponse;
import roomescape.member.controller.dto.MemberResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/members")
public class AdminMemberController {

    private final AdminMemberService adminMemberService;

    @GetMapping
    public List<MemberResponse> getAll() {
        List<MemberServiceResponse> responses = adminMemberService.getAll();
        return responses.stream()
                .map(MemberResponse::from)
                .toList();
    }
}
