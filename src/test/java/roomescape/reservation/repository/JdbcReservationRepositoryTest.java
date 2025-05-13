package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

class JdbcReservationRepositoryTest {

    private static EmbeddedDatabase db;
    private JdbcReservationRepository repository;

    @BeforeAll
    static void initDatabase() {
        db = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .addScript("classpath:schema.sql")
                .addScript("classpath:data.sql")
                .build();
    }

    @BeforeEach
    void setUp() {
        repository = new JdbcReservationRepository(db);
    }

    @AfterEach
    void cleanUp() {
        new JdbcTemplate(db).execute("DELETE FROM reservation");
    }

    @AfterAll
    static void shutdownDatabase() {
        db.shutdown();
    }

    @Test
    void 예약이_올바르게_생성된다() {
        // given
        LocalDate date = LocalDate.of(2025, 7, 1);
        LocalTime time = LocalTime.of(10, 0);
        Theme theme1 = new Theme(1L, "테마1", "설명1", "썸네일1");
        Member member1 = new Member(1L, "이름", "member@naver.com", "1234", MemberRole.MEMBER.name());
        Reservation reservation = new Reservation(
                null,
                date,
                new ReservationTime(1L, time),
                theme1,
                member1
        );

        // when
        Reservation saved = repository.save(reservation);

        // then
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void 기존에_날짜와_시간이_같은_예약이_있는지_확인() {
        // given
        LocalDate existedDate = LocalDate.of(2025, 7, 1);
        LocalTime existedTime = LocalTime.of(10, 0);
        Theme theme1 = new Theme(1L, "테마1", "설명1", "썸네일1");
        Member member1 = new Member(1L, "이름", "member@naver.com", "1234", MemberRole.MEMBER.name());
        Reservation reservation = new Reservation(
                null,
                existedDate,
                new ReservationTime(1L, existedTime),
                theme1,
                member1
        );
        repository.save(reservation);

        LocalDate otherDate = LocalDate.of(2026, 1, 1);
        LocalTime otherTime = LocalTime.of(14, 1);

        // then
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(repository.existsByDateAndTimeAndTheme(existedDate, existedTime, 1L))
                    .isTrue();
            soft.assertThat(repository.existsByDateAndTimeAndTheme(otherDate, otherTime, 1L))
                    .isFalse();
        });
    }

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

        Reservation reservation1 = new Reservation(
                null,
                date,
                time1,
                theme1,
                member1
        );
        Reservation reservation2 = new Reservation(
                null,
                date,
                time2,
                theme2,
                member2
        );
        repository.save(reservation1);
        repository.save(reservation2);

        // when
        List<Reservation> reservations = repository.findByCriteria(null, null, null, null);

        // then
        assertThat(reservations).hasSize(2);
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

        repository.save(new Reservation(null, date1, time, theme, member));
        repository.save(new Reservation(null, date2, time, theme, member));
        repository.save(new Reservation(null, date3, time, theme, member));

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

        repository.save(new Reservation(null, date1, time, theme, member1));
        repository.save(new Reservation(null, date2, time, theme, member1));
        repository.save(new Reservation(null, date3, time, theme, member1));
        repository.save(new Reservation(null, date4, time, theme, member2));
        repository.save(new Reservation(null, date5, time, theme, member2));

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
    void 예약에_예약_시간이_존재하는지_확인() {
        // given
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme1 = new Theme(1L, "테마1", "설명1", "썸네일1");
        Member member1 = new Member(1L, "테스트유저", "test@naver.com", "pwd", MemberRole.MEMBER.name());
        Reservation reservation = new Reservation(
                null,
                LocalDate.of(2999, 7, 1),
                reservationTime,
                theme1,
                member1
        );
        repository.save(reservation);

        // when
        boolean exists = repository.existsByReservationTimeId(reservationTime.getId());

        // then
        assertThat(exists).isTrue();
    }
}
