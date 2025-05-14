package roomescape.member.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Account;
import roomescape.member.domain.MemberEmail;
import roomescape.member.domain.MemberId;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Account save(Account account);

    Account findByMemberId(MemberId memberId);

    Optional<Account> findAccountByMemberEmail(MemberEmail email);
}
