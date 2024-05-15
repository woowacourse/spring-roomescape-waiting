package roomescape.member.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.member.domain.Member;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    @Query("""
            SELECT count(*) > 0 FROM Member m 
            WHERE m.email = :email AND m.password = :password
            """)
    boolean existsByEmailAndPassword(String email, String password);

    boolean existsByEmail(String email);
}
