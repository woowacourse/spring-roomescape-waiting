package roomescape.repository;

import java.util.Locale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import roomescape.domain.member.Member;

import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface MemberRepository extends JpaRepository<Member, Long> {

    Member save(Member user);

    Optional<Member> findByEmailAndPassword(String email, String password);

    Optional<Member> findById(long id);

    List<Member> findAll();

    int deleteById(long id);
}
