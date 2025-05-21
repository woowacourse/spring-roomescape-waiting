package roomescape.member.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import roomescape.auth.dto.request.MemberSignUpRequest;
import roomescape.auth.dto.response.MemberSignUpResponse;
import roomescape.member.dto.response.MemberNameSelectResponse;

@Service
public class MemberServiceFacade {

    private final MemberService memberService;

    @Autowired
    public MemberServiceFacade(MemberService memberService) {
        this.memberService = memberService;
    }

    public MemberSignUpResponse signup(MemberSignUpRequest request) {
        return memberService.signup(request);
    }

    public List<MemberNameSelectResponse> findMemberNames() {
        return memberService.findMemberNames();
    }
}
