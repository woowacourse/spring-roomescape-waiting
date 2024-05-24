package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import static roomescape.service.fixture.TestMemberFactory.createMember;
import static roomescape.service.fixture.TestReservationFactory.createAcceptReservationAtNow;
import static roomescape.service.fixture.TestReservationTimeFactory.createReservationTime;
import static roomescape.service.fixture.TestThemeFactory.createTheme;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import roomescape.model.Member;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.model.Theme;
import roomescape.service.dto.MemberReservation;

@DataJpaTest
@Sql("/init-data.sql")
class ReservationRepositoryTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ReservationRepository reservationRepository;

    @DisplayName("모든 예약을 조회한다.")
    @Test
    void should_get_all_reservations() {
        System.out.println(entityManager);

        ReservationTime time1 = saveReservationTime(createReservationTime(1L, "10:00"));
        ReservationTime time2 = saveReservationTime(createReservationTime(2L, "12:00"));
        Theme theme = saveTheme(createTheme(1L));
        Member member = saveMember(createMember(1L));
        saveReservation(createAcceptReservationAtNow(1L, time1, theme, member));
        saveReservation(createAcceptReservationAtNow(2L, time2, theme, member));

        List<Reservation> reservations = reservationRepository.findAll();
        assertThat(reservations).hasSize(2);
    }

    @DisplayName("특정 날짜와 테마에 해당하는 시간을 조회한다.")
    @Test
    void should_search_reservation_by_condition() {
        ReservationTime time1 = saveReservationTime(createReservationTime(1L, "10:00"));
        ReservationTime time2 = saveReservationTime(createReservationTime(2L, "12:00"));
        Theme theme = saveTheme(createTheme(1L));
        Member member = saveMember(createMember(1L));
        saveReservation(createAcceptReservationAtNow(1L, time1, theme, member));
        saveReservation(createAcceptReservationAtNow(2L, time2, theme, member));

        List<Reservation> reservations = reservationRepository.findAllByDateAndTheme(LocalDate.now(), theme);

        assertThat(reservations).hasSize(2);
    }

    @DisplayName("조회한 예약에 예약 시간이 존재한다.")
    @Test
    void should_get_reservation_times() {
        ReservationTime time1 = saveReservationTime(createReservationTime(1L, "10:00"));
        ReservationTime time2 = saveReservationTime(createReservationTime(2L, "12:00"));
        Theme theme = saveTheme(createTheme(1L));
        Member member = saveMember(createMember(1L));
        saveReservation(createAcceptReservationAtNow(1L, time1, theme, member));
        saveReservation(createAcceptReservationAtNow(2L, time2, theme, member));

        List<Reservation> reservations = reservationRepository.findAll();
        assertThat(reservations.get(0).getTime().getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @DisplayName("예약을 추가한다")
    @Test
    void should_add_reservation() {
        saveReservationTime(createReservationTime(1L, "12:00"));
        ReservationTime time2 = saveReservationTime(createReservationTime(2L, "12:00"));
        Theme theme = saveTheme(createTheme(1L));
        Member member = saveMember(createMember(1L));

        reservationRepository.save(createAcceptReservationAtNow(1L, time2, theme, member));

        assertThat(reservationRepository.count()).isEqualTo(1);
    }

    @DisplayName("예약을 삭제한다")
    @Test
    void should_delete_reservation() {
        ReservationTime time1 = saveReservationTime(createReservationTime(1L, "10:00"));
        ReservationTime time2 = saveReservationTime(createReservationTime(2L, "12:00"));
        Theme theme = saveTheme(createTheme(1L));
        Member member = saveMember(createMember(1L));
        saveReservation(createAcceptReservationAtNow(1L, time1, theme, member));
        saveReservation(createAcceptReservationAtNow(2L, time2, theme, member));

        reservationRepository.deleteById(1L);

        assertThat(reservationRepository.count()).isEqualTo(1);
    }

    @DisplayName("아이디에 해당하는 예약 개수를 반환한다.")
    @Test
    void should_return_true_when_id_exist() {
        ReservationTime time1 = saveReservationTime(createReservationTime(1L, "10:00"));
        ReservationTime time2 = saveReservationTime(createReservationTime(2L, "12:00"));
        Theme theme = saveTheme(createTheme(1L));
        Member member = saveMember(createMember(1L));
        saveReservation(createAcceptReservationAtNow(1L, time1, theme, member));
        saveReservation(createAcceptReservationAtNow(2L, time2, theme, member));

        long count = reservationRepository.countById(1L);
        assertThat(count).isEqualTo(1);
    }

    @DisplayName("예약시간에 해당하는 예약 개수를 반환한다.")
    @Test
    void should_return_reservation_count_when_give_time() {
        ReservationTime time1 = saveReservationTime(createReservationTime(1L, "10:00"));
        ReservationTime time2 = saveReservationTime(createReservationTime(2L, "12:00"));
        Theme theme = saveTheme(createTheme(1L));
        Member member = saveMember(createMember(1L));
        saveReservation(createAcceptReservationAtNow(1L, time1, theme, member));
        saveReservation(createAcceptReservationAtNow(2L, time2, theme, member));

        long count = reservationRepository.countByTime(time1);
        assertThat(count).isEqualTo(1);
    }

    @DisplayName("날짜, 시간, 테마에 해당하는 예약 개수를 반환한다.")
    @Test
    void should_return_reservation_count_when_give_date_and_time_and_theme() {
        ReservationTime time1 = saveReservationTime(createReservationTime(1L, "10:00"));
        ReservationTime time2 = saveReservationTime(createReservationTime(2L, "12:00"));
        Theme theme = saveTheme(createTheme(1L));
        Member member = saveMember(createMember(1L));
        saveReservation(createAcceptReservationAtNow(1L, time1, theme, member));
        saveReservation(createAcceptReservationAtNow(2L, time2, theme, member));

        long count = reservationRepository.countByDateAndTimeAndTheme(LocalDate.now(), time1, theme);
        assertThat(count).isEqualTo(1);
    }

    @DisplayName("사용자 아이디에 해당하는 예약을 반환한다.")
    @Test
    void should_return_member_reservations() {
        ReservationTime time1 = saveReservationTime(createReservationTime(1L, "10:00"));
        ReservationTime time2 = saveReservationTime(createReservationTime(2L, "12:00"));
        Theme theme = saveTheme(createTheme(1L));
        Member member = saveMember(createMember(1L));
        saveReservation(createAcceptReservationAtNow(1L, time1, theme, member));
        saveReservation(createAcceptReservationAtNow(2L, time2, theme, member));

        List<Reservation> reservations = reservationRepository.findAllByMember(member);
        assertThat(reservations).hasSize(2);
    }

    @DisplayName("날짜, 시간, 테마에 해당하는 예약 개수를 반환한다.")
    @Sql(scripts = {"/init-data.sql", "/member-reservation-test-data.sql"})
    @Test
    void should_return_member_reservation_when_give_member() {
        List<MemberReservation> memberReservation = reservationRepository.findMemberReservation(2L);

        assertSoftly(assertions -> {
            assertions.assertThat(memberReservation).hasSize(3);
            assertions.assertThat(memberReservation).extracting("order")
                    .containsExactlyInAnyOrder(0L, 2L, 4L);
        });
    }

    public Theme saveTheme(Theme theme) {
        entityManager.merge(theme);
        return theme;
    }

    public ReservationTime saveReservationTime(ReservationTime time) {
        entityManager.merge(time);
        return time;
    }

    public Reservation saveReservation(Reservation reservation) {
        entityManager.merge(reservation);
        return reservation;
    }

    public Member saveMember(Member member) {
        entityManager.merge(member);
        return member;
    }
}
