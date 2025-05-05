package roomescape.repository.member;

import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.domain.member.Member;

@Repository
public class JdbcMemberRepository implements MemberRepository {

    @Override
    public long add(Member member) {
        return 0;
    }

    @Override
    public boolean existsByEmailAndPassword(Member member) {
        return false;
    }

    @Override
    public Optional<Member> findById(long id) {
        return Optional.empty();
    }
}
