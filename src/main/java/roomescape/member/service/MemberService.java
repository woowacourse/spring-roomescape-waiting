package roomescape.member.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.StreamSupport;
import org.springframework.stereotype.Service;
import roomescape.member.controller.dto.request.SignupRequest;
import roomescape.member.domain.Email;
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

    public Member findById(final long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("[ERROR] 잘못된 회원 번호를 입력하였습니다."));
    }

    public Member findByEmail(final Email email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("[ERROR] (email : " + email + ") 에 대한 사용자가 존재하지 않습니다."));
    }
}
