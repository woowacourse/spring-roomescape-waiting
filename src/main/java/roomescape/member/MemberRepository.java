package roomescape.member;

import java.util.List;

public interface MemberRepository {
    void saveMember(Member member);

    Member findByEmail(String email);
    List<Member> findAll();

    Boolean existsById(Long id);
    Boolean existsByEmail(String email);
}
