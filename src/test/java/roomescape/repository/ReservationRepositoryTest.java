package roomescape.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.Theme;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@JdbcTest
class ReservationRepositoryTest {

    private final ReservationRepository reservationRepository;

    @Autowired
    ReservationRepositoryTest(DataSource dataSource) {
        this.reservationRepository = new H2ReservationRepository(dataSource);
    }

    @Test
    @DisplayName("모든 예약 목록을 조회한다.")
    void findAll() {
        // given
        final Member member1 = new Member(1L, "", "", "", null);
        final Member member2 = new Member(2L, "", "", "", null);
        final Member member3 = new Member(3L, "", "", "", null);

        final ReservationTime time1 = new ReservationTime(1L, null);
        final ReservationTime time2 = new ReservationTime(2L, null);
        final ReservationTime time3 = new ReservationTime(3L, null);
        final ReservationTime time4 = new ReservationTime(4L, null);

        final Theme theme1 = new Theme(1L, "", "", "");
        final Theme theme2 = new Theme(2L, "", "", "");
        final Theme theme3 = new Theme(3L, "", "", "");

        final LocalDate today = LocalDate.now();

        final List<Reservation> expected = List.of(
                new Reservation(1L, member1, today.minusDays(3), time1, theme1),
                new Reservation(2L, member2, today.minusDays(2), time3, theme2),
                new Reservation(3L, member1, today.minusDays(1), time2, theme2),
                new Reservation(4L, member2, today.minusDays(1), time1, theme2),
                new Reservation(5L, member3, today.minusDays(7), time1, theme3),
                new Reservation(6L, member3, today.plusDays(3), time4, theme3)
        );

        // when
        List<Reservation> actual = reservationRepository.findAll();

        // then
        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    @DisplayName("특정 id를 통해 예약을 조회한다.")
    void findByIdPresent() {
        // given
        long id = 2L;
        Reservation expected = new Reservation(
                id,
                new Member(2L, "be", "bb@email.com", "1111", Role.USER),
                LocalDate.of(2025, 2, 19),
                new ReservationTime(2L, LocalTime.parse("00:00")),
                new Theme(2L, "", "", "")
        );

        // when
        Optional<Reservation> actual = reservationRepository.findById(id);

        // then
        assertThat(actual).hasValue(expected);
    }

    @Test
    @DisplayName("존재하지 않는 예약을 조회할 경우 빈 값을 반환한다.")
    void findByIdNotPresent() {
        // given
        long id = 100L;

        // when
        Optional<Reservation> actual = reservationRepository.findById(id);

        // then
        assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("등록된 시간 아이디로 예약 존재 여부를 확인한다.")
    void existsByTimeIdPresent() {
        // given
        final long timeId = 2L;
        final boolean expected = true;

        // when
        final boolean actual = reservationRepository.existsByTimeId(timeId);

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("등록되지 않은 시간 아이디로 예약 존재 여부를 확인한다,")
    void existsByTimeIdNotPresent() {
        // given
        final long timeId = 100L;
        final boolean expected = false;

        // when
        final boolean actual = reservationRepository.existsByTimeId(timeId);

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("등록된 예약 번호로 삭제한다.")
    void deleteAssignedId() {
        // given
        final long id = 2L;

        // when & then
        assertThat(reservationRepository.findById(id)).isPresent();
        assertThatCode(() -> reservationRepository.deleteById(id))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("최근 일주일간 인기 테마를 조회한다.")
    void findPopularThemes() {
        //given
        final List<Theme> expected = List.of(new Theme(2L, "", "", ""),
                new Theme(1L, "", "", ""),
                new Theme(3L, "", "", ""));

        final LocalDate from = LocalDate.now().minusDays(7);
        final LocalDate until = LocalDate.now().minusDays(1);

        //when & then
        final List<Theme> actual = reservationRepository.findPopularThemes(from, until, 10);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("한달 전 인기 테마를 조회한다.")
    void findPopularThemesAMonthAgo() {
        //given
        List<Theme> expected = List.of();
        final LocalDate from = LocalDate.now().minusMonths(1).minusDays(7);
        final LocalDate until = LocalDate.now().minusMonths(1).minusDays(1);

        //when & then
        final List<Theme> actual = reservationRepository.findPopularThemes(from, until, 10);
        assertThat(actual).isEqualTo(expected);
    }
}
