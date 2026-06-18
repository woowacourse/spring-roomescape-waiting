package roomescape.jpaStudy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.Hibernate;
import org.hibernate.LazyInitializationException;
import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.transaction.TestTransaction;
import roomescape.domain.ReservationTime;
import roomescape.domain.Slot;
import roomescape.domain.Theme;

@DataJpaTest
@TestPropertySource(properties = {
    "spring.jpa.show-sql=false",
    "spring.jpa.properties.hibernate.show_sql=false",
    "spring.jpa.properties.hibernate.session_factory.statement_inspector=roomescape.jpaStudy.LazyInitializationExceptionTest$SqlCollector"
})
class LazyInitializationExceptionTest {

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        SqlCollector.clear();
    }

    @Test
    void 트랜잭션_밖에서_LAZY_필드에_접근하면_LazyInitializationException이_발생한다() {
        ReservationTime time = new ReservationTime(LocalTime.of(10, 0));
        Theme theme = new Theme("공포", "설명", "image.png");
        entityManager.persist(time);
        entityManager.persist(theme);

        Slot slot = Slot.of(LocalDate.now().plusDays(1), time, theme);
        entityManager.persist(slot);
        entityManager.flush();
        entityManager.clear();

        SqlCollector.start();
        Slot found = entityManager.find(Slot.class, slot.getId());
        SqlCollector.stop();

        assertThat(Hibernate.isInitialized(found.getTime())).isFalse();
        assertThat(SqlCollector.sqls()).anySatisfy(sql -> assertThat(sql)
            .contains("from slot")
            .doesNotContain("join reservation_time"));

        TestTransaction.end();

        assertThatThrownBy(() -> found.getTime().getStartAt())
            .isInstanceOf(LazyInitializationException.class);
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
