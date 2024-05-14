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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
class ReservationJdbcRepositoryTest {
    private ReservationRepository reservationRepository;
    private ReservationTimeRepository reservationTimeRepository;
    private ThemeRepository themeRepository;
    private MemberRepository memberRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        reservationRepository = new ReservationJdbcRepository(jdbcTemplate);
        reservationTimeRepository = new ReservationTimeJdbcRepository(jdbcTemplate);
        themeRepository = new ThemeJdbcRepository(jdbcTemplate);
        memberRepository = new MemberJdbcRepository(jdbcTemplate);
    }

    @DisplayName("새로운 예약을 저장한다.")
    @Test
    void saveReservation() {
        //given
        Reservation reservation = new Reservation(getReservationDatePlus(1), getMember("lini", Role.GUEST), getReservationTime("10:00"), getTheme("레벨2 탈출"));

        //when
        Reservation result = reservationRepository.save(reservation);

        //then
        assertThat(result.getId()).isNotZero();
    }

    @DisplayName("모든 예약 내역을 조회한다.")
    @Test
    void findAllReservationTest() {
        //given
        saveReservation(getReservationDatePlus(1), getReservationTime("10:00"), getTheme("레벨2 탈출"), getMember("lini", Role.GUEST));
        int expectedSize = 1;

        //when
        List<Reservation> reservations = reservationRepository.findAll();

        //then
        assertThat(reservations.size()).isEqualTo(expectedSize);
    }

    @DisplayName("id로 예약을 삭제한다.")
    @Test
    void deleteReservationByIdTest() {
        //given
        Reservation target = saveReservation(getReservationDatePlus(1), getReservationTime("10:00"), getTheme("레벨2 탈출"), getMember("lini", Role.GUEST));

        int expectedSize = 0;

        //when
        reservationRepository.deleteById(target.getId());

        //then
        assertThat(reservationRepository.findAll().size()).isEqualTo(expectedSize);
    }

    @DisplayName("주어진 일정, 테마가 동일한 예약이 존재한다.")
    @Test
    void existsByDateAndTimeAndThemeTest() {
        //given
        String reservationDate = getReservationDatePlus(1);
        ReservationTime reservationTime = getReservationTime("10:00");
        Theme theme = getTheme("레벨2 탈출");
        saveReservation(reservationDate, reservationTime, theme, getMember("lini", Role.GUEST));

        //when
        boolean result = reservationRepository.existsByDateAndTimeAndTheme(reservationDate, reservationTime.getId(), theme.getId());

        //then
        assertThat(result).isTrue();
    }

    @DisplayName("주어진 일정, 테마가 동일한 예약이 존재하지 않는다.")
    @Test
    void notExistsByDateAndTimeAndThemeTest() {
        //given
        ReservationTime reservationTime = getReservationTime("10:00");
        Theme theme = getTheme("레벨2 탈출");
        saveReservation(getReservationDatePlus(1), reservationTime, theme, getMember("lini", Role.GUEST));
        String newDate = getReservationDatePlus(2);


        //when
        boolean result = reservationRepository.existsByDateAndTimeAndTheme(newDate, reservationTime.getId(), theme.getId());

        //then
        assertThat(result).isFalse();
    }

    @DisplayName("주어진 시간에 대한 예약이 존재한다.")
    @Test
    void existsByTimeIdTest() {
        //given
        ReservationTime reservationTime = getReservationTime("10:00");
        saveReservation(getReservationDatePlus(1), reservationTime, getTheme("레벨2 탈출"), getMember("lini", Role.GUEST));

        //when
        boolean result = reservationRepository.existsByTimeId(reservationTime.getId());

        //then
        assertThat(result).isTrue();
    }

    @DisplayName("주어진 시간에 대한 예약이 존재하지 않는다.")
    @Test
    void notExistsByTimeIdTest() {
        //when
        ReservationTime reservationTime = getReservationTime("10:00");
        boolean result = reservationRepository.existsByTimeId(reservationTime.getId());

        //then
        assertThat(result).isFalse();
    }

    @DisplayName("주어진 테마에 대한 예약이 존재한다.")
    @Test
    void existsByThemeIdTest() {
        //given
        Theme theme = getTheme("레벨2 탈출");
        saveReservation(getReservationDatePlus(1), getReservationTime("10:00"), theme, getMember("lini", Role.GUEST));


        //when
        boolean result = reservationRepository.existsByThemeId(theme.getId());

        //then
        assertThat(result).isTrue();
    }

    @DisplayName("주어진 테마에 대한 예약이 존재하지 않는다.")
    @Test
    void notExistsByThemeIdTest() {
        //when
        Theme theme = getTheme("레벨2 탈출");
        boolean result = reservationRepository.existsByThemeId(theme.getId());

        //then
        assertThat(result).isFalse();
    }

    @DisplayName("주어진 식별자의 예약이 존재한다.")
    @Test
    void existsByIdTest() {
        //given
        Reservation target = saveReservation(getReservationDatePlus(1), getReservationTime("10:00"), getTheme("레벨2 탈출"), getMember("lini", Role.GUEST));

        //when
        boolean result = reservationRepository.existsById(target.getId());

        //then
        assertThat(result).isTrue();
    }

    @DisplayName("주어진 식별자의 예약이 존재하지 않는다.")
    @Test
    void notExistsByIdTest() {
        //when
        boolean result = reservationRepository.existsByThemeId(0);

        //then
        assertThat(result).isFalse();
    }

    @DisplayName("주어진 식별자의 예약을 찾는다.")
    @Test
    void findById() {
        //given
        Reservation target = saveReservation(getReservationDatePlus(1), getReservationTime("10:00"), getTheme("레벨2 탈출"), getMember("lini", Role.GUEST));

        //when
        Optional<Reservation> result = reservationRepository.findById(target.getId());

        //then
        assertThat(result).isNotEmpty();
    }

    @DisplayName("주어진 식별자의 예약을 찾지 못한다.")
    @Test
    void cannotFindById() {
        //when
        Optional<Reservation> reservation = reservationRepository.findById(0);

        //then
        assertThat(reservation).isEmpty();
    }

    @DisplayName("주어진 식별자의 예약을 가져온다.")
    @Test
    void getById() {
        //given
        Reservation target = saveReservation(getReservationDatePlus(1), getReservationTime("10:00"), getTheme("레벨2 탈출"), getMember("lini", Role.GUEST));

        //when
        Reservation result = reservationRepository.getById(target.getId());

        //then
        assertThat(result.getId()).isEqualTo(target.getId());
    }

    @DisplayName("주어진 조건에 맞는 예약 목록을 찾는다. - DateFrom, DateTo")
    @Test
    void findByDateFromAndDateTo() {
        //given
        ReservationTime reservationTime = getReservationTime("10:00");
        Member lini = getMember("lini", Role.GUEST);
        Member lily = getMember("lily", Role.GUEST);
        Theme level1 = getTheme("레벨1 탈출");
        Theme level2 = getTheme("레벨2 탈출");
        Theme level3 = getTheme("레벨3 탈출");
        saveReservation(getReservationDatePlus(1), reservationTime, level1, lini);
        saveReservation(getReservationDatePlus(2), reservationTime, level2, lini);
        saveReservation(getReservationDatePlus(3), reservationTime, level2, lily);
        saveReservation(getReservationDatePlus(4), reservationTime, level3, lini);

        //when
        List<Reservation> result = reservationRepository.findBy(null, null, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));

        //then
        assertThat(result).hasSize(3);
    }

    @DisplayName("주어진 조건에 맞는 예약 목록을 찾는다. - memberId")
    @Test
    void findByMemberId() {
        //given
        ReservationTime reservationTime = getReservationTime("10:00");
        Member lini = getMember("lini", Role.GUEST);
        Member lily = getMember("lily", Role.GUEST);
        Theme level1 = getTheme("레벨1 탈출");
        Theme level2 = getTheme("레벨2 탈출");
        Theme level3 = getTheme("레벨3 탈출");
        saveReservation(getReservationDatePlus(1), reservationTime, level1, lini);
        saveReservation(getReservationDatePlus(2), reservationTime, level2, lini);
        saveReservation(getReservationDatePlus(3), reservationTime, level2, lily);
        saveReservation(getReservationDatePlus(4), reservationTime, level3, lini);

        //when
        List<Reservation> result = reservationRepository.findBy(lini.getId(), null, null, null);

        //then
        assertThat(result).hasSize(3);
    }

    @DisplayName("주어진 조건에 맞는 예약 목록을 찾는다. - themeId, DateFrom")
    @Test
    void findByThemeIdAndDateFrom() {
        //given
        ReservationTime reservationTime = getReservationTime("10:00");
        Member lini = getMember("lini", Role.GUEST);
        Member lily = getMember("lily", Role.GUEST);
        Theme level1 = getTheme("레벨1 탈출");
        Theme level2 = getTheme("레벨2 탈출");
        Theme level3 = getTheme("레벨3 탈출");
        saveReservation(getReservationDatePlus(1), reservationTime, level1, lini);
        saveReservation(getReservationDatePlus(2), reservationTime, level2, lini);
        saveReservation(getReservationDatePlus(3), reservationTime, level2, lily);
        saveReservation(getReservationDatePlus(4), reservationTime, level3, lini);

        //when
        List<Reservation> result = reservationRepository.findBy(null, level2.getId(), LocalDate.now().plusDays(3), null);

        //then
        assertThat(result).hasSize(1);
    }

    @DisplayName("주어진 조건에 맞는 예약 목록을 찾는다. - memberId, DateTo, DateFrom")
    @Test
    void findByMemberIdAndDateToAndDateFrom() {
        //given
        ReservationTime reservationTime = getReservationTime("10:00");
        Member lini = getMember("lini", Role.GUEST);
        Member lily = getMember("lily", Role.GUEST);
        Theme level1 = getTheme("레벨1 탈출");
        Theme level2 = getTheme("레벨2 탈출");
        Theme level3 = getTheme("레벨3 탈출");
        saveReservation(getReservationDatePlus(1), reservationTime, level1, lini);
        saveReservation(getReservationDatePlus(2), reservationTime, level2, lini);
        saveReservation(getReservationDatePlus(3), reservationTime, level2, lily);
        saveReservation(getReservationDatePlus(4), reservationTime, level3, lini);

        //when
        List<Reservation> result = reservationRepository.findBy(lini.getId(), null, LocalDate.now().plusDays(1), LocalDate.now().plusDays(2));

        //then
        assertThat(result).hasSize(2);
    }

    @DisplayName("주어진 조건에 맞는 예약 목록을 찾는다. - memberId, themeId, DateTo, DateFrom")
    @Test
    void findByMemberIdAndThemeIdAndDateToAndDateFrom() {
        //given
        ReservationTime reservationTime = getReservationTime("10:00");
        Member lini = getMember("lini", Role.GUEST);
        Member lily = getMember("lily", Role.GUEST);
        Theme level1 = getTheme("레벨1 탈출");
        Theme level2 = getTheme("레벨2 탈출");
        Theme level3 = getTheme("레벨3 탈출");
        saveReservation(getReservationDatePlus(1), reservationTime, level1, lini);
        saveReservation(getReservationDatePlus(2), reservationTime, level2, lini);
        saveReservation(getReservationDatePlus(3), reservationTime, level2, lily);
        saveReservation(getReservationDatePlus(4), reservationTime, level3, lini);

        //when
        List<Reservation> result = reservationRepository.findBy(lini.getId(), level2.getId(), LocalDate.now().plusDays(3), LocalDate.now().plusDays(4));

        //then
        assertThat(result).hasSize(0);
    }

    private Reservation saveReservation(String reservationDate, ReservationTime reservationTime, Theme theme, Member member) {
        Reservation reservation = new Reservation(reservationDate, member, reservationTime, theme);
        return reservationRepository.save(reservation);
    }

    private Member getMember(String name, Role role) {
        return memberRepository.save(new Member(name, name + "@email.com", name + "123", role));
    }

    private Theme getTheme(String name) {
        return themeRepository.save(new Theme(name, "우테코 레벨2를 탈출하는 내용입니다.", "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"));
    }

    private ReservationTime getReservationTime(String time) {
        return reservationTimeRepository.save(new ReservationTime(time));
    }

    private String getReservationDatePlus(int days) {
        return LocalDate.now().plusDays(days).toString();
    }
}
