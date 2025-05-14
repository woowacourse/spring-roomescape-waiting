package roomescape.member.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Account;
import roomescape.member.domain.MemberEmail;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Account save(Account account);

    Account findByMemberId(Long memberId);

    Optional<Account> findAccountByMemberEmail(MemberEmail email);
}
