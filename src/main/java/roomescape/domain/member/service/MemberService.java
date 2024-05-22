package roomescape.domain.member.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.member.domain.Member;
import roomescape.domain.member.exception.InvalidEmailPasswordException;
import roomescape.domain.member.repository.MemberRepository;
import roomescape.global.exception.NoMatchingDataException;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    public Member getMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new NoMatchingDataException("없는 member를 조회 했습니다."));
    }

    public Member getMemberByEmailAndPassword(String email, String password) {
        return memberRepository.findByEmailAndPassword(email, password)
                .orElseThrow(InvalidEmailPasswordException::new);
    }
}
