package roomescape.member.repository;

import java.util.List;
import java.util.Optional;
import roomescape.member.domain.Email;
import roomescape.member.domain.LoginMember;
import roomescape.member.domain.Password;

public interface MemberRepository {

    Optional<LoginMember> findById(Long id);

    Optional<LoginMember> findByEmail(Email email);

    List<LoginMember> findAll();

    boolean isCorrectPassword(Email email, Password password);

    void delete(Long id);
}
