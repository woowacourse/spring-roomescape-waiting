package roomescape.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.Member;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    List<Member> findAll();

    Member save(Member member);

    Optional<Member> findById(Long id);
}
