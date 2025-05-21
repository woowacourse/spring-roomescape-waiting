package roomescape.member.infrastructure;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;

public interface JpaMemberRepository extends ListCrudRepository<Member, Long> {

    List<Member> findByMemberRole(MemberRole memberRole);

    Optional<Member> findByEmail(String email);

    @Query("SELECT EXISTS (SELECT 1 FROM Member m WHERE m.email = :email) ")
    boolean existsByEmail(String email);
}
