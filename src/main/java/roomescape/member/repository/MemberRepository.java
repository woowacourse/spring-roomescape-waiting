package roomescape.member.repository;

import java.util.List;
import java.util.Optional;
import roomescape.member.domain.Account;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberEmail;
import roomescape.member.domain.MemberId;

public interface MemberRepository {

    boolean existsByEmail(MemberEmail email);

    Member save(Account account);

    Optional<Member> findById(MemberId id);

    Optional<Account> findAccountByEmail(MemberEmail email);

    List<Member> findAll();
}
