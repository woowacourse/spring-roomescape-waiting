package roomescape.member.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.common.exception.AlreadyExistException;
import roomescape.member.domain.Account;
import roomescape.member.domain.MemberEmail;

public class FakeAccountRepository implements AccountRepository {

    private final List<Account> accounts = Collections.synchronizedList(new ArrayList<>());
    private final AtomicLong index = new AtomicLong(1L);

    @Override
    public Account save(Account account) {
        if (accounts.stream()
                .anyMatch(ac -> ac.getId().equals(account.getMember().getId()))) {
            throw new AlreadyExistException("이미 존재하는 계정입니다.");
        }

        Account saved = Account.withId(
                index.getAndIncrement(),
                account.getMember(),
                account.getPassword());
        accounts.add(saved);

        return saved;
    }

    @Override
    public Optional<Account> findAccountByMemberEmail(MemberEmail email) {
        return accounts.stream()
                .filter(account -> account.getMember().getEmail().equals(email))
                .findFirst();
    }
}
