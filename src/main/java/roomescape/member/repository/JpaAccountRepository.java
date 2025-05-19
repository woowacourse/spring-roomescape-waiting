package roomescape.member.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.member.domain.Account;
import roomescape.member.domain.MemberEmail;

public interface JpaAccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findAccountByMemberEmail(MemberEmail email);
}
