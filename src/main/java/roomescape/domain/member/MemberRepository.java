package roomescape.domain.member;

import java.util.List;
import java.util.Optional;
import roomescape.exception.InvalidMemberException;

public interface MemberRepository {
    Member save(Member member);

    Optional<Member> findByEmail(String email);

    default Member getByEmail(String email) {
        return findByEmail(email).orElseThrow(() -> new InvalidMemberException("이메일 또는 비밀번호가 잘못되었습니다."));
    }

    Optional<Member> findById(long id);

    default Member getById(long id) {
        return findById(id).orElseThrow(() -> new InvalidMemberException("존재하지 않는 회원입니다."));
    }

    boolean existsByEmail(String email);

    List<Member> findAll();
}
