package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.controller.dto.CreateReservationResponse;
import roomescape.controller.dto.FindReservationResponse;
import roomescape.controller.dto.FindReservationStandbyResponse;
import roomescape.domain.reservation.ReservationTime;
import roomescape.global.exception.RoomescapeException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationTimeRepository;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@Sql(scripts = "/truncate.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class AdminReservationServiceTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AdminReservationService adminReservationService;

    @Autowired
    private UserReservationService userReservationService;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private MemberRepository memberRepository;

    private final LocalDate date = LocalDate.parse("2060-01-01");

    private final Long timeId = 1L;
    private final Long themeId = 1L;
    private final Long userId = 1L;
    private final Long adminId = 2L;

    @BeforeEach
    void setUpData() {
        jdbcTemplate.update("""
            INSERT INTO member(name, email, password, role)
            VALUES ('러너덕', 'user@a.com', '123a!', 'USER'),
                   ('트레', 'tre@a.com', '123a!', 'ADMIN');
                        
            INSERT INTO theme(name, description, thumbnail)
            VALUES ('테마1', 'd1', 'https://test.com/test1.jpg'),
                   ('테마1', 'd1', 'https://test.com/test1.jpg');
                        
            INSERT INTO reservation_time(start_at)
            VALUES ('08:00');
            """);
    }

    @DisplayName("성공: 예약을 저장하고, 해당 예약을 id값과 함께 반환한다.")
    @Test
    void save() {
        CreateReservationResponse saved = adminReservationService.reserve(userId, date, timeId, themeId);
        assertThat(saved.id()).isEqualTo(1L);
    }

    @DisplayName("실패: 존재하지 않는 멤버 ID 입력 시 예외가 발생한다.")
    @Test
    void save_MemberIdDoesntExist() {
        assertThatThrownBy(
            () -> adminReservationService.reserve(3L, date, timeId, themeId)
        ).isInstanceOf(RoomescapeException.class)
            .hasMessage("입력한 사용자 ID에 해당하는 데이터가 존재하지 않습니다.");
    }

    @DisplayName("실패: 존재하지 않는 시간 ID 입력 시 예외가 발생한다.")
    @Test
    void save_TimeIdDoesntExist() {
        assertThatThrownBy(
            () -> adminReservationService.reserve(userId, date, 2L, themeId)
        ).isInstanceOf(RoomescapeException.class)
            .hasMessage("입력한 시간 ID에 해당하는 데이터가 존재하지 않습니다.");
    }

    @DisplayName("실패: 중복 예약을 생성하면 예외가 발생한다.")
    @Test
    void save_Duplication() {
        adminReservationService.reserve(userId, date, timeId, themeId);

        assertThatThrownBy(
            () -> adminReservationService.reserve(userId, date, timeId, themeId)
        ).isInstanceOf(RoomescapeException.class)
            .hasMessage("해당 시간에 예약이 이미 존재합니다.");
    }

    @DisplayName("실패: 과거 날짜 예약 생성하면 예외 발생 -- 어제")
    @Test
    void save_PastDateReservation() {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        assertThatThrownBy(
            () -> adminReservationService.reserve(userId, yesterday, timeId, themeId)
        ).isInstanceOf(RoomescapeException.class)
            .hasMessage("과거 예약을 추가할 수 없습니다.");
    }

    @DisplayName("실패: 같은 날짜, 과거 시간 예약 생성하면 예외 발생 -- 1분 전")
    @Test
    void save_TodayPastTimeReservation() {
        LocalDate today = LocalDate.now();
        String oneMinuteAgo = LocalTime.now().minusMinutes(1).toString();

        ReservationTime savedTime = reservationTimeRepository.save(new ReservationTime(oneMinuteAgo));

        assertThatThrownBy(
            () -> adminReservationService.reserve(userId, today, savedTime.getId(), themeId)
        ).isInstanceOf(RoomescapeException.class)
            .hasMessage("과거 예약을 추가할 수 없습니다.");
    }

    @DisplayName("성공: 예약 삭제")
    @Test
    void deleteById() {
        adminReservationService.reserve(adminId, LocalDate.parse("2060-01-01"), timeId, themeId);
        adminReservationService.reserve(adminId, LocalDate.parse("2060-01-02"), timeId, themeId);
        adminReservationService.reserve(adminId, LocalDate.parse("2060-01-03"), timeId, themeId);

        adminReservationService.deleteById(2L);

        assertThat(adminReservationService.findAllReserved())
            .extracting(FindReservationResponse::id)
            .containsExactly(1L, 3L);
    }

    @DisplayName("성공: 다른 회원의 예약대기 삭제")
    @Test
    void deleteStandby_ByAdmin() {
        adminReservationService.reserve(adminId, date, timeId, themeId);
        userReservationService.standby(userId, date, timeId, themeId);

        assertThatCode(() -> adminReservationService.deleteStandby(2L))
            .doesNotThrowAnyException();
    }

    @DisplayName("성공: 전체 예약 조회")
    @Test
    void findAllReserved() {
        adminReservationService.reserve(adminId, LocalDate.parse("2060-01-01"), timeId, themeId);
        adminReservationService.reserve(userId, LocalDate.parse("2060-01-02"), timeId, themeId);
        adminReservationService.reserve(adminId, LocalDate.parse("2060-01-03"), timeId, themeId);

        assertThat(adminReservationService.findAllReserved())
            .extracting(FindReservationResponse::id)
            .containsExactly(1L, 2L, 3L);
    }

    @DisplayName("성공: 전체 대기 조회")
    @Test
    void findAllStandby() {
        adminReservationService.reserve(adminId, date, timeId, themeId);
        userReservationService.standby(userId, date, timeId, themeId);

        assertThat(adminReservationService.findAllStandby())
            .extracting(FindReservationStandbyResponse::id)
            .containsExactly(2L);
    }

    @DisplayName("성공: 검색 필터에 따라 조회")
    @Test
    void findAllByFilter() {
        adminReservationService.reserve(adminId, LocalDate.parse("2060-01-01"), 1L, 1L);
        adminReservationService.reserve(adminId, LocalDate.parse("2060-01-02"), 1L, 2L);
        adminReservationService.reserve(adminId, LocalDate.parse("2060-01-03"), 1L, 1L);
        adminReservationService.reserve(userId, LocalDate.parse("2060-01-04"), 1L, 2L);
        adminReservationService.reserve(userId, LocalDate.parse("2060-01-05"), 1L, 1L);
        adminReservationService.reserve(userId, LocalDate.parse("2060-01-06"), 1L, 2L);

        List<FindReservationResponse> reservations = adminReservationService.findAllByFilter(
            2L, userId, LocalDate.parse("2060-01-01"), LocalDate.parse("2060-01-06"));

        assertThat(reservations)
            .extracting(FindReservationResponse::id)
            .containsExactly(4L, 6L);
    }
}
