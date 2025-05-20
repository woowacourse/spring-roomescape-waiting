package roomescape.member.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.auth.service.MyPasswordEncoder;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.member.dto.request.SignupRequest;
import roomescape.member.dto.response.MemberResponse;
import roomescape.member.dto.response.SignUpResponse;
import roomescape.member.exception.MemberDuplicatedException;

@Service
public class MemberApplicationService {

    private final MemberDomainService memberDomainService;
    private final MyPasswordEncoder myPasswordEncoder;

    public MemberApplicationService(final MemberDomainService memberDomainService,
                                    final MyPasswordEncoder myPasswordEncoder) {
        this.memberDomainService = memberDomainService;
        this.myPasswordEncoder = myPasswordEncoder;
    }

    public SignUpResponse signup(final SignupRequest signupRequest) {
        String encodedPassword = myPasswordEncoder.encode(signupRequest.password());
        Member member = new Member(signupRequest.name(), signupRequest.email(), encodedPassword, MemberRole.USER);
        if (memberDomainService.existsByEmail(signupRequest.email())) {
            throw new MemberDuplicatedException("이미 존재하는 회원입니다.");
        }
        return SignUpResponse.from(memberDomainService.save(member));
    }

    public List<MemberResponse> findAllUsers() {
        return memberDomainService.findByMemberRole(MemberRole.USER).stream()
                .map(member -> new MemberResponse(member.getId(), member.getName()))
                .toList();
    }

    public Member getMember(Long memberId) {
        return memberDomainService.getMember(memberId);
    }
}
