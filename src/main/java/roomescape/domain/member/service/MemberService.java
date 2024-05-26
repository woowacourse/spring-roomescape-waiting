package roomescape.domain.member.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.login.dto.LoginQuery;
import roomescape.domain.member.domain.Member;
import roomescape.domain.member.exception.InvalidEmailPasswordException;
import roomescape.domain.member.repository.MemberRepository;
import roomescape.global.exception.NoMatchingDataException;

@Service
public class MemberService {

    protected static final String NON_EXIST_MEMBER_ERROR_MESSAGE = "없는 member를 조회 했습니다.";

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    public Member getMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new NoMatchingDataException(NON_EXIST_MEMBER_ERROR_MESSAGE));
    }

    public Member getMemberByEmailAndPassword(LoginQuery loginQuery) {
        return memberRepository.findByEmailAndPassword(loginQuery.email(), loginQuery.password())
                .orElseThrow(InvalidEmailPasswordException::new);
    }
}
