package roomescape.dao;

import roomescape.domain.member.Member;

import java.util.List;
import java.util.Optional;

public interface MemberDao {

    Member save(final Member member);

    Optional<Member> findById(final Long id);

    Optional<Member> findByEmail(final String email);

    List<Member> findAll();
}
