package roomescape.reservation.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.auth.sign.password.Password;
import roomescape.common.domain.Email;
import roomescape.common.exception.NotFoundException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.ui.dto.AvailableReservationTimeWebResponse;
import roomescape.reservation.ui.dto.CreateReservationWithUserIdWebRequest;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeDescription;
import roomescape.theme.domain.ThemeName;
import roomescape.theme.domain.ThemeRepository;
import roomescape.theme.domain.ThemeThumbnail;
import roomescape.time.domain.ReservationTime;
import roomescape.time.domain.ReservationTimeRepository;
import roomescape.user.domain.User;
import roomescape.user.domain.UserName;
import roomescape.user.domain.UserRepository;
import roomescape.user.domain.UserRole;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ReservationFacadeIntegrationTest {

    @Autowired
    private ReservationFacade reservationFacade;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationTimeRepository timeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private User user;
    private Theme theme;
    private ReservationTime time;

    @BeforeEach
    void setUp() {
        user = userRepository.save(
                User.withoutId(
                        UserName.from("테스트사용자"),
                        Email.from("test@example.com"),
                        Password.fromEncoded("encoded-password"),
                        UserRole.NORMAL
                )
        );

        theme = themeRepository.save(
                Theme.withoutId(
                        ThemeName.from("테스트테마"),
                        ThemeDescription.from("테마 설명"),
                        ThemeThumbnail.from("http://example.com/image.jpg")
                )
        );

        time = timeRepository.save(
                ReservationTime.withoutId(LocalTime.of(15, 0))
        );
    }

    @Test
    @DisplayName("특정 날짜와 테마의 사용 가능한 예약 시간을 조회할 수 있다")
    void getAvailable() {
        // given
        LocalDate targetDate = LocalDate.now().plusDays(1);

        ReservationTime anotherTime = timeRepository.save(
                ReservationTime.withoutId(LocalTime.of(16, 0))
        );

        reservationRepository.save(
                Reservation.withoutId(
                        user.getId(),
                        ReservationDate.from(targetDate),
                        time,
                        theme
                )
        );

        // when
        List<AvailableReservationTimeWebResponse> responses = reservationFacade.getAvailable(
                targetDate,
                theme.getId().getValue()
        );

        // then
        assertThat(responses).isNotEmpty();
        assertThat(responses)
                .anyMatch(response -> response.timeId().equals(anotherTime.getId().getValue()) && !response.isBooked())
                .anyMatch(response -> response.timeId().equals(time.getId().getValue()) && response.isBooked());
    }

    @Test
    @DisplayName("예약 생성 실패 후 데이터베이스 상태가 변경되지 않는지 확인한다 (트랜잭션 롤백)")
    void transactionRollbackVerification() {
        // given
        clearAllUsers();
        int userCount = countUsers();
        assertThat(userCount).isEqualTo(0);

        int initialReservationCount = countReservations();

        CreateReservationWithUserIdWebRequest request = new CreateReservationWithUserIdWebRequest(
                LocalDate.now().plusDays(1),
                time.getId().getValue(),
                theme.getId().getValue(),
                user.getId().getValue()
        );

        // when & then
        assertThatThrownBy(() -> reservationFacade.create(request))
                .isInstanceOf(NotFoundException.class);

        // 예약 수 변경 없음 확인 (트랜잭션 롤백 검증)
        assertThat(countReservations()).isEqualTo(initialReservationCount);
    }

    private int countReservations() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reservations", Integer.class);
    }

    private int countUsers() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
    }

    private void clearAllUsers() {
        jdbcTemplate.update("DELETE FROM reservations WHERE user_id = ?", user.getId().getValue());
        jdbcTemplate.update("DELETE FROM users");
    }
}
