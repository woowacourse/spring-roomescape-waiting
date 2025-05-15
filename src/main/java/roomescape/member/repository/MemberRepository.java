package roomescape.member.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;

public interface MemberRepository extends ListCrudRepository<Member, Long> {

    @Query(   " SELECT m                    "
            + " FROM Member m            "
            + " WHERE m.memberRole = :memberRole ")
    List<Member> findAllByMemberRole(@Param("memberRole") final MemberRole memberRole);

    boolean existsByEmailAndPassword(final String email, final String password);

    Optional<Member> findByEmailAndPassword(final String email, final String password);
}
