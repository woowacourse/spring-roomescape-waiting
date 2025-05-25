package roomescape.application.member.query;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.member.query.dto.MemberResult;
import roomescape.domain.member.repository.MemberRepository;

@Service
@Transactional(readOnly = true)
public class MemberQueryService {

    private final MemberRepository memberRepository;

    public MemberQueryService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public List<MemberResult> findAll() {
        return memberRepository.findAll()
                .stream()
                .map(MemberResult::from)
                .toList();
    }
}
