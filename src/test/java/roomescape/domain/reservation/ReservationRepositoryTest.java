package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.domain.BaseRepositoryTest;
import roomescape.domain.member.Member;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.support.fixture.MemberFixture;
import roomescape.support.fixture.ReservationFixture;
import roomescape.support.fixture.ReservationTimeFixture;
import roomescape.support.fixture.ThemeFixture;

class ReservationRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    private Member member;

    private Theme theme;

    private ReservationTime time;

    @BeforeEach
    void setUp() {
        member = save(MemberFixture.create());
        theme = save(ThemeFixture.create());
        time = save(ReservationTimeFixture.create());
    }

    @Test
    @DisplayName("조건에 해당하는 모든 예약들을 조회한다.")
    void findAll() {
        save(ReservationFixture.create("2024-04-09", member, time, theme));
        save(ReservationFixture.create("2024-04-10", member, time, theme));
        save(ReservationFixture.create("2024-04-11", member, time, theme));

        List<Reservation> reservations = reservationRepository.findAllByConditions(
                member.getId(),
                theme.getId(),
                LocalDate.of(2024, 4, 9),
                LocalDate.of(2024, 4, 10)
        );

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(reservations).hasSize(2);
            softly.assertThat(reservations.get(0).getDate()).isEqualTo("2024-04-09");
            softly.assertThat(reservations.get(1).getDate()).isEqualTo("2024-04-10");
        });
    }

    @Test
    @DisplayName("예약 시간 id에 해당하는 예약이 존재하는지 확인한다.")
    void existsByTimeId() {
        save(ReservationFixture.create(member, time, theme));

        assertThat(reservationRepository.existsByTimeId(time.getId())).isTrue();
    }

    @Test
    @DisplayName("테마 id에 해당하는 예약이 존재하는지 확인한다.")
    void existsByThemeId() {
        save(ReservationFixture.create(member, time, theme));

        assertThat(reservationRepository.existsByThemeId(theme.getId())).isTrue();
    }

    @Test
    @DisplayName("날짜와 시간 id, 테마 id에 해당하는 예약이 존재하면 true를 반환한다.")
    void existsByValidReservation() {
        String date = "2024-04-09";
        save(ReservationFixture.create(date, member, time, theme));

        assertThat(reservationRepository.existsByDateAndTimeIdAndThemeId(LocalDate.parse(date), time.getId(),
                theme.getId())).isTrue();
    }

    @ParameterizedTest
    @CsvSource({"2024-04-10, 1, 1", "2024-04-09, 1, 2", "2024-04-09, 2, 1"})
    @DisplayName("날짜와 시간 id, 테마 id에 해당하는 예약이 존재하지 않으면 false를 반환한다.")
    void existsByInvalidReservation(LocalDate date, Long timeId, Long themeId) {
        save(ReservationFixture.create("2024-04-09", member, time, theme));

        assertThat(reservationRepository.existsByDateAndTimeIdAndThemeId(date, timeId, themeId)).isFalse();
    }
}
