package roomescape.unit.fake;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberEmail;
import roomescape.domain.member.MemberEncodedPassword;
import roomescape.domain.member.MemberName;
import roomescape.domain.member.MemberRole;
import roomescape.repository.MemberRepository;

public class FakeMemberRepository implements MemberRepository {
    private final List<Member> values = new ArrayList();
    private final AtomicLong increment = new AtomicLong(1);

    @Override
    public Member save(
            final MemberEmail email,
            final MemberName name,
            final MemberEncodedPassword password,
            final MemberRole role
    ) {
        Member member = new Member(increment.getAndIncrement(), name, email, password, role);
        values.add(member);
        return member;
    }

    @Override
    public Optional<Member> findByEmail(final MemberEmail email) {
        return values.stream().filter(member -> member.getEmail().equals(email)).findFirst();
    }

    @Override
    public Optional<Member> findById(final Long id) {
        return values.stream().filter(member -> member.getId().equals(id)).findFirst();
    }

    @Override
    public List<Member> findAll() {
        return Collections.unmodifiableList(values);
    }
}
