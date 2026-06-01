package roomescape.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.member.service.dto.MemberSaveCommand;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public Member register(MemberSaveCommand command) {
        // TODO name Unique 제약 조건 추가.
        return memberRepository.save(Member.register(command.name(), command.password()));
    }

}
