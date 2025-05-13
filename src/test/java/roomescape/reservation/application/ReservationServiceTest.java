package roomescape.reservation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.NoSuchElementException;
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
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.infrastructure.dao.ReservationDao;
import roomescape.reservation.infrastructure.dao.ReservationTimeDao;
import roomescape.reservation.infrastructure.dao.ThemeDao;
import roomescape.reservation.presentation.dto.AdminReservationRequest;
import roomescape.reservation.presentation.dto.ReservationResponse;
import roomescape.reservation.presentation.dto.ReservationTimeRequest;
import roomescape.reservation.presentation.dto.ThemeRequest;

@JdbcTest
public class ReservationServiceTest {
    @Autowired
    private ReservationService reservationService;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private ThemeRepository themeRepository;

    @Test
    @DisplayName("예약 추가 테스트")
    void createReservationTest() {
        // given
        ReservationTimeRequest reservationTimeRequest = new ReservationTimeRequest(LocalTime.of(15, 40));
        ReservationTime reservationTime = reservationTimeRepository.insert(reservationTimeRequest.getStartAt());

        ThemeRequest themeRequest = new ThemeRequest("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg");
        Theme theme = themeRepository.insert(themeRequest);

        AdminReservationRequest adminReservationRequest = new AdminReservationRequest(
                LocalDate.of(2025, 8, 5),
                theme.getId(),
                reservationTime.getId(),
                2L
        );

        // when - then
        assertThatCode(() -> reservationService.createReservation(adminReservationRequest)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("예약 추가 시 예약 시간이 조회되지 않으면 예외가 발생한다")
    void createReservationExceptionTest() {
        // given
        ThemeRequest themeRequest = new ThemeRequest("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg");
        Theme theme = themeRepository.insert(themeRequest);

        AdminReservationRequest adminReservationRequest = new AdminReservationRequest(
                LocalDate.of(2025, 8, 5),
                theme.getId(),
                1L,
                2L
        );

        // when - then
        assertThatThrownBy(() -> reservationService.createReservation(adminReservationRequest))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("예약 시간 정보를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("예약 추가 시 테마가 조회되지 않으면 예외가 발생한다")
    void createThemeExceptionTest() {
        // given
        ReservationTimeRequest reservationTimeRequest = new ReservationTimeRequest(LocalTime.of(15, 40));
        ReservationTime reservationTime = reservationTimeRepository.insert(reservationTimeRequest.getStartAt());

        AdminReservationRequest adminReservationRequest = new AdminReservationRequest(
                LocalDate.of(2025, 8, 5),
                1L,
                reservationTime.getId(),
                2L
        );

        // when - then
        assertThatThrownBy(() -> reservationService.createReservation(adminReservationRequest))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("테마 정보를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("예약 전체 조회 테스트")
    void getReservationsTest() {
        // given
        ReservationTimeRequest reservationTimeRequest = new ReservationTimeRequest(LocalTime.of(15, 40));
        ReservationTime reservationTime = reservationTimeRepository.insert(reservationTimeRequest.getStartAt());

        ThemeRequest themeRequest = new ThemeRequest("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg");
        Theme theme = themeRepository.insert(themeRequest);

        AdminReservationRequest adminReservationRequest = new AdminReservationRequest(
                LocalDate.of(2025, 8, 5),
                theme.getId(),
                reservationTime.getId(),
                2L
        );
        reservationService.createReservation(adminReservationRequest);

        // when - then
        assertThat(reservationService.getReservations(null, null, null, null).size()).isEqualTo(1);
    }

    @Test
    @DisplayName("예약 삭제 테스트")
    void deleteReservationTest() {
        // given
        ReservationTimeRequest reservationTimeRequest = new ReservationTimeRequest(LocalTime.of(15, 40));
        ReservationTime reservationTime = reservationTimeRepository.insert(reservationTimeRequest.getStartAt());

        ThemeRequest themeRequest = new ThemeRequest("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg");
        Theme theme = themeRepository.insert(themeRequest);

        AdminReservationRequest adminReservationRequest = new AdminReservationRequest(
                LocalDate.of(2025, 8, 5),
                theme.getId(),
                reservationTime.getId(),
                2L
        );
        ReservationResponse reservation = reservationService.createReservation(adminReservationRequest);

        // when
        reservationService.deleteReservation(reservation.getId());

        // then
        assertThat(reservationService.getReservations(null, null, null, null).size()).isEqualTo(0);
    }

    @Test
    @DisplayName("저장되어 있지 않은 id로 요청을 보내면 예외가 발생한다.")
    void deleteExceptionTest() {
        assertThatThrownBy(() -> reservationService.deleteReservation(1L))
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
