package roomescape.member.repository;

import java.util.Optional;
import roomescape.member.domain.Account;
import roomescape.member.domain.MemberEmail;

public interface AccountRepository {

    Account save(Account account);

    Optional<Account> findAccountByMemberEmail(MemberEmail email);
}
