package roomescape.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.sql.init.SqlDataSourceScriptDatabaseInitializer;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.sql.init.DatabaseInitializationSettings;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.Member;
import roomescape.domain.MemberRepository;
import roomescape.domain.MemberRole;
import roomescape.persistence.query.CreateMemberQuery;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class JdbcMemberRepositoryTest {

    private static final DataSource TEST_DATASOURCE = DataSourceBuilder.create()
            .driverClassName("org.h2.Driver")
            .url("jdbc:h2:mem:database-test")
            .username("sa")
            .build();

    private final JdbcTemplate jdbcTemplate = new JdbcTemplate(TEST_DATASOURCE);
    private final MemberRepository memberRepository = new JdbcMemberRepository(jdbcTemplate);

    @BeforeEach
    void setUp() {
        DatabaseInitializationSettings settings = new DatabaseInitializationSettings();
        settings.setSchemaLocations(List.of("classpath:schema.sql"));
        SqlDataSourceScriptDatabaseInitializer sqlDataSourceScriptDatabaseInitializer =
                new SqlDataSourceScriptDatabaseInitializer(TEST_DATASOURCE, settings);
        sqlDataSourceScriptDatabaseInitializer.initializeDatabase();
    }

    @Test
    void 이메일로_멤버를_찾을_수_있다() {
        //given
        jdbcTemplate.update("INSERT INTO member (name, role, email, password) VALUES ('Eve', 'USER', 'eve@example.com', 'password')");

        //when
        Optional<Member> member = memberRepository.findByEmail("eve@example.com");

        //then
        assertThat(member).hasValue(
                new Member(1L, "Eve", MemberRole.USER, "eve@example.com", "password")
        );
    }

    @Test
    void id로_멤버를_찾을_수_있다() {
        //given
        jdbcTemplate.update("INSERT INTO member (name, role, email, password) VALUES ('Eve', 'USER', 'eve@example.com', 'password')");

        //when
        Optional<Member> member = memberRepository.findById(1L);

        //then
        assertThat(member).hasValue(
                new Member(1L, "Eve", MemberRole.USER, "eve@example.com", "password")
        );
    }

    @Test
    void 멤버를_생성할_수_있다() {
        //given
        CreateMemberQuery createMemberQuery = new CreateMemberQuery("Eve", MemberRole.USER, "eve@example.com", "password");

        //when
        Long id = memberRepository.create(createMemberQuery);

        //then
        Optional<Member> createdMember = memberRepository.findById(id);
        assertAll(
                () -> assertThat(createdMember).isPresent(),
                () -> assertThat(createdMember.get().getName()).isEqualTo("Eve")
        );
    }
}