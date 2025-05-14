package roomescape.theme.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import roomescape.member.domain.Member;
import roomescape.member.domain.JpaMemberRepository;
import roomescape.member.domain.Role;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationPeriod;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@JdbcTest
class JdbcThemeRepositoryTest {

    @Autowired
    private DataSource dataSource;
    private ThemeRepository repository;

    @BeforeEach
    void beforeEach() {
        repository = new JdbcThemeRepository(dataSource);
    }

    @Test
    @DisplayName("저장 후 아이디 반환 테스트")
    void save_test() {
        Theme theme = Theme.createWithoutId("a", "a", "a");

        Long save = repository.save(theme);

        assertThat(save).isNotNull();
    }

    @ParameterizedTest
    @DisplayName("삭제 성공 관련 테스트")
    @CsvSource({"0,true", "1,false"})
    void delete_test(Long plus, boolean expected) {
        // given
        Theme theme = Theme.createWithoutId("a", "a", "a");
        Long save = repository.save(theme);
        // when
        boolean isDeleted = repository.deleteById(save + plus);
        // then
        assertThat(isDeleted).isEqualTo(expected);
    }

    @Test
    @DisplayName("전체 조회 테스트")
    void find_all_test() {
        // given
        Theme theme1 = Theme.createWithoutId("a", "a", "a");
        Theme theme2 = Theme.createWithoutId("b", "b", "b");
        Theme theme3 = Theme.createWithoutId("c", "c", "c");
        repository.save(theme1);
        repository.save(theme2);
        repository.save(theme3);
        // when
        List<Theme> reservations = repository.findAll();
        // then
        List<String> names = reservations.stream()
                .map(Theme::getName)
                .toList();
        List<String> descriptions = reservations.stream()
                .map(Theme::getName)
                .toList();
        List<String> thumbnails = reservations.stream()
                .map(Theme::getName)
                .toList();
        assertAll(
                () -> assertThat(reservations).hasSize(3),
                () -> assertThat(names).contains("a", "b", "c"),
                () -> assertThat(descriptions).contains("a", "b", "c"),
                () -> assertThat(thumbnails).contains("a", "b", "c")
        );
    }

    @Test
    @DisplayName("아이디로 조회 테스트")
    void find_by_id() {
        // given
        Theme theme = Theme.createWithoutId("a", "a", "a");
        Long save = repository.save(theme);
        // when
        Theme findTheme = repository.findById(save);
        // then
        assertAll(
                () -> assertThat(findTheme.getName()).isEqualTo(theme.getName()),
                () -> assertThat(findTheme.getDescription()).isEqualTo(theme.getDescription()),
                () -> assertThat(findTheme.getThumbnail()).isEqualTo(theme.getThumbnail())
        );
    }

    @Test
    @DisplayName("인기 많은 테마를 순서대로 반환한다.(시간 조건 미포함, 개수 조건 미포함)")
    void find_popular_theme_no_time_and_count_condition() {
        // given
        ReservationRepository reservationRepository = new JdbcReservationRepository(dataSource);
        ReservationTimeRepository reservationTimeRepository = new JdbcReservationTimeRepository(dataSource);
        JpaMemberRepository memberRepository = new JdbcMemberRepository(dataSource);
        ReservationTime reservationTime1 = ReservationTime.createWithoutId(LocalTime.of(10, 10));
        ReservationTime reservationTime2 = ReservationTime.createWithoutId(LocalTime.of(10, 11));
        Theme theme1 = Theme.createWithoutId("a", "a", "a");
        Theme theme2 = Theme.createWithoutId("b", "b", "b");
        Theme theme3 = Theme.createWithoutId("c", "c", "c");

        Long timeId1 = reservationTimeRepository.save(reservationTime1);
        Long timeId2 = reservationTimeRepository.save(reservationTime2);

        Member member = Member.createWithoutId("a", "a", "a", Role.USER);
        Long memberId = memberRepository.save(member);
        member = member.assignId(memberId);

        Long themeId1 = repository.save(theme1);
        Long themeId2 = repository.save(theme2);
        repository.save(theme3);

        Reservation reservation1 = Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member,
                LocalDate.of(2000, 11, 2),
                reservationTime1.assignId(timeId1), theme1.assignId(themeId1));
        Reservation reservation2 = Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member,
                LocalDate.of(2000, 11, 3),
                reservationTime2.assignId(timeId2), theme1.assignId(themeId1));
        Reservation reservation3 = Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member,
                LocalDate.of(2000, 11, 3),
                reservationTime1.assignId(timeId1), theme2.assignId(themeId2));
        reservationRepository.save(reservation1);
        reservationRepository.save(reservation2);
        reservationRepository.save(reservation3);

        ReservationPeriod period = new ReservationPeriod(LocalDate.of(2000, 11, 5), 3, 1);
        // when
        List<Theme> popularThemes = repository.findPopularThemes(period, 3);
        List<String> names = popularThemes.stream()
                .map(Theme::getName)
                .toList();
        // then
        assertThat(popularThemes).hasSize(2);
        assertThat(names).containsExactly("a", "b");
    }

    @Test
    @DisplayName("인기 많은 테마를 순서대로 반환한다.(시간 조건 포함, 개수 조건 미포함)")
    void find_popular_theme_no_count_condition() {
        // given
        ReservationRepository reservationRepository = new JdbcReservationRepository(dataSource);
        ReservationTimeRepository reservationTimeRepository = new JdbcReservationTimeRepository(dataSource);
        JpaMemberRepository memberRepository = new JdbcMemberRepository(dataSource);
        ReservationTime reservationTime1 = ReservationTime.createWithoutId(LocalTime.of(10, 10));
        ReservationTime reservationTime2 = ReservationTime.createWithoutId(LocalTime.of(10, 11));
        Theme theme1 = Theme.createWithoutId("a", "a", "a");
        Theme theme2 = Theme.createWithoutId("b", "b", "b");
        Theme theme3 = Theme.createWithoutId("c", "c", "c");

        Long timeId1 = reservationTimeRepository.save(reservationTime1);
        Long timeId2 = reservationTimeRepository.save(reservationTime2);
        Long themeId1 = repository.save(theme1);
        Long themeId2 = repository.save(theme2);
        repository.save(theme3);

        Member member = Member.createWithoutId("a", "a", "a", Role.USER);
        Long memberId = memberRepository.save(member);
        member = member.assignId(memberId);

        Reservation reservation1 = Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member,
                LocalDate.of(2000, 11, 2),
                reservationTime1.assignId(timeId1), theme1.assignId(themeId1));
        Reservation reservation2 = Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member,
                LocalDate.of(2000, 11, 3),
                reservationTime2.assignId(timeId2), theme1.assignId(themeId1));
        Reservation reservation3 = Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member,
                LocalDate.of(2000, 11, 3),
                reservationTime1.assignId(timeId1), theme2.assignId(themeId2));
        Reservation reservation4 = Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member,
                LocalDate.of(2000, 11, 4),
                reservationTime1.assignId(timeId1), theme2.assignId(themeId2));
        reservationRepository.save(reservation1);
        reservationRepository.save(reservation2);
        reservationRepository.save(reservation3);
        reservationRepository.save(reservation4);

        ReservationPeriod period = new ReservationPeriod(LocalDate.of(2000, 11, 5), 2, 1);
        // when
        List<Theme> popularThemes = repository.findPopularThemes(period, 3);
        List<String> names = popularThemes.stream()
                .map(Theme::getName)
                .toList();
        // then
        assertThat(popularThemes).hasSize(2);
        assertThat(names).containsExactly("b", "a");
    }

    @Test
    @DisplayName("인기 많은 테마를 순서대로 반환한다.(시간 조건 포함, 개수 조건 포함)")
    void find_popular_theme() {
        // given
        ReservationRepository reservationRepository = new JdbcReservationRepository(dataSource);
        ReservationTimeRepository reservationTimeRepository = new JdbcReservationTimeRepository(dataSource);
        JpaMemberRepository memberRepository = new JdbcMemberRepository(dataSource);
        ReservationTime reservationTime1 = ReservationTime.createWithoutId(LocalTime.of(10, 10));
        ReservationTime reservationTime2 = ReservationTime.createWithoutId(LocalTime.of(10, 11));
        Theme theme1 = Theme.createWithoutId("a", "a", "a");
        Theme theme2 = Theme.createWithoutId("b", "b", "b");
        Theme theme3 = Theme.createWithoutId("c", "c", "c");

        Long timeId1 = reservationTimeRepository.save(reservationTime1);
        Long timeId2 = reservationTimeRepository.save(reservationTime2);
        Long themeId1 = repository.save(theme1);
        Long themeId2 = repository.save(theme2);
        repository.save(theme3);

        Member member = Member.createWithoutId("a", "a", "a", Role.USER);
        Long memberId = memberRepository.save(member);
        member = member.assignId(memberId);

        Reservation reservation1 = Reservation.createWithoutId(
                LocalDateTime.of(1999, 11, 2, 20, 10), member, LocalDate.of(2000, 11, 2),
                reservationTime1.assignId(timeId1), theme1.assignId(themeId1));
        Reservation reservation2 = Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member,
                LocalDate.of(2000, 11, 3),
                reservationTime2.assignId(timeId2), theme1.assignId(themeId1));
        Reservation reservation3 = Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member,
                LocalDate.of(2000, 11, 3),
                reservationTime1.assignId(timeId1), theme2.assignId(themeId2));
        Reservation reservation4 = Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member,
                LocalDate.of(2000, 11, 4),
                reservationTime1.assignId(timeId1), theme2.assignId(themeId2));
        reservationRepository.save(reservation1);
        reservationRepository.save(reservation2);
        reservationRepository.save(reservation3);
        reservationRepository.save(reservation4);

        ReservationPeriod period = new ReservationPeriod(LocalDate.of(2000, 11, 5), 2, 1);
        // when
        List<Theme> popularThemes = repository.findPopularThemes(period, 1);
        List<String> names = popularThemes.stream()
                .map(Theme::getName)
                .toList();
        // then
        assertThat(popularThemes).hasSize(1);
        assertThat(names).containsExactly("b");
    }
}
