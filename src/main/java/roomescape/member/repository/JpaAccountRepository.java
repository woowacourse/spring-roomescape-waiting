package roomescape.member.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Account;
import roomescape.member.domain.MemberEmail;

@Repository
public interface JpaAccountRepository extends AccountRepository, JpaRepository<Account, Long> {

    @Override
    Account findByMemberId(Long memberId);

    @Override
    Optional<Account> findAccountByMemberEmail(MemberEmail email);

    @Override
    Account save(Account account);
}
