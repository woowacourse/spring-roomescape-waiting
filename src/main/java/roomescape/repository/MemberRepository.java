package roomescape.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import roomescape.domain.Member;

public interface MemberRepository extends CrudRepository<Member, Long> {

    List<Member> findAll();

    boolean existsByEmail(String email);

    Optional<Member> findByEmailAndPassword(String email, String password);
}
