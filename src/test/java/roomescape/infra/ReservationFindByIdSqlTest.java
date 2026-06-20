package roomescape.infra;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
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
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Slot;
import roomescape.domain.Theme;
import roomescape.repository.ReservationRepository;
import roomescape.repository.SlotRepository;

@DataJpaTest
@TestPropertySource(properties = {
    "spring.jpa.show-sql=false",
    "spring.jpa.properties.hibernate.show_sql=false",
    "spring.jpa.properties.hibernate.session_factory.statement_inspector=roomescape.infra.ReservationFindByIdSqlTest$SqlCollector"
})
class ReservationFindByIdSqlTest {

    @Autowired
    private ReservationRepository reservationRepository;

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
    void findById로_조회한_예약의_시간을_꺼낼_때_발생하는_SQL을_확인한다() {
        ReservationTime time = testEntityManager.persist(new ReservationTime(LocalTime.of(10, 0)));
        Theme theme = testEntityManager.persist(new Theme("방탈출", "설명", "image.png"));
        Member member = testEntityManager.persist(new Member("브라운"));
        Slot slot = slotRepository.getOrCreate(Slot.of(LocalDate.now().plusDays(1), time, theme));
        Reservation reservation = reservationRepository.save(new Reservation(member, slot));

        entityManager.flush();
        entityManager.clear();
        SqlCollector.start();

        LocalTime startAt = reservationRepository.findById(reservation.getId())
            .orElseThrow()
            .getTime()
            .getStartAt();

        SqlCollector.stop();

        assertThat(startAt).isEqualTo(LocalTime.of(10, 0));
        assertThat(SqlCollector.sqls()).anySatisfy(sql -> assertThat(sql)
            .contains("from reservation")
            .contains("join slot")
            .contains("join reservation_time"));
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
