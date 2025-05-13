package roomescape.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Member save(Member member); //TODO: 이미 구현되어있어서 나중에 삭제

    List<Member> findAll(); //TODO: 이미 구현되어있으므로 나중에 삭제

    Optional<Member> findById(long id); //TODO: 이미 구현되어있으므로 나중에 삭제

    Optional<Member> findByEmailAndPassword(String email, String password);

    boolean existsByEmail(String email);
}
