package roomescape.reservation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.member.application.repository.MemberRepository;
import roomescape.member.infrastructure.dao.MemberDao;
import roomescape.reservation.application.repository.ReservationRepository;
import roomescape.reservation.application.repository.ReservationTimeRepository;
import roomescape.reservation.application.repository.ThemeRepository;
import roomescape.reservation.application.service.ReservationService;
import roomescape.reservation.application.service.ReservationTimeService;
import roomescape.reservation.application.service.ThemeService;
import roomescape.reservation.infrastructure.dao.ReservationDao;
import roomescape.reservation.infrastructure.dao.ReservationTimeDao;
import roomescape.reservation.infrastructure.dao.ThemeDao;
import roomescape.reservation.presentation.dto.AdminReservationRequest;
import roomescape.reservation.presentation.dto.AvailableReservationTimeResponse;
import roomescape.reservation.presentation.dto.ReservationTimeRequest;
import roomescape.reservation.presentation.dto.ReservationTimeResponse;
import roomescape.reservation.presentation.dto.ThemeRequest;
import roomescape.reservation.presentation.dto.ThemeResponse;

@JdbcTest
public class ReservationTimeServiceTest {

    @Autowired
    private ReservationTimeService reservationTimeService;
    @Autowired
    private ReservationService reservationService;
    @Autowired
    private ThemeService themeService;

    @Test
    @DisplayName("예약 시간 추가 테스트")
    void createReservationTimeTest() {
        // given
        ReservationTimeRequest reservationTimeRequest = new ReservationTimeRequest(LocalTime.of(15, 40));

        // when
        ReservationTimeResponse reservationTime = reservationTimeService.createReservationTime(reservationTimeRequest);

        // then
        assertThat(reservationTime.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("동일한 시간으로 예약 시간을 중복 등록할 경우 예외가 발생한다.")
    void createDuplicateReservationTimeTest() {
        // given
        LocalTime duplicateTime = LocalTime.of(15, 40);
        ReservationTimeRequest request = new ReservationTimeRequest(duplicateTime);
        reservationTimeService.createReservationTime(request);

        // when - then
        assertThatThrownBy(() -> reservationTimeService.createReservationTime(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("중복된 시간은 추가할 수 없습니다.");
    }

    @Test
    @DisplayName("예약 시간 전체 조회 테스트")
    void getReservationTimesTest() {
        // given
        ReservationTimeRequest reservationTimeRequest = new ReservationTimeRequest(LocalTime.of(15, 40));
        reservationTimeService.createReservationTime(reservationTimeRequest);

        // when - then
        assertThat(reservationTimeService.getReservationTimes().size()).isEqualTo(1);
    }

    @Test
    @DisplayName("예약 가능 시간 조회 테스트")
    void getAvailableReservationTimesTest() {
        // given
        ReservationTimeRequest availableReservationTimeRequest = new ReservationTimeRequest(LocalTime.of(15, 40));
        ReservationTimeRequest unAvailableReservationTimeRequest = new ReservationTimeRequest(LocalTime.of(15, 50));
        ReservationTimeResponse reservationTime = reservationTimeService.createReservationTime(
                availableReservationTimeRequest);
        reservationTimeService.createReservationTime(unAvailableReservationTimeRequest);

        ThemeRequest themeRequest = new ThemeRequest(
                "레벨2 탈출",
                "우테코 레벨2를 탈출하는 내용입니다.",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
        );
        themeService.createTheme(themeRequest);

        AdminReservationRequest adminReservationRequest = new AdminReservationRequest(
                LocalDate.of(2025, 8, 5),
                1L,
                reservationTime.getId(),
                2L
        );
        reservationService.createReservation(adminReservationRequest);

        // when
        List<AvailableReservationTimeResponse> reservationTimes = reservationTimeService.getReservationTimes(
                LocalDate.of(2025, 8, 5), 1L);

        // then
        assertThat(reservationTimes.stream()
                .filter(AvailableReservationTimeResponse::isAlreadyBooked)
                .count())
                .isEqualTo(1);
    }

    @Test
    @DisplayName("예약 시간 삭제 테스트")
    void deleteReservationTimeTest() {
        // given
        ReservationTimeRequest reservationTimeRequest = new ReservationTimeRequest(LocalTime.of(15, 40));
        ReservationTimeResponse reservationTime = reservationTimeService.createReservationTime(reservationTimeRequest);

        // when
        reservationTimeService.deleteReservationTime(reservationTime.getId());

        // then
        assertThat(reservationTimeService.getReservationTimes().size()).isEqualTo(0);
    }

    @Test
    @DisplayName("예약이 존재하는 시간을 삭제할 경우 예외가 발생한다.")
    void deleteReservationTimeWithExistingReservationTest() {
        // given
        ReservationTimeRequest timeRequest = new ReservationTimeRequest(LocalTime.of(15, 40));
        ReservationTimeResponse reservationTime = reservationTimeService.createReservationTime(timeRequest);

        ThemeRequest themeRequest = new ThemeRequest(
                "레벨2 탈출",
                "우테코 레벨2를 탈출하는 내용입니다.",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
        );
        ThemeResponse theme = themeService.createTheme(themeRequest);

        AdminReservationRequest adminReservationRequest = new AdminReservationRequest(
                LocalDate.of(2025, 8, 5),
                theme.getId(),
                reservationTime.getId(),
                2L
        );
        reservationService.createReservation(adminReservationRequest);

        // when - then
        assertThatThrownBy(() -> reservationTimeService.deleteReservationTime(reservationTime.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("예약이 이미 존재하는 시간입니다.");
    }

    @Test
    @DisplayName("저장되어 있지 않은 id로 요청을 보내면 예외가 발생한다.")
    void deleteExceptionTest() {
        assertThatThrownBy(() -> reservationTimeService.deleteReservationTime(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 삭제되어 있는 리소스입니다.");
    }


    @TestConfiguration
    static class TestConfig {
        @Bean
        public ThemeRepository themeRepository(
                final JdbcTemplate jdbcTemplate
        ) {
            return new ThemeDao(jdbcTemplate) {
            };
        }

        @Bean
        public ReservationRepository reservationRepository(
                final JdbcTemplate jdbcTemplate
        ) {
            return new ReservationDao(jdbcTemplate) {
            };
        }

        @Bean
        public ReservationTimeRepository reservationTimeRepository(
                final JdbcTemplate jdbcTemplate
        ) {
            return new ReservationTimeDao(jdbcTemplate) {
            };
        }

        @Bean
        public MemberRepository memberRepository(
                final JdbcTemplate jdbcTemplate
        ) {
            return new MemberDao(jdbcTemplate) {
            };
        }

        @Bean
        public ThemeService themeService(
                final ReservationRepository reservationRepository,
                final ThemeRepository themeRepository
        ) {
            return new ThemeService(reservationRepository, themeRepository);
        }

        @Bean
        public ReservationTimeService reservationTimeService(
                final ReservationRepository reservationRepository,
                final ReservationTimeRepository reservationTimeRepository
        ) {
            return new ReservationTimeService(reservationTimeRepository, reservationRepository);
        }

        @Bean
        public ReservationService reservationService(
                final ReservationRepository reservationRepository,
                final ReservationTimeRepository reservationTimeRepository,
                final ThemeRepository themeRepository,
                final MemberRepository memberRepository
        ) {
            return new ReservationService(reservationRepository, reservationTimeRepository, themeRepository,
                    memberRepository);
        }
    }

}
