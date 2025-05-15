package roomescape.member.service;

import java.util.List;
import java.util.stream.StreamSupport;
import org.springframework.stereotype.Service;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.member.dto.MemberResponse;
import roomescape.member.dto.SignupRequest;
import roomescape.member.exception.MemberDuplicatedException;
import roomescape.member.repository.MemberRepository;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(final MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Member signup(final SignupRequest signupRequest) {
        Member member = new Member(signupRequest.name(), signupRequest.email(), signupRequest.password(),
                MemberRole.USER);
        if (memberRepository.existsByEmailAndPassword(signupRequest.email(), signupRequest.password())) {
            throw new MemberDuplicatedException("이미 존재하는 회원입니다.");
        }
        return memberRepository.save(member);
    }

    public List<MemberResponse> findAllUsers() {
        return memberRepository.findAllByMemberRole(MemberRole.USER).stream()
                .map(member -> new MemberResponse(member.getId(), member.getName()))
                .toList();
    }
}
