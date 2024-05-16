package roomescape.member.domain.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.member.domain.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

    @Query("""
            SELECT m FROM Member m 
            WHERE m.email.email = :email
            """)
    Optional<Member> findByEmail(String email);

    @Query("""
            SELECT COUNT(m) > 0 FROM Member m 
            WHERE m.email.email = :email
            """)
    boolean existsByEmail(String email);
}
