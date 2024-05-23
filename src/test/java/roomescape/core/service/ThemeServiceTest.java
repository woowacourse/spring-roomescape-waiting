package roomescape.core.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.core.domain.Member;
import roomescape.core.domain.Reservation;
import roomescape.core.domain.ReservationTime;
import roomescape.core.domain.Theme;
import roomescape.core.dto.theme.ThemeRequest;
import roomescape.core.dto.theme.ThemeResponse;
import roomescape.core.repository.MemberRepository;
import roomescape.core.repository.ReservationRepository;
import roomescape.core.repository.ReservationTimeRepository;
import roomescape.core.repository.ThemeRepository;
import roomescape.utils.DatabaseCleaner;
import roomescape.utils.TestFixture;

@ServiceTest
class ThemeServiceTest {
    @Autowired
    private ThemeService themeService;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private TestFixture testFixture;

    private ThemeRequest request;

    @BeforeEach
    void setUp() {
        databaseCleaner.executeTruncate();
        testFixture.initTestData();

        request = new ThemeRequest("테스트", "테스트 테마입니다.", "test.jpg");
    }

    @Test
    @DisplayName("테마를 생성한다.")
    void create() {
        final ThemeResponse response = themeService.create(request);

        assertAll(
                () -> assertThat(response.getName()).isEqualTo(request.getName()),
                () -> assertThat(response.getDescription()).isEqualTo(request.getDescription()),
                () -> assertThat(response.getThumbnail()).isEqualTo(request.getThumbnail())
        );
    }

    @Test
    @DisplayName("이름이 중복된 테마를 생성할 시, 예외가 발생한다.")
    void createWithDuplicatedName() {
        themeService.create(request);

        assertThatThrownBy(() -> themeService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ThemeService.THEME_NAME_DUPLICATED_EXCEPTION_MESSAGE);
    }

    @Test
    @DisplayName("모든 테마를 조회한다.")
    void findAll() {
        assertThat(themeService.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("지난 한 주의 인기 테마를 인기순으로 조회한다.")
    void findPopularTheme() {
        themeService.create(request);
        createReservations();

        final List<ThemeResponse> responses = themeService.findPopularTheme();

        assertThat(responses.get(0).getName()).isEqualTo("테스트");
    }

    private void createReservations() {
        final ReservationTime reservationTimeAfterOneMinute = testFixture.persistReservationTimeAfterMinute(1);
        final Reservation oneMinuteAfterReservation = new Reservation(getMember(), TestFixture.getTodayDate(),
                reservationTimeAfterOneMinute, getTheme());

        final ReservationTime reservationTimeAfterTwoMinute = testFixture.persistReservationTimeAfterMinute(2);
        final Reservation twoMinuteAfterReservation = new Reservation(getMember(), TestFixture.getTodayDate(),
                reservationTimeAfterTwoMinute, getTheme());

        reservationRepository.save(oneMinuteAfterReservation);
        reservationRepository.save(twoMinuteAfterReservation);
    }

    private Member getMember() {
        return memberRepository.findByEmail(TestFixture.getEmail());
    }

    private Theme getTheme() {
        return themeRepository.findById(2L).orElseThrow(IllegalArgumentException::new);
    }

    @Test
    @DisplayName("테마를 삭제한다.")
    void delete() {
        final ThemeResponse response = themeService.create(request);

        themeService.delete(response.getId());

        assertThat(themeService.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("테마를 삭제 시, 예약 내역이 존재하면 예외가 발생한다.")
    void deleteBookedTheme() {
        final ThemeResponse response = themeService.create(request);
        final Long themeId = response.getId();

        final ReservationTime reservationTime = testFixture.persistReservationTimeAfterMinute(1);
        final Reservation reservation = new Reservation(getMember(), TestFixture.getTodayDate(),
                reservationTime, themeRepository.findById(themeId).orElseThrow());
        reservationRepository.save(reservation);

        assertThatThrownBy(() -> themeService.delete(themeId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ThemeService.BOOKED_THEME_DELETE_EXCEPTION_MESSAGE);
    }
}