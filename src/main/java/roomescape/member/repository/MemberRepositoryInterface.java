package roomescape.member.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Account;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberEmail;

@Repository
public interface MemberRepositoryInterface {

    boolean existsByEmail(MemberEmail email);

    Member save(Account account);

    Optional<Member> findById(Long id);

    Optional<Account> findAccountByEmail(MemberEmail email);

    List<Member> findAll();
}
