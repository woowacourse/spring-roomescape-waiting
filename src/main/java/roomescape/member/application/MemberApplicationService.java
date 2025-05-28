package roomescape.member.application;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.common.security.application.MyPasswordEncoder;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.member.presentation.dto.request.SignupRequest;
import roomescape.member.presentation.dto.response.MemberResponse;
import roomescape.member.presentation.dto.response.SignUpResponse;
import roomescape.member.exception.MemberDuplicatedException;

@Service
public class MemberApplicationService {

    private final MemberDataService memberDataService;
    private final MyPasswordEncoder myPasswordEncoder;

    public MemberApplicationService(final MemberDataService memberDataService,
                                    final MyPasswordEncoder myPasswordEncoder) {
        this.memberDataService = memberDataService;
        this.myPasswordEncoder = myPasswordEncoder;
    }

    public SignUpResponse signup(final SignupRequest signupRequest) {
        String encodedPassword = myPasswordEncoder.encode(signupRequest.password());
        Member member = new Member(signupRequest.name(), signupRequest.email(), encodedPassword, MemberRole.REGULAR);
        validateMemberExists(signupRequest);
        return SignUpResponse.from(memberDataService.create(member));
    }

    public List<MemberResponse> findAllRegular() {
        return memberDataService.findByMemberRole(MemberRole.REGULAR).stream()
                .map(member -> new MemberResponse(member.getId(), member.getName()))
                .toList();
    }

    private void validateMemberExists(final SignupRequest signupRequest) {
        if (memberDataService.existsByEmail(signupRequest.email())) {
            throw new MemberDuplicatedException("이미 존재하는 회원입니다.");
        }
    }
}
