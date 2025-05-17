package roomescape.member.service.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.common.exception.NotFoundException;
import roomescape.member.controller.dto.LoginRequest;
import roomescape.member.domain.Account;
import roomescape.member.domain.MemberEmail;
import roomescape.member.repository.AccountRepository;

@Service
@RequiredArgsConstructor
public class AccountQueryUseCase {

    private final AccountRepository accountRepository;

    public Account getAccount(LoginRequest loginRequest) {
        return accountRepository.findAccountByMemberEmail(MemberEmail.from(loginRequest.email()))
                .orElseThrow(() -> new NotFoundException("등록된 이메일이 존재하지 않습니다."));
    }
}
