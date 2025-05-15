package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.constant.TestData.RESERVATION_COUNT;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.error.ReservationException;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@DataJpaTest
@Sql("/data.sql")
class JdbcReservationRepositoryTest {

    @Autowired
    private ReservationRepository repository;

    @Test
    void 모든_예약_조회() {
        // given
        LocalDate date = LocalDate.of(2025, 7, 1);
        ReservationTime time1 = new ReservationTime(1L, LocalTime.of(10, 0));
        ReservationTime time2 = new ReservationTime(2L, LocalTime.of(11, 0));
        Theme theme1 = new Theme(1L, "테마1", "설명1", "썸네일1");
        Theme theme2 = new Theme(2L, "테마2", "설명2", "썸네일2");
        Member member1 = new Member(1L, "유저1", "user1@naver.com", "pwd", MemberRole.MEMBER.name());
        Member member2 = new Member(2L, "유저2", "user2@naver.com", "pwd", MemberRole.MEMBER.name());

        Reservation reservation1 = Reservation.booked(date, time1, theme1, member1);
        Reservation reservation2 = Reservation.booked(date, time2, theme2, member2);
        repository.save(reservation1);
        repository.save(reservation2);

        // when
        List<Reservation> reservations = repository.findByCriteria(null, null, null, null);

        // then
        assertThat(reservations).hasSize(RESERVATION_COUNT + 2);
    }

    @Test
    void 특정_날짜로_예약_조회() {
        // given
        LocalDate date1 = LocalDate.of(2999, 7, 1);
        LocalDate date2 = LocalDate.of(2999, 7, 2);
        LocalDate date3 = LocalDate.of(2999, 7, 3);
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "테마1", "설명1", "썸네일1");
        Member member = new Member(1L, "유저1", "user1@naver.com", "pwd", MemberRole.MEMBER.name());

        repository.save(Reservation.booked(date1, time, theme, member));
        repository.save(Reservation.booked(date2, time, theme, member));
        repository.save(Reservation.booked(date3, time, theme, member));

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
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "테마1", "설명1", "썸네일1");
        Member member1 = new Member(1L, "유저1", "user1@naver.com", "pwd", MemberRole.MEMBER.name());
        Member member2 = new Member(2L, "유저2", "user2@naver.com", "pwd", MemberRole.MEMBER.name());

        repository.save(Reservation.booked(date1, time, theme, member1));
        repository.save(Reservation.booked(date2, time, theme, member1));
        repository.save(Reservation.booked(date3, time, theme, member1));
        repository.save(Reservation.booked(date4, time, theme, member2));
        repository.save(Reservation.booked(date5, time, theme, member2));

        // when
        List<Reservation> reservations = repository.findByCriteria(null, member1.getId(), null, null);

        // then
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(reservations).hasSize(RESERVATION_COUNT + 2);
            soft.assertThat(reservations.get(0).getMember().getId()).isEqualTo(member1.getId());
            soft.assertThat(reservations.get(1).getMember().getId()).isEqualTo(member1.getId());
            soft.assertThat(reservations.get(2).getMember().getId()).isEqualTo(member1.getId());
        });
    }

    @Test
    void 예약_시간이_현재_이후면_객체가_정상_생성된다() {
        // given
        Theme defaultTheme = new Theme(1L, "테마", "설명", "썸네일");
        Member defaultMember = new Member(1L, "member", "member@naver.com", "1234", MemberRole.MEMBER.name());
        LocalDate today = LocalDate.now();
        LocalTime oneMinuteLater = LocalTime.now().plusMinutes(1);
        ReservationTime futureTime = new ReservationTime(1L, oneMinuteLater);
        final Reservation booked = Reservation.booked(today, futureTime, defaultTheme, defaultMember);
        // when
        // then
        assertThatCode(() -> repository.save(booked)).doesNotThrowAnyException();
    }

    @Test
    void 예약_시간이_현재_이전이면_ReservationException이_발생한다() {
        // given
        Theme defaultTheme = new Theme(1L, "테마", "설명", "썸네일");
        Member defaultMember = new Member(1L, "member", "member@naver.com", "1234", MemberRole.MEMBER.name());
        LocalDate today = LocalDate.now();
        LocalTime oneMinuteBefore = LocalTime.now().minusMinutes(1);
        ReservationTime pastTime = new ReservationTime(1L, oneMinuteBefore);
        final Reservation booked = Reservation.booked(today, pastTime, defaultTheme, defaultMember);
        // when

        // then
        assertThatThrownBy(() -> repository.save(booked))
                .isInstanceOf(ReservationException.class)
                .hasMessage("예약은 현재 시간 이후로 가능합니다.");
    }
}
