package roomescape.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

    @Query("select m from Member m left join fetch m.reservations where m.id = :memberId")
    Optional<Member> findFetchById(@Param("memberId") Long memberId);

    Optional<Member> findByEmailAndPassword(String email, String password);

    boolean existsByEmail(String email);
}
