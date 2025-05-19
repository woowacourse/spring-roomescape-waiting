package roomescape.member.repository;

import java.util.Optional;
import roomescape.member.domain.Account;
import roomescape.member.domain.MemberEmail;

public interface AccountRepository {

    Account findByMemberId(Long memberId);

    Optional<Account> findAccountByMemberEmail(MemberEmail email);

    Account save(Account account);
}
