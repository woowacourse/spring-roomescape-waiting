package roomescape.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MemberRepository extends JpaRepository<Member, Long> {

    @Query( "select case when(count(*)>0) then true else false end from Member m where m.email = :email")
    boolean existByEmail(String email);

    Optional<Member> findByEmailAndPassword(String email, String password);
}
