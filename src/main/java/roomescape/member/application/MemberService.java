package roomescape.member.application;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.impl.NotFoundException;
import roomescape.member.application.dto.MemberResponse;
import roomescape.member.domain.Member;
import roomescape.member.domain.repository.MemberRepository;

@Service
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(final MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public List<MemberResponse> findAll() {
        final List<Member> members = memberRepository.findAll();
        return members.stream()
                .map(MemberResponse::of)
                .toList();
    }

    public MemberResponse findById(final Long id) {
        return MemberResponse.of(memberRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("해당하는 사용자가 없습니다.")));
    }
}
