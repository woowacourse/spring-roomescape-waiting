package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;
import roomescape.repository.rowmapper.MemberRowMapper;
import roomescape.repository.rowmapper.ReservationRowMapper;
import roomescape.repository.rowmapper.ReservationTimeRowMapper;
import roomescape.repository.rowmapper.ThemeRowMapper;

@TestExecutionListeners(value = {
        DatabaseCleanupListener.class,
        DependencyInjectionTestExecutionListener.class
})
@JdbcTest
class JdbcReservationTimeRepositoryTest {

    @Autowired
    private DataSource dataSource;
    private JdbcReservationTimeRepository reservationTimeRepository;
    private JdbcThemeRepository themeRepository;
    private JdbcReservationRepository reservationRepository;
    private JdbcMemberRepository memberRepository;

    private final String startAt1 = "13:00";
    private final String startAt2 = "15:00";
    private final String startAt3 = "17:00";

    private final ReservationTime time1 = new ReservationTime(null, startAt1);
    private final ReservationTime time2 = new ReservationTime(null, startAt2);
    private final ReservationTime time3 = new ReservationTime(null, startAt3);

    private final Theme theme1 = new Theme(null, "공포", "난이도 1", "hi.jpg");
    private final Theme theme2 = new Theme(null, "우테코", "난이도 2", "hi.jpg");

    private final Member member1 = new Member(null, "t1@t1.com", "123", "러너덕", "MEMBER");
    private final Member member2 = new Member(null, "t2@t2.com", "124", "재즈", "MEMBER");
    private final Member member3 = new Member(null, "t3@t3.com", "125", "재즈덕", "MEMBER");

    @BeforeEach
    void setUp() {
        reservationTimeRepository = new JdbcReservationTimeRepository(dataSource, new ReservationTimeRowMapper());
        themeRepository = new JdbcThemeRepository(dataSource, new ThemeRowMapper());
        reservationRepository = new JdbcReservationRepository(dataSource, new ReservationRowMapper());
        memberRepository = new JdbcMemberRepository(dataSource, new MemberRowMapper());
    }

    @DisplayName("저장된 모든 예약 시간 정보를 가져온다.")
    @Test
    void find_all_reservation_times() {
        ReservationTime savedTime1 = reservationTimeRepository.insertReservationTime(time1);
        ReservationTime savedTime2 = reservationTimeRepository.insertReservationTime(time2);
        ReservationTime savedTime3 = reservationTimeRepository.insertReservationTime(time3);

        List<ReservationTime> allReservationTimes = reservationTimeRepository.findAllReservationTimes();

        assertAll(
                () -> assertThat(allReservationTimes.get(0).getStartAt()).isEqualTo(savedTime1.getStartAt()),
                () -> assertThat(allReservationTimes.get(1).getStartAt()).isEqualTo(savedTime2.getStartAt()),
                () -> assertThat(allReservationTimes.get(2).getStartAt()).isEqualTo(savedTime3.getStartAt()),
                () -> assertThat(allReservationTimes.size()).isEqualTo(3)
        );
    }

    @DisplayName("예약 시간을 저장한다.")
    @Test
    void save_reservation_time() {
        ReservationTime savedTime = reservationTimeRepository.insertReservationTime(time1);

        assertAll(
                () -> assertThat(savedTime.getId()).isEqualTo(1),
                () -> assertThat(savedTime.getStartAt()).isEqualTo(time1.getStartAt())
        );
    }

    @Test
    @DisplayName("예약 시간을 id로 삭제한다.")
    void delete_reservation_time_by_id() {
        reservationTimeRepository.insertReservationTime(time1);
        int beforeSize = reservationTimeRepository.findAllReservationTimes().size();

        reservationTimeRepository.deleteReservationTimeById(1L);
        int afterSize = reservationTimeRepository.findAllReservationTimes().size();

        assertAll(
                () -> assertThat(beforeSize).isEqualTo(1),
                () -> assertThat(afterSize).isEqualTo(0)
        );
    }

    @Test
    @DisplayName("시간 존재 여부를 시작 시간으로 판단한다.")
    void is_time_exists_by_start_time() {
        ReservationTime savedTime1 = reservationTimeRepository.insertReservationTime(time1);

        boolean existTime = reservationTimeRepository.isTimeExistsByStartTime(savedTime1.getStartAt().toString());
        boolean notExistTime = reservationTimeRepository.isTimeExistsByStartTime("15:00");

        assertAll(
                () -> assertThat(existTime).isTrue(),
                () -> assertThat(notExistTime).isFalse()
        );
    }

    @Test
    @DisplayName("시간 존재 여부를 id로 판단한다.")
    void is_time_exists_by_time_id() {
        ReservationTime savedTime1 = reservationTimeRepository.insertReservationTime(time1);

        boolean existId = reservationTimeRepository.isTimeExistsByTimeId(savedTime1.getId());
        boolean notExistId = reservationTimeRepository.isTimeExistsByTimeId(2L);

        assertAll(
                () -> assertThat(existId).isTrue(),
                () -> assertThat(notExistId).isFalse()
        );
    }

    @Test
    @DisplayName("id로 시간을 조회한다.")
    void find_reservation_time_by_id() {
        ReservationTime savedTime1 = reservationTimeRepository.insertReservationTime(time1);

        ReservationTime findTime1 = reservationTimeRepository.findReservationTimeById(savedTime1.getId());

        assertAll(
                () -> assertThat(findTime1.getId()).isEqualTo(savedTime1.getId()),
                () -> assertThat(findTime1.getStartAt()).isEqualTo(savedTime1.getStartAt())
        );
    }

    @Test
    @DisplayName("날짜와 테마를 입력받아 해당 날짜, 테마에 예약된 시간을 모두 조회한다.")
    void find_reservation_time_by_theme_and_date() {
        ReservationTime savedTime1 = reservationTimeRepository.insertReservationTime(time1);
        ReservationTime savedTime2 = reservationTimeRepository.insertReservationTime(time2);
        ReservationTime savedTime3 = reservationTimeRepository.insertReservationTime(time3);
        Theme savedTheme1 = themeRepository.insertTheme(theme1);
        Theme savedTheme2 = themeRepository.insertTheme(theme2);
        Member savedMember1 = memberRepository.insertMember(member1);
        Member savedMember2 = memberRepository.insertMember(member2);
        Member savedMember3 = memberRepository.insertMember(member3);

        Reservation reservation1 = new Reservation(
                null, savedMember1, savedTheme1, new ReservationDate("2024-05-01"), savedTime1);
        Reservation reservation2 = new Reservation(
                null, savedMember2, savedTheme1, new ReservationDate("2024-05-01"), savedTime2);
        Reservation reservation3 = new Reservation(
                null, savedMember3, savedTheme2, new ReservationDate("2024-05-02"), savedTime3);
        reservationRepository.insertReservation(reservation1);
        reservationRepository.insertReservation(reservation2);
        reservationRepository.insertReservation(reservation3);

        List<ReservationTime> bookedTimes = reservationTimeRepository.findReservedTimeByThemeAndDate("2024-05-01",
                savedTheme1.getId());

        assertAll(
                () -> assertThat(bookedTimes.size()).isEqualTo(2),
                () -> assertThat(bookedTimes.get(0).getStartAt()).isEqualTo(reservation1.getTimeStartAt()),
                () -> assertThat(bookedTimes.get(1).getStartAt()).isEqualTo(reservation2.getTimeStartAt())
        );
    }
}
