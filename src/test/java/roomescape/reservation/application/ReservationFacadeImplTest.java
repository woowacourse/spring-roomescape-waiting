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
import roomescape.reservation.ui.dto.ReservationResponse;
import roomescape.reservation.ui.dto.ReservationSearchWebRequest;
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
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
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
    @DisplayName("모든 예약을 조회할 수 있다")
    void getAll() {
        // given
        Reservation reservation1 = reservationRepository.save(
                Reservation.withoutId(
                        user.getId(),
                        ReservationDate.from(LocalDate.now().plusDays(1)),
                        time,
                        theme
                )
        );

        User anotherUser = userRepository.save(
                User.withoutId(
                        UserName.from("다른사용자"),
                        Email.from("another@example.com"),
                        Password.fromEncoded("encoded-password"),
                        UserRole.NORMAL
                )
        );

        Reservation reservation2 = reservationRepository.save(
                Reservation.withoutId(
                        anotherUser.getId(),
                        ReservationDate.from(LocalDate.now().plusDays(2)),
                        time,
                        theme
                )
        );

        // when
        List<ReservationResponse> responses = reservationFacade.getAll();

        // then
        assertAll(
                () -> assertThat(responses).hasSize(2),
                () -> assertThat(responses)
                        .extracting(ReservationResponse::reservationId)
                        .containsExactly(
                                reservation1.getId().getValue(),
                                reservation2.getId().getValue()
                        ),
                () -> assertThat(responses)
                        .extracting(response -> response.user().id())
                        .containsExactlyInAnyOrder(
                                user.getId().getValue(),
                                anotherUser.getId().getValue()
                        )
        );
    }

    @Test
    @DisplayName("빈 예약 목록을 조회할 수 있다")
    void getAllWhenEmpty() {
        // when
        List<ReservationResponse> responses = reservationFacade.getAll();

        // then
        assertThat(responses).isEmpty();
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
                .anyMatch(response -> response.timeId().equals(anotherTime.getId().getValue()) && response.isBooked() == false)
                .anyMatch(response -> response.timeId().equals(time.getId().getValue()) && response.isBooked() == true);
    }

    @Test
    @DisplayName("사용자 ID로 해당 사용자의 모든 예약을 조회할 수 있다")
    void getAllByUserId() {
        // given
        Reservation reservation1 = reservationRepository.save(
                Reservation.withoutId(
                        user.getId(),
                        ReservationDate.from(LocalDate.now().plusDays(1)),
                        time,
                        theme
                )
        );

        Reservation reservation2 = reservationRepository.save(
                Reservation.withoutId(
                        user.getId(),
                        ReservationDate.from(LocalDate.now().plusDays(2)),
                        time,
                        theme
                )
        );

        User anotherUser = userRepository.save(
                User.withoutId(
                        UserName.from("다른사용자"),
                        Email.from("another@example.com"),
                        Password.fromEncoded("encoded-password"),
                        UserRole.NORMAL
                )
        );

        reservationRepository.save(
                Reservation.withoutId(
                        anotherUser.getId(),
                        ReservationDate.from(LocalDate.now().plusDays(3)),
                        time,
                        theme
                )
        );

        // when
        List<ReservationResponse> responses = reservationFacade.getAllByUserId(user.getId().getValue());

        // then
        assertAll(
                () -> assertThat(responses).hasSize(2),
                () -> assertThat(responses)
                        .extracting(ReservationResponse::reservationId)
                        .containsExactly(
                                reservation1.getId().getValue(),
                                reservation2.getId().getValue()
                        ),
                () -> assertThat(responses)
                        .allMatch(response -> response.user().id().equals(user.getId().getValue()))
        );
    }

    @Test
    @DisplayName("존재하지 않는 사용자 ID로 예약 조회 시 예외가 발생한다")
    void getAllByUserIdWithNonExistentUserId() {
        // given
        Long nonExistentUserId = 9999L;

        // when & then
        assertThatThrownBy(() -> reservationFacade.getAllByUserId(nonExistentUserId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("[USER] not found");
    }

    @Test
    @DisplayName("검색 조건으로 예약을 조회할 수 있다")
    void getByParams() {
        // given
        LocalDate targetDate = LocalDate.now().plusDays(1);

        Reservation reservation = reservationRepository.save(
                Reservation.withoutId(
                        user.getId(),
                        ReservationDate.from(targetDate),
                        time,
                        theme
                )
        );

        reservationRepository.save(
                Reservation.withoutId(
                        user.getId(),
                        ReservationDate.from(targetDate.plusDays(10)),
                        time,
                        theme
                )
        );

        ReservationSearchWebRequest searchRequest = new ReservationSearchWebRequest(
                theme.getId().getValue(),
                user.getId().getValue(),
                targetDate,
                targetDate
        );
        // when
        List<ReservationResponse> responses = reservationFacade.getByParams(searchRequest);
        // then
        assertAll(
                () -> assertThat(responses).hasSize(1),
                () -> assertThat(responses.getFirst().reservationId()).isEqualTo(reservation.getId().getValue()),
                () -> assertThat(responses.getFirst().user().id()).isEqualTo(user.getId().getValue()),
                () -> assertThat(responses.getFirst().theme().id()).isEqualTo(theme.getId().getValue())
        );
    }

    @Test
    @DisplayName("존재하지 않는 테마 ID로 예약 생성 시도 시 예외가 발생한다")
    void createWithNonExistentThemeId() {
        // given
        Long nonExistentThemeId = 9999L;

        CreateReservationWithUserIdWebRequest request = new CreateReservationWithUserIdWebRequest(
                LocalDate.now().plusDays(1),
                time.getId().getValue(),
                nonExistentThemeId,
                user.getId().getValue()
        );

        int initialCount = countReservations();

        // when & then
        assertThatThrownBy(() -> reservationFacade.create(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("[THEME] not found");

        assertThat(countReservations()).isEqualTo(initialCount);
    }

    @Test
    @DisplayName("존재하지 않는 시간 ID로 예약 생성 시도 시 예외가 발생한다")
    void createWithNonExistentTimeId() {
        // given
        Long nonExistentTimeId = 9999L;

        CreateReservationWithUserIdWebRequest request = new CreateReservationWithUserIdWebRequest(
                LocalDate.now().plusDays(1),
                nonExistentTimeId,
                theme.getId().getValue(),
                user.getId().getValue()
        );

        // when & then
        assertThatThrownBy(() -> reservationFacade.create(request))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("존재하지 않는 예약 ID로 삭제 시도 시 예외가 발생한다")
    void deleteWithNonExistentReservationId() {
        // given
        Long nonExistentReservationId = 9999L;
        int initialCount = countReservations();

        // when & then
        assertThatThrownBy(() -> reservationFacade.delete(nonExistentReservationId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("[RESERVATION] not found");

        assertThat(countReservations()).isEqualTo(initialCount);
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
