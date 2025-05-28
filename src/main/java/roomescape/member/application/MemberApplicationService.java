package roomescape.member.application;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.common.security.application.MyPasswordEncoder;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.member.presentation.dto.request.SignupWebRequest;
import roomescape.member.presentation.dto.response.MemberWebResponse;
import roomescape.member.presentation.dto.response.SignUpWebResponse;
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

    public SignUpWebResponse signup(final SignupWebRequest signupWebRequest) {
        String encodedPassword = myPasswordEncoder.encode(signupWebRequest.password());
        Member member = new Member(signupWebRequest.name(), signupWebRequest.email(), encodedPassword, MemberRole.REGULAR);
        validateMemberExists(signupWebRequest);
        return SignUpWebResponse.from(memberDataService.create(member));
    }

    public List<MemberWebResponse> findAllRegular() {
        return memberDataService.findByMemberRole(MemberRole.REGULAR).stream()
                .map(member -> new MemberWebResponse(member.getId(), member.getName()))
                .toList();
    }

    private void validateMemberExists(final SignupWebRequest signupWebRequest) {
        if (memberDataService.existsByEmail(signupWebRequest.email())) {
            throw new MemberDuplicatedException("이미 존재하는 회원입니다.");
        }
    }
}
