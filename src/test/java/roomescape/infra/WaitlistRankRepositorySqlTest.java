package roomescape.infra;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;
import roomescape.domain.Member;
import roomescape.domain.ReservationTime;
import roomescape.domain.Slot;
import roomescape.domain.Theme;
import roomescape.domain.Waitlist;
import roomescape.domain.WaitlistWithRank;
import roomescape.repository.SlotRepository;
import roomescape.repository.WaitlistRankRepository;

@DataJpaTest
@TestPropertySource(properties = {
    "spring.jpa.show-sql=false",
    "spring.jpa.properties.hibernate.show_sql=false",
    "spring.jpa.properties.hibernate.session_factory.statement_inspector=roomescape.infra.WaitlistRankRepositorySqlTest$SqlCollector"
})
class WaitlistRankRepositorySqlTest {

    @Autowired
    private WaitlistRankRepository waitlistRankRepository;

    @Autowired
    private SlotRepository slotRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        SqlCollector.clear();
    }

    @Test
    void JPQL로_내_대기의_순번을_계산하며_발생하는_SQL을_확인한다() {
        ReservationTime time = testEntityManager.persist(new ReservationTime(LocalTime.of(10, 0)));
        Theme theme = testEntityManager.persist(new Theme("방탈출", "설명", "image.png"));
        Slot slot = slotRepository.getOrCreate(Slot.of(LocalDate.now().plusDays(1), time, theme));

        Member brie = testEntityManager.persist(new Member("브리"));
        Member neo = testEntityManager.persist(new Member("네오"));
        Member pobi = testEntityManager.persist(new Member("포비"));

        testEntityManager.persist(new Waitlist(brie, slot, LocalDateTime.of(2026, 1, 1, 10, 0)));
        testEntityManager.persist(new Waitlist(neo, slot, LocalDateTime.of(2026, 1, 1, 10, 1)));
        testEntityManager.persist(new Waitlist(pobi, slot, LocalDateTime.of(2026, 1, 1, 10, 2)));

        entityManager.flush();
        entityManager.clear();
        SqlCollector.start();

        List<WaitlistWithRank> waitlistsWithRank = waitlistRankRepository.findByMemberNameWithRank("네오");

        SqlCollector.stop();

        assertThat(waitlistsWithRank)
            .extracting(WaitlistWithRank::waitingOrderAsInt)
            .containsExactly(2);
        assertThat(SqlCollector.sqls()).anySatisfy(sql -> assertThat(sql)
            .contains("select")
            .contains("from waitlist")
            .contains("count")
            .contains("slot_id"));
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
