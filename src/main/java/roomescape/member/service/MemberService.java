package roomescape.member.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.global.auth.dto.UserInfo;
import roomescape.global.auth.service.MyPasswordEncoder;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.member.dto.MemberResponse;
import roomescape.member.dto.SignupRequest;
import roomescape.member.exception.MemberDuplicatedException;
import roomescape.member.exception.MemberNotFoundException;
import roomescape.member.repository.MemberRepository;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final MyPasswordEncoder myPasswordEncoder;

    public MemberService(final MemberRepository memberRepository, final MyPasswordEncoder myPasswordEncoder) {
        this.memberRepository = memberRepository;
        this.myPasswordEncoder = myPasswordEncoder;
    }

    public Member signup(final SignupRequest signupRequest) {
        String encodedPassword = myPasswordEncoder.encode(signupRequest.password());
        Member member = new Member(signupRequest.name(), signupRequest.email(), encodedPassword, MemberRole.USER);
        if (memberRepository.existsByEmail(signupRequest.email())) {
            throw new MemberDuplicatedException("이미 존재하는 회원입니다.");
        }
        return memberRepository.save(member);
    }

    public List<MemberResponse> findAllUsers() {
        return memberRepository.findAllByMemberRole(MemberRole.USER).stream()
                .map(member -> new MemberResponse(member.getId(), member.getName()))
                .toList();
    }

    public Member getMember(final UserInfo userInfo) {
        return memberRepository.findById(userInfo.id())
                .orElseThrow(()->new MemberNotFoundException("존재하지 않은 멤버입니다."));
    }
}
