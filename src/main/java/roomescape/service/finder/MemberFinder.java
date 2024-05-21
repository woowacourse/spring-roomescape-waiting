package roomescape.service.finder;

import static roomescape.exception.ExceptionType.NOT_FOUND_MEMBER;

import org.springframework.stereotype.Service;
import roomescape.domain.Member;
import roomescape.exception.RoomescapeException;
import roomescape.repository.MemberRepository;

@Service
public class MemberFinder {
    private final MemberRepository memberRepository;

    public MemberFinder(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Member findById(long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new RoomescapeException(NOT_FOUND_MEMBER));
    }
}
