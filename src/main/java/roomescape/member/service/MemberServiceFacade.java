package roomescape.member.service;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import roomescape.auth.dto.request.MemberSignUpRequest;
import roomescape.auth.dto.response.MemberSignUpResponse;
import roomescape.auth.infrastructure.methodargument.MemberPrincipal;
import roomescape.member.domain.Member;
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

    public Member findMemberById(Long id) {
        return memberService.findExistingMemberById(id);
    }

    public Member findExistingMemberByPrincipal(MemberPrincipal memberPrincipal) {
        return memberService.findExistingMemberByPrincipal(memberPrincipal);
    }

    public Optional<Member> findByEmail(String email) {
        return memberService.findByEmail(email);
    }

    public boolean isExistMemberById(Long id) {
        return memberService.isExistMemberById(id);
    }

    public List<MemberNameSelectResponse> findMemberNames() {
        return memberService.findMemberNames();
    }

    public boolean existsByName(String name) {
        return memberService.existsByName(name);
    }
}
