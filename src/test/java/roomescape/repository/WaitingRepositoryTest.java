package roomescape.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import roomescape.model.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.model.Role.MEMBER;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Sql(scripts = "/test_data.sql")
class WaitingRepositoryTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private WaitingRepository waitingRepository;

    @DisplayName("예약 대기를 추가한다")
    @Test
    void should_add_waiting() {
        LocalDate day = LocalDate.of(2024, 5, 15);
        ReservationTime time1 = new ReservationTime(LocalTime.of(10, 0));
        ReservationTime time2 = new ReservationTime(LocalTime.of(11, 0));
        Theme theme = new Theme("무빈테마", "무빈테마설명", "무빈테마썸네일");
        Member member = new Member("무빈", MEMBER, "email@email.com", "password");

        entityManager.persist(time1);
        entityManager.persist(time2);
        entityManager.persist(theme);
        entityManager.persist(member);

        waitingRepository.save(new Waiting(day, time2, theme, member));

        assertThat(waitingRepository.count()).isEqualTo(1);
    }

    @DisplayName("예약 대기를 삭제한다")
    @Test
    void should_delete_waiting() {
        LocalDate day = LocalDate.of(2024, 5, 15);
        ReservationTime time1 = new ReservationTime(LocalTime.of(10, 0));
        ReservationTime time2 = new ReservationTime(LocalTime.of(11, 0));
        Theme theme = new Theme("무빈테마", "무빈테마설명", "무빈테마썸네일");
        Member member = new Member("무빈", MEMBER, "email@email.com", "password");

        entityManager.persist(time1);
        entityManager.persist(time2);
        entityManager.persist(theme);
        entityManager.persist(member);

        Waiting waiting1 = new Waiting(day, time1, theme, member);
        Waiting waiting2 = new Waiting(day, time2, theme, member);

        entityManager.persist(waiting1);
        entityManager.persist(waiting2);

        waitingRepository.deleteById(1L);

        assertThat(waitingRepository.count()).isEqualTo(1);
    }

    @DisplayName("아이디에 해당하는 예약 대기가 존재하면 참을 반환한다.")
    @Test
    void should_return_true_when_id_exist() {
        LocalDate day = LocalDate.of(2024, 5, 15);
        ReservationTime time1 = new ReservationTime(LocalTime.of(10, 0));
        ReservationTime time2 = new ReservationTime(LocalTime.of(11, 0));
        Theme theme = new Theme("무빈테마", "무빈테마설명", "무빈테마썸네일");
        Member member = new Member("무빈", MEMBER, "email@email.com", "password");

        entityManager.persist(time1);
        entityManager.persist(time2);
        entityManager.persist(theme);
        entityManager.persist(member);

        Waiting waiting1 = new Waiting(day, time1, theme, member);
        Waiting waiting2 = new Waiting(day, time2, theme, member);

        entityManager.persist(waiting1);
        entityManager.persist(waiting2);

        boolean exists = waitingRepository.existsById(1L);
        assertThat(exists).isTrue();
    }

    @DisplayName("사용자 아이디에 해당하는 예약 대기를 반환한다.")
    @Test
    void should_return_member_waiting() {
        LocalDate day = LocalDate.of(2024, 5, 15);
        ReservationTime time1 = new ReservationTime(LocalTime.of(10, 0));
        ReservationTime time2 = new ReservationTime(LocalTime.of(11, 0));
        Theme theme = new Theme("무빈테마", "무빈테마설명", "무빈테마썸네일");
        Member member = new Member("무빈", MEMBER, "email@email.com", "password");

        entityManager.persist(time1);
        entityManager.persist(time2);
        entityManager.persist(theme);
        entityManager.persist(member);

        Waiting waiting1 = new Waiting(day, time1, theme, member);
        Waiting waiting2 = new Waiting(day, time2, theme, member);

        entityManager.persist(waiting1);
        entityManager.persist(waiting2);

        List<WaitingWithRank> waiting = waitingRepository.findWaitingWithRankByMemberId(member.getId());
        assertThat(waiting).hasSize(2);
    }

    @DisplayName("날짜, 시간, 테마, 멤버에 해당하는 예약 대기가 존재하면 참을 반환한다.")
    @Test
    void should_return_true_when_date_and_time_and_theme_and_member_exist() {
        LocalDate day = LocalDate.of(2024, 5, 15);
        ReservationTime time1 = new ReservationTime(LocalTime.of(10, 0));
        ReservationTime time2 = new ReservationTime(LocalTime.of(11, 0));
        Theme theme = new Theme("무빈테마", "무빈테마설명", "무빈테마썸네일");
        Member member = new Member("무빈", MEMBER, "email@email.com", "password");

        entityManager.persist(time1);
        entityManager.persist(time2);
        entityManager.persist(theme);
        entityManager.persist(member);

        Waiting waiting1 = new Waiting(day, time1, theme, member);
        Waiting waiting2 = new Waiting(day, time2, theme, member);

        entityManager.persist(waiting1);
        entityManager.persist(waiting2);

        boolean exists = waitingRepository.existsWaitingByThemeAndDateAndTimeAndMember(theme, day, time1, member);
        assertThat(exists).isTrue();
    }
}
