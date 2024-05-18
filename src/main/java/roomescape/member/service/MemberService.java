package roomescape.member.service;

import java.util.List;
import java.util.stream.StreamSupport;
import org.springframework.stereotype.Service;
import roomescape.member.controller.dto.request.SignupRequest;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(final MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Member save(final SignupRequest signupRequest) {
        return memberRepository.save(signupRequest.toEntity());
    }

    public List<Member> getAll() {
        return StreamSupport.stream(memberRepository.findAll().spliterator(), false).toList();
    }
}
