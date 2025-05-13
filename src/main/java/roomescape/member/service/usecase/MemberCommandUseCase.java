package roomescape.member.service.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.common.exception.ConflictException;
import roomescape.member.auth.vo.MemberInfo;
import roomescape.member.domain.Account;
import roomescape.member.repository.MemberRepository;
import roomescape.member.service.MemberConverter;

@Service
@RequiredArgsConstructor
public class MemberCommandUseCase {

    private final MemberRepository memberRepository;

    public MemberInfo create(Account account) {
        if (memberRepository.existsByEmail(account.getMember().getEmail())) {
            throw new ConflictException("이미 존재하는 이메일입니다.");
        }

        return MemberConverter.toDto(memberRepository.save(account));
    }
}
