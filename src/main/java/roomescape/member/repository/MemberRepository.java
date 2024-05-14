package roomescape.member.repository;

import roomescape.member.model.Member;

import java.util.List;
import java.util.Optional;

public interface MemberRepository {

    List<Member> findAll();

    Optional<Member> findById(Long memberId);

    Optional<Member> findByEmail(String email);

    Member save(Member member);

    boolean existByEmail(String email);
}
