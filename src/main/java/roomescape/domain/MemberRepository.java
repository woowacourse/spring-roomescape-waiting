package roomescape.domain;

import roomescape.persistence.query.CreateMemberQuery;

import java.util.List;
import java.util.Optional;

public interface MemberRepository {

    Optional<Member> findByEmail(String email);

    Optional<Member> findById(Long id);

    Long create(CreateMemberQuery createMemberQuery);

    List<Member> findAll();
}
