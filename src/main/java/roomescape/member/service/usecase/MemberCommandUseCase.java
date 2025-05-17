package roomescape.member.service.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.common.exception.AlreadyExistException;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;

@Service
@RequiredArgsConstructor
public class MemberCommandUseCase {

    private final MemberRepository memberRepository;

    public Member create(Member member) {
        if (memberRepository.existsByEmail(member.getEmail())) {
            throw new AlreadyExistException("이미 존재하는 이메일입니다.");
        }

        return memberRepository.save(member);
    }
}
