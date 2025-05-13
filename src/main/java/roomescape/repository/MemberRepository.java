package roomescape.repository;

import java.util.List;
import java.util.Optional;
import roomescape.domain.LoginMember;
import roomescape.domain.RegistrationDetails;

public interface MemberRepository {

    void save(final RegistrationDetails registrationDetails);

    Optional<LoginMember> findByEmail(final String email);

    Optional<LoginMember> findByEmailAndPassword(final String email, final String password);

    Optional<LoginMember> findById(final long id);

    List<LoginMember> findAll();
}
