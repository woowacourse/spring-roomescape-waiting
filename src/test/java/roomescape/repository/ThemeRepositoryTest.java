package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;

@DataJpaTest
class ThemeRepositoryTest {

    Member member = new Member("seyang@test.com", "seyang", "Seyang");
    List<Theme> themes = IntStream.range(0, 5)
            .mapToObj(i -> new Theme("Theme" + i, "Desc" + i, "Thumb" + i))
            .toList();
    List<ReservationTime> times = IntStream.range(0, 5)
            .mapToObj(i -> new ReservationTime(LocalTime.of(8 + i, 10 * i)))
            .toList();
    List<Reservation> reservations;

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ThemeRepository themeRepository;

    @Autowired
    ReservationTimeRepository timeRepository;

    @BeforeEach
    void setUp() {
        LocalDate twoDaysAgo = LocalDate.now().minusDays(2);
        LocalDate yesterday = LocalDate.now().minusDays(1);

        member = memberRepository.save(member);
        themes = themes.stream().map(themeRepository::save).toList();
        times = times.stream().map(timeRepository::save).toList();

        reservations = Stream.of(
                        new Reservation(member, themes.get(0), twoDaysAgo, times.get(0)),
                        new Reservation(member, themes.get(0), twoDaysAgo, times.get(1)),

                        new Reservation(member, themes.get(1), yesterday, times.get(2)),
                        new Reservation(member, themes.get(1), yesterday, times.get(3)),
                        new Reservation(member, themes.get(1), yesterday, times.get(4)),

                        new Reservation(member, themes.get(2), yesterday, times.get(0)))
                .map(reservationRepository::save)
                .toList();
    }

    @ParameterizedTest
    @CsvSource(value = {"1,1", "1,2", "1,3", "2,1", "2,2", "2,3", "3,1", "3,2", "3,3"}, delimiter = ',')
    @DisplayName("시간 범위와 최대 개수를 통해 인기 테마를 조회한다.")
    void findPopularThemes(int days, int limit) {
        // given
        LocalDate yesterday = LocalDate.now().plusDays(1);
        LocalDate from = LocalDate.now().minusDays(days);

        // when
        List<Theme> actual = themeRepository.findPopularThemes(from, yesterday, limit);
        Map<Theme, Long> countByPopularTheme = reservations.stream()
                .filter(r -> r.getDate().isAfter(from) || r.getDate().isEqual(from))
                .filter(r -> r.getDate().isBefore(yesterday) || r.getDate().isEqual(yesterday))
                .map(Reservation::getTheme)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        List<Long> expectedDescendingCounts = actual.stream()
                .map(countByPopularTheme::get)
                .toList();

        // then
        assertThat(actual.size()).isLessThanOrEqualTo(limit);
        assertThat(expectedDescendingCounts).isSortedAccordingTo((a, b) -> Math.toIntExact(b - a));
    }
}