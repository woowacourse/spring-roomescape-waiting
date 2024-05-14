package roomescape.member.domain.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.member.domain.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findById(long id);

    Optional<Member> findByEmail(String email);

    @Query(value = "SELECT 1 "
            + " FROM member "
            + " WHERE email = ? AND password = ? "
            + " LIMIT 1;" ,nativeQuery = true)
    int existsByEmailAndPassword(String email, String password);

    boolean existsByEmail(String email);
}
