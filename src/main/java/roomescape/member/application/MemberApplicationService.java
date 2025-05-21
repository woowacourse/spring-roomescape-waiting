package roomescape.member.application;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.common.security.application.MyPasswordEncoder;
import roomescape.member.domain.Member;
import roomescape.member.domain.service.MemberDomainService;
import roomescape.member.domain.MemberRole;
import roomescape.member.presentation.dto.request.SignupRequest;
import roomescape.member.presentation.dto.response.MemberResponse;
import roomescape.member.presentation.dto.response.SignUpResponse;
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
        Member member = new Member(signupRequest.name(), signupRequest.email(), encodedPassword, MemberRole.REGULAR);
        if (memberDomainService.existsByEmail(signupRequest.email())) {
            throw new MemberDuplicatedException("이미 존재하는 회원입니다.");
        }
        return SignUpResponse.from(memberDomainService.save(member));
    }

    public List<MemberResponse> findAllRegularMembers() {
        return memberDomainService.findByMemberRole(MemberRole.REGULAR).stream()
                .map(member -> new MemberResponse(member.getId(), member.getName()))
                .toList();
    }
}
