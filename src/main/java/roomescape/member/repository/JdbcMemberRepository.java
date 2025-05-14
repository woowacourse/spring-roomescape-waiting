package roomescape.member.repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import roomescape.common.utils.JdbcUtils;
import roomescape.member.domain.Account;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberEmail;
import roomescape.member.domain.MemberName;
import roomescape.member.domain.Password;
import roomescape.member.domain.Role;

@RequiredArgsConstructor
public class JdbcMemberRepository implements MemberRepositoryInterface {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Member> memberRowMapper = (resultSet, rowNum) -> Member.withId(
            resultSet.getLong("id"),
            MemberName.from(resultSet.getString("name")),
            MemberEmail.from(resultSet.getString("email")),
            Role.from(resultSet.getString("role"))
    );

    private final RowMapper<Account> accountRowMapper = (resultSet, rowNum) -> Account.withoutId(
            Member.withId(
                    resultSet.getLong("id"),
                    MemberName.from(resultSet.getString("name")),
                    MemberEmail.from(resultSet.getString("email")),
                    Role.from(resultSet.getString("role"))
            ),
            Password.from(resultSet.getString("password"))
    );

    @Override
    public boolean existsByEmail(MemberEmail email) {
        final String sql = """
                select exists
                    (select 1 from member where email = ?)
                """;

        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, email.getValue()));
    }

    @Override
    public Optional<Member> findById(Long id) {
        final String sql = """
                SELECT id, name, email, role
                FROM member
                WHERE id = ?
                """;

        return JdbcUtils.queryForOptional(jdbcTemplate, sql, memberRowMapper, id);
    }

    @Override
    public Optional<Account> findAccountByEmail(MemberEmail email) {
        final String sql = """
                SELECT id, name, email, password, role
                FROM member
                WHERE email = ?
                """;

        return JdbcUtils.queryForOptional(jdbcTemplate, sql, accountRowMapper, email.getValue());
    }

    @Override
    public List<Member> findAll() {
        final String sql = """
                SELECT id, name, email, role
                FROM member
                """;

        return jdbcTemplate.query(sql, memberRowMapper).stream()
                .toList();
    }

    @Override
    public Member save(Account account) {
        final String sql = """
                INSERT INTO member (name, email, password, role) VALUES (?, ?, ?, ?)
                """;
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        final Member member = account.getMember();

        jdbcTemplate.update(connection -> {
            final PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, member.getName().getValue());
            preparedStatement.setString(2, member.getEmail().getValue());
            preparedStatement.setString(3, account.getPassword().getValue());
            preparedStatement.setString(4, member.getRole().name());

            return preparedStatement;
        }, keyHolder);

        final long generatedId = Objects.requireNonNull(keyHolder.getKey()).longValue();

        return Member.withId(
                generatedId,
                member.getName(),
                member.getEmail(),
                member.getRole()
        );
    }
}
