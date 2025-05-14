package roomescape.theme.repository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.common.exception.NotFoundException;
import roomescape.common.utils.ExecuteResult;
import roomescape.common.utils.JdbcUtils;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeDescription;
import roomescape.theme.domain.ThemeName;
import roomescape.theme.domain.ThemeThumbnail;

@Repository
@RequiredArgsConstructor
public class JdbcThemeRepository implements ThemeRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Theme> themeMapper = (resultSet, rowNum) ->
            Theme.withId(
                    resultSet.getLong("id"),
                    ThemeName.from(resultSet.getString("name")),
                    ThemeDescription.from(resultSet.getString("description")),
                    ThemeThumbnail.from(resultSet.getString("thumbnail"))
            );

    @Override
    public List<Theme> findAll() {
        final String sql = """
                select id, name, description, thumbnail from theme
                """;

        return jdbcTemplate.query(sql, themeMapper).stream()
                .toList();
    }

    @Override
    public Optional<Theme> findById(final Long id) {
        final String sql = """
                select id, name, description, thumbnail from theme where id = ?
                """;

        return JdbcUtils.queryForOptional(jdbcTemplate, sql, themeMapper, id);
    }

    @Override
    public Theme save(final Theme theme) {
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        final String sql = """
                insert into theme (name, description, thumbnail) values (?, ?, ?)
                """;

        jdbcTemplate.update(connection -> {
            final PreparedStatement preparedStatement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, theme.getName().getValue());
            preparedStatement.setString(2, theme.getDescription().getValue());
            preparedStatement.setString(3, theme.getThumbnail().getValue());

            return preparedStatement;
        }, keyHolder);

        final long generatedId = Objects.requireNonNull(keyHolder.getKey()).longValue();

        return Theme.withId(
                generatedId,
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnail()
        );
    }

    @Override
    public void deleteById(final Long id) {
        final String sql = "delete from theme where id = ?";

        ExecuteResult result = ExecuteResult.of(jdbcTemplate.update(sql, id));

        if (result == ExecuteResult.FAIL) {
            throw new NotFoundException("삭제할 테마를 찾을 수 없습니다.");
        }
    }
}
