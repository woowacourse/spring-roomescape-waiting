package roomescape.member.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import roomescape.member.domain.Member;

public interface MemberRepository extends CrudRepository<Member, Long> {

    @Query("SELECT m.name FROM Member m WHERE m.email = :email")
    Optional<String> findNameByEmail(@Param("email") final String email);

    boolean existsByEmailAndPassword(final String email, final String password);

    Optional<Member> findByEmail(final String email);

    Optional<Member> findById(final Long memberId);

    List<Member> findAll();
}
