package roomescape.member.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.member.model.Member;

import java.util.Optional;

@Repository
public interface JpaMemberDao extends JpaRepository<Member, Long> {

    Optional<Member> findByEmailAndPassword(String email, String password);

    boolean existsByEmail(String email);

    boolean existsByName(String name);
}
