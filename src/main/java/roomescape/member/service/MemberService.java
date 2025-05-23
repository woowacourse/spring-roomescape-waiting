package roomescape.member.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.DataNotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepositoryInterface;

@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberRepositoryInterface memberRepository;

    @Transactional(readOnly = true)
    public Member findMemberByEmail(final String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new DataNotFoundException("해당 회원 데이터가 존재하지 않습니다. email = " + email));
    }

    @Transactional(readOnly = true)
    public List<Member> findAll() {
        return memberRepository.findAll();
    }
}
