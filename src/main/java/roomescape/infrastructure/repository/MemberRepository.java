package roomescape.infrastructure.repository;

import roomescape.domain.Member;

import java.util.List;
import java.util.Optional;

public interface MemberRepository {

    Optional<Member> findByEmail(String email);

    List<Member> findAll();

    Member save(Member member);

    Optional<Member> findById(Long id);
}
