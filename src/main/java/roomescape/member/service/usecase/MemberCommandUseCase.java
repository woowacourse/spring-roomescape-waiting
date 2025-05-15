package roomescape.member.service.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.common.exception.AlreadyExistException;
import roomescape.member.auth.vo.MemberInfo;
import roomescape.member.domain.Account;
import roomescape.member.repository.AccountRepository;
import roomescape.member.repository.MemberRepository;
import roomescape.member.service.MemberConverter;

@Service
@RequiredArgsConstructor
public class MemberCommandUseCase {

    private final MemberRepository memberRepository;
    private final AccountRepository accountRepository;

    public MemberInfo create(Account account) {
        if (memberRepository.existsByEmail(account.getMember().getEmail())) {
            throw new AlreadyExistException("이미 존재하는 이메일입니다.");
        }

        final MemberInfo dto = MemberConverter.toDto(memberRepository.save(account.getMember()));
        accountRepository.save(account);
        return dto;
    }
}
