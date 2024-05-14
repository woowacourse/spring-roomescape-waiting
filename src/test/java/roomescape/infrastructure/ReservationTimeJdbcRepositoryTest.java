package roomescape.infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.member.Role;
import roomescape.domain.reservation.*;
import roomescape.exception.InvalidReservationException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@JdbcTest
class ReservationTimeJdbcRepositoryTest {
    private ReservationTimeRepository reservationTimeRepository;
    private ReservationRepository reservationRepository;
    private ThemeRepository themeRepository;
    private MemberRepository memberRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        reservationTimeRepository = new ReservationTimeJdbcRepository(jdbcTemplate);
        reservationRepository = new ReservationJdbcRepository(jdbcTemplate);
        themeRepository = new ThemeJdbcRepository(jdbcTemplate);
        memberRepository = new MemberJdbcRepository(jdbcTemplate);
    }

    @DisplayName("새로운 예약 시간을 저장한다.")
    @Test
    void saveReservationTime() {
        //given
        String startAt = "10:00";
        ReservationTime reservationTime = new ReservationTime(startAt);

        //when
        ReservationTime result = reservationTimeRepository.save(reservationTime);

        //then
        assertThat(result.getId()).isNotZero();
    }

    @DisplayName("모든 예약 시간을 조회한다.")
    @Test
    void findAllReservationTimesTest() {
        //given
        String startAt = "10:00";
        ReservationTime reservationTime = new ReservationTime(startAt);
        reservationTimeRepository.save(reservationTime);
        int expectedSize = 1;

        //when
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();

        //then
        assertThat(reservationTimes.size()).isEqualTo(expectedSize);
    }

    @DisplayName("id로 예약 시간을 조회한다.")
    @Test
    void findReservationTimeByIdTest() {
        //given
        String startAt = "10:00";
        ReservationTime reservationTime = new ReservationTime(startAt);
        ReservationTime target = reservationTimeRepository.save(reservationTime);

        //when
        ReservationTime result = reservationTimeRepository.getById(target.getId());

        //then
        assertAll(
                () -> assertThat(result.getId()).isEqualTo(target.getId()),
                () -> assertThat(result.getStartAt()).isEqualTo(startAt)
        );
    }

    @DisplayName("id를 가진 시간을 찾을 수 없으면 예외가 발생한다.")
    @Test
    void cannotFindByUnknownId() {
        //given
        long unknownId = 0;

        //when&then
        assertThatThrownBy(() -> reservationTimeRepository.getById(unknownId))
                .isInstanceOf(InvalidReservationException.class)
                .hasMessage("더이상 존재하지 않는 시간입니다.");
    }

    @DisplayName("id로 예약을 삭제한다.")
    @Test
    void deleteReservationTimeByIdTest() {
        //given
        String startAt = "10:00";
        ReservationTime reservationTime = new ReservationTime(startAt);
        ReservationTime target = reservationTimeRepository.save(reservationTime);
        int expectedSize = 0;

        //when
        reservationTimeRepository.deleteById(target.getId());

        //then
        assertThat(reservationTimeRepository.findAll().size()).isEqualTo(expectedSize);
    }

    @DisplayName("주어진 시간이 이미 존재한다.")
    @Test
    void existsTime() {
        //given
        String startAt = "10:00";
        ReservationTime reservationTime = new ReservationTime(startAt);
        reservationTimeRepository.save(reservationTime);

        //when
        boolean result = reservationTimeRepository.existsByTime(startAt);

        //then
        assertThat(result).isTrue();
    }

    @DisplayName("주어진 시간이 존재하지 않는다.")
    @Test
    void notExistsTime() {
        //given
        String startAt = "10:00";
        ReservationTime reservationTime = new ReservationTime(startAt);
        reservationTimeRepository.save(reservationTime);

        //when
        String newStartAt = "10:01";
        boolean result = reservationTimeRepository.existsByTime(newStartAt);

        //then
        assertThat(result).isFalse();
    }

    @DisplayName("예약이 있는 시간을 찾는다.")
    @Test
    void findBookedTimesByThemeAndDate() {
        //given
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime("10:00"));
        Theme theme = themeRepository.save(new Theme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"));
        String reservationDate = "2222-05-01";
        Member member = memberRepository.save(new Member("lini", "lini@email.com", "lini123", Role.GUEST));
        Reservation reservation = new Reservation(reservationDate, member, reservationTime, theme);
        reservationRepository.save(reservation);

        //when
        List<ReservationTime> result = reservationTimeRepository.findBookedTimesByDateAndTheme(reservationDate, theme.getId());

        //then
        assertThat(result).hasSize(1);
    }

    @DisplayName("예약이 있는 시간이 있다.")
    @Test
    void findNoBookedTimesByThemeAndDate() {
        //given
        reservationTimeRepository.save(new ReservationTime("10:00"));

        //when
        List<ReservationTime> result = reservationTimeRepository.findBookedTimesByDateAndTheme("2222-05-01", 0);

        //then
        assertThat(result).hasSize(0);
    }
}
