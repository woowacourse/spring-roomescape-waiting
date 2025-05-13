package roomescape.reservation.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;
import roomescape.member.domain.Role;
import roomescape.member.infrastructure.JdbcMemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.domain.ReservationTimeRepository;
import roomescape.reservationTime.infrastructure.JdbcReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;
import roomescape.theme.infrastructure.JdbcThemeRepository;

@Sql(scripts = "/data/reservationConditionTest.sql")
@JdbcTest
class JdbcReservationRepositoryTest {

    @Autowired
    private DataSource dataSource;
    private ReservationRepository reservationRepository;
    private ThemeRepository themeRepository;
    private ReservationTimeRepository timeRepository;
    private MemberRepository memberRepository;

    private static Stream<Arguments> findByMemberIdAndThemeIdAndDate_test() {
        return Stream.of(
                Arguments.of(1L, null, null, null, 3),
                Arguments.of(2L, null, null, null, 1),
                Arguments.of(3L, null, null, null, 0),
                Arguments.of(null, 1L, null, null, 2),
                Arguments.of(null, 2L, null, null, 1),
                Arguments.of(null, 3L, null, null, 1),
                Arguments.of(null, null, LocalDate.of(2025, 4, 19), null, 3),
                Arguments.of(null, null, LocalDate.of(2025, 4, 18), null, 4),
                Arguments.of(null, null, LocalDate.of(2025, 4, 28), null, 2),
                Arguments.of(null, null, null, LocalDate.of(2025, 4, 28), 4),
                Arguments.of(null, null, null, LocalDate.of(2025, 4, 26), 2),
                Arguments.of(null, null, null, LocalDate.of(2025, 4, 18), 1),
                Arguments.of(null, null, null, LocalDate.of(2025, 4, 17), 0),
                Arguments.of(1L, 1L, null, null, 2),
                Arguments.of(1L, null, LocalDate.of(2025, 4, 28), null, 2),
                Arguments.of(1L, null, LocalDate.of(2025, 4, 26), null, 3)
        );
    }

    @BeforeEach
    void beforeEach() {
        reservationRepository = new JdbcReservationRepository(dataSource);
        themeRepository = new JdbcThemeRepository(dataSource);
        timeRepository = new JdbcReservationTimeRepository(dataSource);
        memberRepository = new JdbcMemberRepository(dataSource);
    }

    @Test
    @DisplayName("저장 후 아이디 반환 테스트")
    void save_test() {
        // given
        ReservationTime reservationTime = timeRepository.findById(1L);
        Theme theme = themeRepository.findById(1L);
        Optional<Member> member = memberRepository.findById(1L);
        Reservation reservation = Reservation.createWithoutId(
                LocalDateTime.of(1999, 11, 2, 20, 10), member.get(), LocalDate.of(2000, 11, 2), reservationTime, theme);
        // when
        Long save = reservationRepository.save(reservation);
        // then
        assertThat(save).isNotNull();
    }

    @Test
    @DisplayName("날짜와 테마 관련 조회 테스트")
    void find_by_themeId_and_date() {
        // given
        ReservationTime reservationTime = timeRepository.findById(1L);
        Theme theme = themeRepository.findById(1L);
        Optional<Member> member = memberRepository.findById(1L);
        Reservation reservation1 = Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member.get(),
                LocalDate.of(2000, 11, 2), reservationTime, theme);
        Reservation reservation2 = Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member.get(),
                LocalDate.of(2000, 10, 2), reservationTime, theme);
        Reservation reservation3 = Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member.get(),
                LocalDate.of(2000, 11, 2), reservationTime, theme);
        reservationRepository.save(reservation1);
        reservationRepository.save(reservation2);
        reservationRepository.save(reservation3);
        // when
        List<Reservation> reservations = reservationRepository.findByDateAndThemeId(LocalDate.of(2000, 11, 2), 1L);
        // then
        assertThat(reservations).hasSize(2);

    }

    @ParameterizedTest
    @DisplayName("삭제 성공 관련 테스트")
    @CsvSource({"3,false", "4,false"})
    void delete_test(Long id, boolean expected) {
        // given
        ReservationTime reservationTime = timeRepository.findById(1L);
        Theme theme = themeRepository.findById(1L);
        Optional<Member> member = memberRepository.findById(1L);
        Reservation reservation = Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member.get(),
                LocalDate.of(2000, 11, 2), reservationTime, theme);
        reservationRepository.save(reservation);
        // when
        boolean isDeleted = reservationRepository.deleteById(id);
        // then
        assertThat(isDeleted).isEqualTo(expected);
    }

    @Test
    @DisplayName("전체 조회 테스트")
    void find_all_test() {
        // given
        ReservationTime reservationTime = timeRepository.findById(1L);
        Theme theme = themeRepository.findById(1L);
        Optional<Member> member = memberRepository.findById(1L);
        Reservation reservation1 = Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member.get(),
                LocalDate.of(2000, 11, 2), reservationTime, theme);
        Reservation reservation2 = Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member.get(),
                LocalDate.of(2000, 10, 2), reservationTime, theme);
        Reservation reservation3 = Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member.get(),
                LocalDate.of(2000, 11, 2), reservationTime, theme);
        reservationRepository.save(reservation1);
        reservationRepository.save(reservation2);
        reservationRepository.save(reservation3);
        // when
        List<Reservation> reservations = reservationRepository.findAll();
        // then
        assertThat(reservations).hasSize(7);

    }

    @ParameterizedTest
    @DisplayName("예약 시간 유무 조회 테스트")
    @CsvSource({"1,true", "4,false"})
    void exist_by_time(Long themeId, boolean expected) {
        // given
        ReservationTime reservationTime = timeRepository.findById(1L);
        Theme theme = themeRepository.findById(1L);
        Optional<Member> member = memberRepository.findById(1L);
        Reservation reservation = Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member.get(),
                LocalDate.of(2000, 11, 2), reservationTime, theme);
        reservationRepository.save(reservation);
        // when
        boolean existed = reservationRepository.existByReservationTimeId(themeId);
        // then
        assertThat(existed).isEqualTo(expected);
    }

    @Test
    @DisplayName("특정 조건 조회 테스트")
    void exist_by_test() {
        // given
        ReservationTime reservationTime = timeRepository.findById(1L);
        Theme theme = themeRepository.findById(1L);
        Optional<Member> member = memberRepository.findById(1L);
        Reservation reservation = Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member.get(),
                LocalDate.of(2000, 11, 2), reservationTime, theme);
        reservationRepository.save(reservation);
        // when & then
        assertAll(
                () -> assertThat(reservationRepository.hasSameReservation(
                        createReservationForTestBy(1L, LocalDate.of(2000, 11, 3),
                                LocalTime.of(10, 0)))).isFalse(),
                () -> assertThat(reservationRepository.hasSameReservation(
                        createReservationForTestBy(1L, LocalDate.of(2000, 11, 2),
                                LocalTime.of(10, 1)))).isFalse(),
                () -> assertThat(reservationRepository.hasSameReservation(
                        createReservationForTestBy(1L + 1, LocalDate.of(2000, 11, 2),
                                LocalTime.of(10, 0)))).isFalse(),
                () -> assertThat(reservationRepository.hasSameReservation(
                        createReservationForTestBy(1L, LocalDate.of(2000, 11, 2),
                                LocalTime.of(10, 0)))).isTrue()
        );
    }

    private Reservation createReservationForTestBy(Long themeId, LocalDate localDate, LocalTime localTime) {
        ReservationTime reservationTime = ReservationTime.createWithId(1L, localTime);
        Theme theme = Theme.createWithId(themeId, "a", "a", "a");
        Member member = Member.createWithId(1L, "a", "a", "a", Role.USER);
        return Reservation.createWithId(1L, member, localDate, reservationTime, theme);
    }

    @ParameterizedTest
    @DisplayName("테마 유무 조회 테스트")
    @CsvSource({"3,true", "4,false"})
    void exist_by_theme(Long themeId, boolean expected) {
        // given
        ReservationTime reservationTime = timeRepository.findById(1L);
        Theme theme = themeRepository.findById(1L);
        Optional<Member> member = memberRepository.findById(1L);
        Reservation reservation = Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member.get(),
                LocalDate.of(2000, 11, 2), reservationTime, theme);
        reservationRepository.save(reservation);
        // when
        boolean existed = reservationRepository.existByThemeId(themeId);
        // then
        assertThat(existed).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource
    @DisplayName("예약 조건 조회 테스트")
    void findByMemberIdAndThemeIdAndDate_test(Long memberId, Long themeId, LocalDate from, LocalDate to,
                                              int expectedSize) {
        // when
        List<Reservation> reservations = reservationRepository.findByMemberIdAndThemeIdAndDate(memberId,
                themeId, from, to);
        // then
        assertThat(reservations).hasSize(expectedSize);
    }
}
