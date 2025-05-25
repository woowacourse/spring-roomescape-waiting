package roomescape.member.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.dto.request.MemberSignUpRequest;
import roomescape.auth.dto.response.MemberSignUpResponse;
import roomescape.member.dto.response.MemberNameSelectResponse;

import java.util.List;

@Service
public class MemberServiceFacade {

    private final MemberService memberService;

    @Autowired
    public MemberServiceFacade(MemberService memberService) {
        this.memberService = memberService;
    }

    @Transactional
    public MemberSignUpResponse signup(MemberSignUpRequest request) {
        return memberService.signup(request);
    }

    @Transactional
    public List<MemberNameSelectResponse> getMemberNames() {
        return memberService.findMemberNames();
    }

}
