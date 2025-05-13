package roomescape.repository;

import java.util.List;
import java.util.Optional;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberEmail;
import roomescape.domain.member.MemberEncodedPassword;
import roomescape.domain.member.MemberName;
import roomescape.domain.member.MemberRole;

public interface MemberRepository {

    Member save(
            final MemberEmail email,
            final MemberName name,
            final MemberEncodedPassword password,
            final MemberRole role
    );

    Optional<Member> findByEmail(final MemberEmail email);

    Optional<Member> findById(final Long id);

    List<Member> findAll();
}
