package roomescape.member.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;
import roomescape.member.domain.Role;
import roomescape.member.dto.request.SignupRequest;
import roomescape.member.dto.response.MemberResponse;
import roomescape.member.dto.response.SignupResponse;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public SignupResponse createUser(SignupRequest request) {
        if (memberRepository.existByEmail(request.email())) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다");
        }

        Member member = Member.createWithoutId(request.name(), request.email(), request.password(), Role.USER);
        Long id = memberRepository.save(member);
        return SignupResponse.from(member.assignId(id));
    }

    public List<MemberResponse> findAllMember() {
        List<Member> members = memberRepository.findAll();
        return members.stream()
                .map(MemberResponse::from)
                .toList();
    }
}
