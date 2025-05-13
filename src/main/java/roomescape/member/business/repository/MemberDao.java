package roomescape.member.business.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.member.business.domain.Email;
import roomescape.member.business.domain.Member;
import roomescape.member.business.domain.Password;

public interface MemberDao extends JpaRepository<Member, Long> {

    Optional<Member> findMemberByEmailAndPassword(Email email, Password password);

    boolean existsByEmail(Email email);

//    List<Member> findAll();
//
//    Member save(Member member);
//
//    int deleteById(Long id);
//
//    Optional<Member> findById(Long id);

//    Optional<Member> findByEmailAndPassword(String email, String password);

//    boolean existsByEmail(String email);
}
