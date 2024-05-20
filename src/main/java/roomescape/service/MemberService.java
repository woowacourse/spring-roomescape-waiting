package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;
import roomescape.global.exception.RoomescapeException;
import roomescape.repository.MemberRepository;

@Service
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional(readOnly = true)
    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Member findById(Long memberId) {
        if (memberId == -1L) {
            return new Member(-1L, "noMember", "noMember@no.com", "123456a!", Role.NOT_REGISTERED);
        }
        return memberRepository.findById(memberId)
            .orElseThrow(() -> new RoomescapeException("입력한 사용자 ID에 해당하는 데이터가 존재하지 않습니다."));
    }
}
