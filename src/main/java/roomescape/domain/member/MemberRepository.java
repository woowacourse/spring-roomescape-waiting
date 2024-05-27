package roomescape.domain.member;

import java.util.List;

public interface MemberRepository {

    Member save(Member member);

    List<Member> findAll();

    boolean existsByEmail(Email email);

    Member getById(long id);

    Member getByEmail(Email email);
}
