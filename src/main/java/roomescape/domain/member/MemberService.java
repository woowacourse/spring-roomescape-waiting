package roomescape.domain.member;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.support.exception.MemberErrorCode;
import roomescape.support.exception.RoomescapeException;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public Member findById(Long id) {
        return memberRepository.findById(id)
            .orElseThrow(() -> new RoomescapeException(MemberErrorCode.MEMBER_NOT_FOUND));
    }
}
