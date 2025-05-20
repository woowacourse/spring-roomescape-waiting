package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.constant.TestData.RESERVATION_COUNT;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.jdbc.Sql;
import roomescape.exception.ReservationException;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.member.domain.Password;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@DataJpaTest
@Sql("/data.sql")
class JdbcReservationRepositoryTest {

    @Autowired
    private TestEntityManager tm;

    @Autowired
    private ReservationRepository repository;

    private LocalDate date;
    private ReservationTime time1;
    private ReservationTime time2;
    private Theme theme1;
    private Theme theme2;
    private Member member1;
    private Member member2;

    @BeforeEach
    void setUp() {
        date = LocalDate.of(2025, 7, 1);
        time1 = ReservationTime.from(LocalTime.of(10, 0));
        time2 = ReservationTime.from(LocalTime.of(11, 0));
        theme1 = Theme.of("테마1", "설명1", "썸네일1");
        theme2 = Theme.of("테마2", "설명2", "썸네일2");
        member1 = Member.builder()
                .name("유저1")
                .email("user1@naver.com")
                .password(Password.createForMember("pwd123"))
                .role(MemberRole.MEMBER)
                .build();
        member2 = Member.builder()
                .name("유저2")
                .email("user2@naver.com")
                .password(Password.createForMember("pwd123"))
                .role(MemberRole.MEMBER)
                .build();
    }

    @Test
    void 모든_예약_조회() {
        // given
        tm.persistAndFlush(time1);
        tm.persistAndFlush(time2);
        tm.persistAndFlush(theme1);
        tm.persistAndFlush(theme2);
        tm.persistAndFlush(member1);
        tm.persistAndFlush(member2);

        Reservation reservation1 = Reservation.booked(date, time1, theme1, member1);
        Reservation reservation2 = Reservation.booked(date, time2, theme2, member2);
        tm.persistAndFlush(reservation1);
        tm.persistAndFlush(reservation2);

        tm.clear();

        // when
        List<Reservation> reservations = repository.findByCriteria(null, null, null, null);

        // then
        assertThat(reservations).hasSize(RESERVATION_COUNT + 2);
    }

    @Test
    void 특정_날짜로_예약_조회() {
        // given
        tm.persistAndFlush(time1);
        tm.persistAndFlush(theme1);
        tm.persistAndFlush(member1);

        LocalDate date1 = LocalDate.of(2999, 7, 1);
        LocalDate date2 = LocalDate.of(2999, 7, 2);
        LocalDate date3 = LocalDate.of(2999, 7, 3);

        tm.persistAndFlush(Reservation.booked(date1, time1, theme1, member1));
        tm.persistAndFlush(Reservation.booked(date2, time1, theme1, member1));
        tm.persistAndFlush(Reservation.booked(date3, time1, theme1, member1));

        tm.clear();

        // when
        List<Reservation> reservations = repository.findByCriteria(null, null, date2, null);

        // then
        SoftAssertions.assertSoftly(soft -> {
            assertThat(reservations).hasSize(2);
            assertThat(reservations.get(0).getDate()).isEqualTo(date2);
            assertThat(reservations.get(1).getDate()).isEqualTo(date3);
        });
    }

    @Test
    void 회원_ID로_예약_조회() {
        // given
        LocalDate date1 = LocalDate.of(2999, 7, 1);
        LocalDate date2 = LocalDate.of(2999, 7, 2);
        LocalDate date3 = LocalDate.of(2999, 7, 3);
        LocalDate date4 = LocalDate.of(2999, 7, 4);
        LocalDate date5 = LocalDate.of(2999, 7, 5);

        tm.persistAndFlush(time1);

        tm.persistAndFlush(time1);
        tm.persistAndFlush(theme1);
        tm.persistAndFlush(member1);
        tm.persistAndFlush(member2);

        tm.persistAndFlush(Reservation.booked(date1, time1, theme1, member1));
        tm.persistAndFlush(Reservation.booked(date2, time1, theme1, member1));
        tm.persistAndFlush(Reservation.booked(date3, time1, theme1, member1));
        tm.persistAndFlush(Reservation.booked(date4, time1, theme1, member2));
        tm.persistAndFlush(Reservation.booked(date5, time1, theme1, member2));

        tm.clear();

        // when
        List<Reservation> reservations = repository.findByCriteria(null, member1.getId(), null, null);

        // then
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(reservations).hasSize(3);
            soft.assertThat(reservations.get(0).getMember().getId()).isEqualTo(member1.getId());
            soft.assertThat(reservations.get(1).getMember().getId()).isEqualTo(member1.getId());
            soft.assertThat(reservations.get(2).getMember().getId()).isEqualTo(member1.getId());
        });
    }

    @Test
    void 예약_시간이_현재_이후면_객체가_정상_생성된다() {
        // given
        LocalDate today = LocalDate.now();
        LocalTime oneMinuteLater = LocalTime.now().plusMinutes(1);
        ReservationTime futureTime = ReservationTime.from(oneMinuteLater);

        tm.persistAndFlush(time1);
        tm.persistAndFlush(theme1);
        tm.persistAndFlush(member1);
        tm.persistAndFlush(futureTime);
        final Reservation booked = Reservation.booked(today, futureTime, theme1, member1);

        tm.clear();

        // when
        // then
        assertThatCode(() -> repository.save(booked)).doesNotThrowAnyException();
    }

    @Test
    void 예약_시간이_현재_이전이면_ReservationException이_발생한다() {
        // given
        Theme defaultTheme = Theme.of("테마", "설명", "썸네일");
        Member defaultMember = Member.builder()
                .name("김철수")
                .email("member@naver.com")
                .password(Password.createForMember("1234"))
                .role(MemberRole.MEMBER)
                .build();
        LocalDate today = LocalDate.now();
        LocalTime oneMinuteBefore = LocalTime.now().minusMinutes(1);
        ReservationTime pastTime = ReservationTime.from(oneMinuteBefore);
        final Reservation booked = Reservation.booked(today, pastTime, defaultTheme, defaultMember);
        // when

        // then
        assertThatThrownBy(() -> repository.save(booked))
                .isInstanceOf(ReservationException.class)
                .hasMessage("예약은 현재 시간 이후로 가능합니다.");
    }
}
