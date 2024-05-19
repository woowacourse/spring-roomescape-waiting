package roomescape.repository;

import java.util.List;
import java.util.Optional;
import roomescape.domain.Member;

public interface MemberRepository {
    Optional<Member> findByEmailAndEncryptedPassword(String email, String encryptedPassword);

    Optional<Member> findById(long id);

    List<Member> findAll();
}
