package roomescape.member.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    @Query("select m from Member m where m.email = :email and m.password.password = :password")
    Optional<Member> findByEmailAndPassword(@Param(value = "email") String email,
                                            @Param(value = "password") String Password);

    boolean existsByEmail(String email);
}
