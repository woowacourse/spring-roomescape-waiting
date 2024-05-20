package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.model.member.Member;
import roomescape.model.theme.Name;
import roomescape.model.theme.Theme;

@Sql("/init.sql")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class ThemeRepositoryTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ThemeRepository themeRepository;

    @BeforeEach
    void setUp() {
        reservationTimeRepository.saveAll(List.of(
                new ReservationTime(LocalTime.of(1, 0)),
                new ReservationTime(LocalTime.of(2, 0))));

        reservationRepository.saveAll(List.of(
                new Reservation(
                        LocalDate.of(2000, 1, 1),
                        new ReservationTime(1, null),
                        new Theme(1, null, null, null),
                        new Member(1, null, null, null, null)),
                new Reservation(LocalDate.of(2000, 1, 2),
                        new ReservationTime(2, null),
                        new Theme(2, null, null, null),
                        new Member(2, null, null, null, null))));
    }

    @DisplayName("두 날짜 사이의 예약을 테마의 개수로 내림차순 정렬하여, 특정 개수의 테마를 조회한다.")
    @Test
    void should_find_theme_ranking_by_date() {
        List<Theme> themes = themeRepository.findRankingByDate(
                LocalDate.of(2000, 1, 1), LocalDate.of(2000, 1, 2), 10);
        assertAll(
                () -> assertThat(themes).hasSizeLessThanOrEqualTo(10),
                () -> assertThat(themes).containsExactly(
                        new Theme(1, "n1", "d1", "t1"),
                        new Theme(2, "n2", "d2", "t2")));
    }

    @DisplayName("특정 이름을 가진 테마가 존재할 경우 참을 반환한다.")
    @Test
    void should_return_true_when_exist_name() {
        boolean isExist = themeRepository.existsByName(new Name("n1"));
        assertThat(isExist).isTrue();
    }

    @DisplayName("특정 이름을 가진 테마가 존재하지 않을 경우 거짓을 반환한다.")
    @Test
    void should_return_false_when_not_exist_name() {
        boolean isExist = themeRepository.existsByName(new Name("n0"));
        assertThat(isExist).isFalse();
    }
}
