package roomescape.member.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.global.auth.dto.UserInfo;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.member.entity.MemberEntity;

@Repository
public class JdbcMemberRepository implements MemberRepository {

    private static final RowMapper<MemberEntity> MEMBER_ENTITY_ROW_MAPPER = (resultSet, rowNum) -> new MemberEntity(
            resultSet.getLong("id"),
            resultSet.getString("name"),
            resultSet.getString("email"),
            null,
            resultSet.getString("role")
    );
    private static final RowMapper<UserInfo> USER_INFO_ROW_MAPPER = (resultSet, rowNum) -> new UserInfo(
            resultSet.getLong("id"),
            resultSet.getString("name"),
            MemberRole.from(resultSet.getString("role"))
    );

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcMemberRepository(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("member")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public boolean existsByEmailAndPassword(final String email, final String password) {
        try {
            return Boolean.TRUE.equals(
                    jdbcTemplate.queryForObject("SELECT EXISTS (SELECT 1 FROM member WHERE (email, password) = (?, ?))",
                            Boolean.class,
                            email,
                            password
                    ));
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    @Override
    public Optional<UserInfo> findMemberByEmailAndPassword(final String email, final String password) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT id, name, role FROM member WHERE (email, password) = (?, ?)",
                    USER_INFO_ROW_MAPPER, email, password));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean existsByIdAndMemberRole(final Long id, final MemberRole memberRole) {
        try {
            return Boolean.TRUE.equals(
                    jdbcTemplate.queryForObject("SELECT EXISTS (SELECT 1 FROM member WHERE (id, role) = (?, ?))",
                            Boolean.class,
                            id,
                            memberRole.name()
                    ));
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    @Override
    public boolean existsById(final Long id) {
        try {
            return Boolean.TRUE.equals(
                    jdbcTemplate.queryForObject("SELECT EXISTS (SELECT 1 FROM member WHERE id = ?)",
                            Boolean.class,
                            id
                    ));
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    @Override
    public Optional<Member> findUserById(final Long id) {
        try {
            MemberEntity memberEntity = jdbcTemplate.queryForObject(
                    "SELECT id, name, email, password, role FROM member WHERE (id, role) = (?, ?)",
                    MEMBER_ENTITY_ROW_MAPPER, id, MemberRole.USER.name());
            return Optional.ofNullable(memberEntity)
                    .map(MemberEntity::toMember);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Member save(final Member member) {
        Long generatedId = simpleJdbcInsert.executeAndReturnKey(
                Map.of("name", member.getName(), "email", member.getEmail(), "password",
                        member.getPassword(), "role", member.getMemberRole().name())
        ).longValue();
        return Member.of(generatedId, member.getName(), member.getEmail(), member.getPassword(),
                member.getMemberRole());
    }

    @Override
    public List<UserInfo> findAllUsers() {
        final String sql = "SELECT id, name, role FROM member WHERE role = ?";
        return jdbcTemplate.query(sql, USER_INFO_ROW_MAPPER,
                MemberRole.USER.name());
    }
}
