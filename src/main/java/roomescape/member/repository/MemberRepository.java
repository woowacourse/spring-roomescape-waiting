package roomescape.member.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.member.domain.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

    @Query("SELECT m.name.value FROM Member m WHERE m.email.value = :email")
    Optional<String> findNameByEmail(@Param("email") final String email);

    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM Member m WHERE m.email.value = :email AND m.password.value = :password")
    boolean existsByEmailAndPassword(@Param("email") final String email, @Param("password") final String password);

    @Query("SELECT m FROM Member m WHERE m.email.value = :email")
    Optional<Member> findByEmail(@Param("email") final String email);

    Optional<Member> findById(final Long memberId);

    List<Member> findAll();
}
