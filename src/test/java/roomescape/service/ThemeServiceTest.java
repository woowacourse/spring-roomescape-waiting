package roomescape.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;
import roomescape.domain.user.Member;
import roomescape.exception.ExistReservationException;
import roomescape.fixture.MemberFixture;
import roomescape.fixture.ReservationTimeFixture;
import roomescape.fixture.ThemeFixture;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.dto.input.ThemeInput;
import roomescape.service.dto.output.ThemeOutput;
import roomescape.util.DatabaseCleaner;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class ThemeServiceTest {

    @Autowired
    ThemeService sut;
    @Autowired
    ThemeRepository themeRepository;
    @Autowired
    ReservationTimeRepository reservationTimeRepository;
    @Autowired
    ReservationRepository reservationRepository;
    @Autowired
    MemberRepository memberRepository;

    @Autowired
    DatabaseCleaner databaseCleaner;

    @BeforeEach
    void setUp() {
        databaseCleaner.initialize();
    }

    @Test
    @DisplayName("유효한 값을 입력하면 예외를 발생하지 않는다.")
    void create_reservationTime() {
        final ThemeInput input = new ThemeInput(
                "레벨2 탈출",
                "우테코 레벨2를 탈출하는 내용입니다.",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
        );
        assertThatCode(() -> sut.createTheme(input))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("유효하지 않은 값을 입력하면 예외를 발생한다.")
    void throw_exception_when_input_is_invalid() {
        final ThemeInput input = new ThemeInput(
                "",
                "우테코 레벨2를 탈출하는 내용입니다.",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
        );
        assertThatThrownBy(() -> sut.createTheme(input))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("모든 요소를 받아온다.")
    void get_all_themes() {
        final ThemeInput input = new ThemeInput(
                "레벨2 탈출",
                "우테코 레벨2를 탈출하는 내용입니다.",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
        );
        sut.createTheme(input);

        final var result = sut.getAllThemes();
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("특정 테마에 대한 예약이 존재하면 예외를 발생한다.")
    void throw_exception_when_delete_id_that_exist_reservation() {
        final Theme theme = themeRepository.save(ThemeFixture.getDomain());
        final ReservationTime reservationTime = reservationTimeRepository.save(ReservationTimeFixture.getDomain());
        final Member member = memberRepository.save(MemberFixture.getDomain());
        reservationRepository.save(Reservation.from(
                "2024-04-30",
                reservationTime,
                theme,
                member
        ));
        final var themeId = theme.getId();

        assertThatThrownBy(() -> sut.deleteTheme(themeId))
                .isInstanceOf(ExistReservationException.class);
    }

    @Test
    @DisplayName("예약이 많은 테마 순으로 조회한다.")
    void get_popular_themes() {
        final Theme theme = themeRepository.save(ThemeFixture.getDomain());
        final Theme theme1 = themeRepository.save(Theme.of(
                "레벨3 탈출",
                "우테코 레벨2를 탈출하는 내용입니다.",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
        ));
        final ReservationTime reservationTime = reservationTimeRepository.save(ReservationTimeFixture.getDomain());
        final Member member = memberRepository.save(MemberFixture.getDomain());

        reservationRepository.save(Reservation.from(
                "2024-06-01",
                reservationTime,
                theme,
                member
        ));
        reservationRepository.save(Reservation.from(
                "2024-06-02",
                reservationTime,
                theme,
                member
        ));
        reservationRepository.save(Reservation.from(

                "2024-06-03",
                reservationTime,
                theme1,
                member
        ));

        final List<ThemeOutput> popularThemes = sut.getPopularThemes(LocalDate.parse("2024-06-04"));

        assertThat(popularThemes).containsExactly(
                ThemeOutput.toOutput(theme),
                ThemeOutput.toOutput(theme1)
        );
    }
}
