package roomescape.member.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    // TODO 보안 처리 고민 이메일 확인 후 엔티티를 통한 비밀번호 검증
    Optional<Member> findByEmailAndPassword_Password(String email, String Password);

    boolean existsByEmail(String email);

}
