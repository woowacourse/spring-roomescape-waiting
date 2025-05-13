package roomescape.member.domain;

import java.util.List;

public interface MemberRepository {

    Long save(Member member);

    Member findById(Long id);

    Member findByEmail(String email);

    boolean isExistsByEmail(String email);

    List<Member> findAll();
}
