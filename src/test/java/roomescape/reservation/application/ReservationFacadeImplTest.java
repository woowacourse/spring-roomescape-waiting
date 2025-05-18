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
import roomescape.reservation.ui.dto.CreateReservationWithUserIdWebRequest;
import roomescape.reservation.ui.dto.ReservationResponse;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationFacadeImplTest {

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
    @DisplayName("예약 생성을 성공적으로 처리된다")
    void create() {
        // given
        final CreateReservationWithUserIdWebRequest request = new CreateReservationWithUserIdWebRequest(
                LocalDate.now().plusDays(1),
                time.getId().getValue(),
                theme.getId().getValue(),
                user.getId().getValue()
        );

        int initialCount = countReservations();

        // when
        ReservationResponse response = reservationFacade.create(request);

        // then
        assertAll(
                () -> assertThat(response).isNotNull(),
                () -> assertThat(response.reservationId()).isNotNull(),
                () -> assertThat(response.user().id()).isEqualTo(user.getId().getValue()),
                () -> assertThat(response.theme().id()).isEqualTo(theme.getId().getValue()),
                () -> assertThat(countReservations()).isEqualTo(initialCount + 1)
        );
    }

    @Test
    @DisplayName("존재하지 않는 사용자 ID로 예약 생성 시도 시 예외가 발생된다")
    void createWithNonExistentUserId() {
        // given
        final Long nonExistentUserId = 9999L;

        final CreateReservationWithUserIdWebRequest request = new CreateReservationWithUserIdWebRequest(
                LocalDate.now().plusDays(1),
                time.getId().getValue(),
                theme.getId().getValue(),
                nonExistentUserId
        );

        int initialCount = countReservations();

        // when & then
        assertThatThrownBy(() -> reservationFacade.create(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("[USER] not found. params={UserId=UserId(9999)}");

        assertThat(countReservations()).isEqualTo(initialCount);
    }

    @Test
    @DisplayName("예약 생성 실패 후 데이터베이스 상태가 변경되지 않는지 확인한다")
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

    @Test
    @DisplayName("예약 삭제가 성공적으로 처리된다")
    void delete() {
        // given
        Reservation reservation = reservationRepository.save(
                Reservation.withoutId(
                        user.getId(),
                        ReservationDate.from(LocalDate.now().plusDays(1)),
                        time,
                        theme
                )
        );

        int initialCount = countReservations();
        assertThat(initialCount).isPositive();

        // when
        reservationFacade.delete(reservation.getId().getValue());

        // then
        assertThat(countReservations()).isEqualTo(initialCount - 1);
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
