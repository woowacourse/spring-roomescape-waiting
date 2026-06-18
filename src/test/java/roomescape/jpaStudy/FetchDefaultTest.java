package roomescape.jpaStudy;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.Hibernate;
import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@EntityScan(basePackages = {"roomescape.domain", "roomescape.jpaStudy"})
@TestPropertySource(properties = {
    "spring.jpa.show-sql=false",
    "spring.jpa.properties.hibernate.show_sql=false",
    "spring.jpa.properties.hibernate.session_factory.statement_inspector=roomescape.jpaStudy.FetchDefaultTest$SqlCollector"
})
class FetchDefaultTest {

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        SqlCollector.clear();
    }

    @Test
    void ManyToOne은_fetch_옵션을_생략하면_EAGER로_조회된다() {
        FetchDefaultTheme theme = new FetchDefaultTheme("공포");
        entityManager.persist(theme);
        FetchDefaultReservation reservation = new FetchDefaultReservation("브라운", theme);
        entityManager.persist(reservation);
        entityManager.flush();
        entityManager.clear();

        SqlCollector.start();
        FetchDefaultReservation found = entityManager.find(FetchDefaultReservation.class, reservation.getId());
        SqlCollector.stop();

        assertThat(Hibernate.isInitialized(found.getTheme())).isTrue();
        assertThat(SqlCollector.sqls()).anyMatch(sql -> sql.contains("fetch_default_reservation"));
        assertThat(SqlCollector.sqls()).anyMatch(sql -> sql.contains("fetch_default_theme"));
    }

    @Test
    void OneToMany는_fetch_옵션을_생략하면_LAZY로_조회된다() {
        FetchDefaultTheme theme = new FetchDefaultTheme("공포");
        entityManager.persist(theme);
        entityManager.persist(new FetchDefaultReservation("브라운", theme));
        entityManager.persist(new FetchDefaultReservation("제이슨", theme));
        entityManager.flush();
        entityManager.clear();

        FetchDefaultTheme found = entityManager.find(FetchDefaultTheme.class, theme.getId());

        SqlCollector.start();
        assertThat(Hibernate.isInitialized(found.getReservations())).isFalse();

        assertThat(found.getReservations()).hasSize(2);
        SqlCollector.stop();

        assertThat(Hibernate.isInitialized(found.getReservations())).isTrue();
        assertThat(SqlCollector.sqls()).anySatisfy(sql -> assertThat(sql)
            .contains("fetch_default_reservation")
            .contains("theme_id"));
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

@Entity
@Table(name = "fetch_default_theme")
class FetchDefaultTheme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @OneToMany(mappedBy = "theme")
    private List<FetchDefaultReservation> reservations = new ArrayList<>();

    protected FetchDefaultTheme() {
    }

    FetchDefaultTheme(String name) {
        this.name = name;
    }

    Long getId() {
        return id;
    }

    List<FetchDefaultReservation> getReservations() {
        return reservations;
    }
}

@Entity
@Table(name = "fetch_default_reservation")
class FetchDefaultReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @ManyToOne
    private FetchDefaultTheme theme;

    protected FetchDefaultReservation() {
    }

    FetchDefaultReservation(String name, FetchDefaultTheme theme) {
        this.name = name;
        this.theme = theme;
    }

    Long getId() {
        return id;
    }

    FetchDefaultTheme getTheme() {
        return theme;
    }
}
