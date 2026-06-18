package roomescape.jpaStudy;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.transaction.TestTransaction;
import roomescape.domain.Theme;
import roomescape.repository.ThemeRepository;

@DataJpaTest
@TestPropertySource(properties = {
    "spring.jpa.show-sql=false",
    "spring.jpa.properties.hibernate.show_sql=false",
    "spring.jpa.properties.hibernate.session_factory.statement_inspector=roomescape.jpaStudy.PersistenceTest$SqlCollector"
})
public class PersistenceTest {

    private final ThemeRepository themeRepository;
    private final EntityManager entityManager;

    @Autowired
    public PersistenceTest(ThemeRepository themeRepository, EntityManager entityManager) {
        this.themeRepository = themeRepository;
        this.entityManager = entityManager;
    }

    @BeforeEach
    void setUp() {
        SqlCollector.clear();
    }

    @Test
    void dirty_checking을_관찰한다() {
        Theme theme = themeRepository.save(
            new Theme("기존 이름", "설명", "image.png")
        );

        entityManager.flush();
        entityManager.clear();

        Theme foundTheme = themeRepository.findById(theme.getId()).orElseThrow();

        SqlCollector.start();
        foundTheme.changeName("변경된 이름");

        entityManager.flush();
        SqlCollector.stop();

        assertThat(SqlCollector.sqls()).anySatisfy(sql -> assertThat(sql)
            .containsIgnoringCase("update theme"));
    }

    @Test
    void 일차캐시() {
        Long id = themeRepository.save(
            new Theme("이름", "설명", "썸네일 URL")
        ).getId();

        entityManager.flush();
        entityManager.clear();

        SqlCollector.start();

        Theme first = themeRepository.findById(id).orElseThrow();
        Theme second = themeRepository.findById(id).orElseThrow();

        SqlCollector.stop();

        assertThat(first).isSameAs(second);
    }

    @Test
    void identity_전략에서는_save_시점에_insert가_나갈_수_있다() {
        SqlCollector.start();

        Theme theme = themeRepository.save(new Theme("공포", "설명", "image.png"));

        System.out.println("id = " + theme.getId());

        SqlCollector.stop();
    }

    @Test
    void 명시적_flush_호출_시점에_변경사항이_DB와_동기화된다() {
        Long themeId = saveThemeAndClear();
        Theme found = themeRepository.findById(themeId).orElseThrow();

        SqlCollector.start();
        found.changeName("명시적 flush");

        assertThat(SqlCollector.sqls()).noneMatch(PersistenceTest::isUpdateThemeSql);

        entityManager.flush();
        SqlCollector.stop();

        assertThat(SqlCollector.sqls()).anyMatch(PersistenceTest::isUpdateThemeSql);
    }

    @Test
    void JPQL_실행_직전에_flush가_먼저_발생한다() {
        Long themeId = saveThemeAndClear();
        Theme found = themeRepository.findById(themeId).orElseThrow();

        SqlCollector.start();
        found.changeName("JPQL 직전 flush");

        entityManager.createQuery("select t from Theme t where t.id = :id", Theme.class)
            .setParameter("id", themeId)
            .getSingleResult();
        SqlCollector.stop();

        List<String> sqls = SqlCollector.sqls();
        int updateIndex = indexOfSql(sqls, "update theme");
        int selectIndex = indexOfSql(sqls, "select");

        assertThat(updateIndex).isNotNegative();
        assertThat(selectIndex).isNotNegative();
        assertThat(updateIndex).isLessThan(selectIndex);
    }

    @Test
    void 트랜잭션_종료_시점에_flush가_발생한다() {
        Long themeId = saveThemeAndClear();
        Theme found = themeRepository.findById(themeId).orElseThrow();

        SqlCollector.start();
        found.changeName("트랜잭션 종료 flush");

        assertThat(SqlCollector.sqls()).noneMatch(PersistenceTest::isUpdateThemeSql);

        TestTransaction.flagForCommit();
        TestTransaction.end();
        SqlCollector.stop();

        assertThat(SqlCollector.sqls()).anyMatch(PersistenceTest::isUpdateThemeSql);
    }

    private Long saveThemeAndClear() {
        Theme theme = themeRepository.save(new Theme("공포", "설명", "image.png"));
        entityManager.flush();
        entityManager.clear();
        SqlCollector.clear();

        return theme.getId();
    }

    private static boolean isUpdateThemeSql(String sql) {
        return sql.toLowerCase().contains("update theme");
    }

    private static int indexOfSql(List<String> sqls, String keyword) {
        for (int i = 0; i < sqls.size(); i++) {
            if (sqls.get(i).toLowerCase().contains(keyword)) {
                return i;
            }
        }

        return -1;
    }

    public static class SqlCollector implements StatementInspector {

        private static final List<String> SQLS = new ArrayList<>();
        private static boolean recording = false;

        @Override
        public String inspect(String sql) {
            if (recording) {
                SQLS.add(sql);
                System.out.println("[확인 대상 SQL]");
                System.out.println(sql);
            }
            return sql;
        }

        static void start() {
            SQLS.clear();
            recording = true;
        }

        static void stop() {
            recording = false;
        }

        static void clear() {
            SQLS.clear();
            recording = false;
        }

        static List<String> sqls() {
            return List.copyOf(SQLS);
        }
    }
}
