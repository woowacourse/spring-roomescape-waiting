package roomescape.member.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.member.domain.Account;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberEmail;

public class FakeAccountRepository implements AccountRepository {

    private final List<Account> accounts = Collections.synchronizedList(new ArrayList<>());
    private final AtomicLong index = new AtomicLong(1L);

    @Override
    public Account save(Account account) {
        Account saved = Account.withId(
                index.getAndIncrement(),
                Member.withId(
                        index.get() - 1,
                        account.getMember().getName(),
                        account.getMember().getEmail(),
                        account.getMember().getRole()),
                account.getPassword());
        accounts.add(saved);

        return saved;
    }

    @Override
    public Account findByMemberId(Long memberId) {
        return accounts.stream()
                .filter(account -> account.getMember().getId().equals(memberId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Optional<Account> findAccountByMemberEmail(MemberEmail email) {
        return accounts.stream()
                .filter(account -> account.getMember().getEmail().equals(email))
                .findFirst();
    }
}
