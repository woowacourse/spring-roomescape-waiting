package roomescape.member.service.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.member.domain.Account;
import roomescape.member.repository.AccountRepository;

@Service
@RequiredArgsConstructor
public class AccountCommandUseCase {

    private final AccountRepository accountRepository;

    public Account create(Account account) {
        return accountRepository.save(account);
    }
}
