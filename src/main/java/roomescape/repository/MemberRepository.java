package roomescape.repository;

import roomescape.domain.Member;

import java.util.List;
import java.util.Optional;

public interface MemberRepository {
    Optional<Member> findByEmailAndEncryptedPassword(String email, String encryptedPassword);

    Optional<Member> findById(long id);

    List<Member> findAll();

    Member save(Member member);
}
