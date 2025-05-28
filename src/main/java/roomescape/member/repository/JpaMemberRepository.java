package roomescape.member.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.ListCrudRepository;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;

public interface JpaMemberRepository extends ListCrudRepository<Member, Long>, MemberRepository {

    List<Member> findByMemberRole(MemberRole memberRole);

    Optional<Member> findByEmail(String email);

    boolean existsByEmail(String email);

    Member save(Member member);

    Optional<Member> findById(Long id);
}
