package roomescape.repository;

import java.util.List;
import java.util.Optional;
import roomescape.domain.member.Member;

public interface MemberRepository {

    List<Member> findAll();

    Optional<Member> findById(Long id);

    Optional<Member> findByEmail(String email);
}
