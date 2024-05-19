package roomescape.member.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;
import roomescape.member.domain.Member;

public interface MemberRepository extends Repository<Member, Long> {

    Optional<Member> findMemberByEmailAndPassword_Password(String email, String password);

    Optional<Member> findMemberById(long id);

    List<Member> findAll();
}
