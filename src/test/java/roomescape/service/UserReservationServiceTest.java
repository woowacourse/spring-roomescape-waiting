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
import roomescape.controller.dto.FindMyReservationResponse;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.ReservationTime;
import roomescape.global.exception.RoomescapeException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationTimeRepository;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@Sql(scripts = "/truncate.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class UserReservationServiceTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserReservationService userReservationService;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private MemberRepository memberRepository;

    private final LocalDate rawDate = LocalDate.parse("2060-01-01");

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
            VALUES ('테마1', 'd1', 'https://test.com/test1.jpg');
                        
            INSERT INTO reservation_time(start_at)
            VALUES ('08:00');
            """);
    }

    @DisplayName("성공: 예약을 저장하고, 해당 예약을 id값과 함께 반환한다.")
    @Test
    void save() {
        CreateReservationResponse saved = userReservationService.reserve(userId, rawDate, timeId, themeId);
        assertThat(saved.id()).isEqualTo(1L);
    }

    @DisplayName("실패: 존재하지 않는 멤버 ID 입력 시 예외가 발생한다.")
    @Test
    void save_MemberIdDoesntExist() {
        assertThatThrownBy(
            () -> userReservationService.reserve(3L, rawDate, timeId, themeId)
        ).isInstanceOf(RoomescapeException.class)
            .hasMessage("입력한 사용자 ID에 해당하는 데이터가 존재하지 않습니다.");
    }

    @DisplayName("실패: 존재하지 않는 시간 ID 입력 시 예외가 발생한다.")
    @Test
    void save_TimeIdDoesntExist() {
        assertThatThrownBy(
            () -> userReservationService.reserve(userId, rawDate, 2L, themeId)
        ).isInstanceOf(RoomescapeException.class)
            .hasMessage("입력한 시간 ID에 해당하는 데이터가 존재하지 않습니다.");
    }

    @DisplayName("실패: 중복 예약을 생성하면 예외가 발생한다.")
    @Test
    void save_Duplication() {
        userReservationService.reserve(userId, rawDate, timeId, themeId);

        assertThatThrownBy(
            () -> userReservationService.reserve(userId, rawDate, timeId, themeId)
        ).isInstanceOf(RoomescapeException.class)
            .hasMessage("해당 시간에 예약이 이미 존재합니다.");
    }

    @DisplayName("실패: 과거 날짜 예약 생성하면 예외 발생 -- 어제")
    @Test
    void save_PastDateReservation() {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        assertThatThrownBy(
            () -> userReservationService.reserve(userId, yesterday, timeId, themeId)
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
            () -> userReservationService.reserve(userId, today, savedTime.getId(), themeId)
        ).isInstanceOf(RoomescapeException.class)
            .hasMessage("과거 예약을 추가할 수 없습니다.");
    }

    @DisplayName("성공: 예약 대기")
    @Test
    void standby() {
        CreateReservationResponse saved = userReservationService.standby(userId, rawDate, timeId, themeId);
        assertThat(saved.id()).isEqualTo(1L);
    }

    @DisplayName("실패: 본인의 예약에 대기를 걸 수 없다.")
    @Test
    void standby_CantReserveAndThenStandbyForTheSameReservation() {
        userReservationService.reserve(userId, rawDate, timeId, themeId);

        assertThatThrownBy(() -> userReservationService.standby(userId, rawDate, timeId, themeId))
            .isInstanceOf(RoomescapeException.class)
            .hasMessage("이미 예약하셨습니다. 대기 없이 이용 가능합니다.");
    }

    @DisplayName("실패: 하나의 예약에 두 개 이상 대기를 걸 수 없다.")
    @Test
    void standby_CantStandbyMoreThanOnce() {
        userReservationService.reserve(adminId, rawDate, timeId, themeId);
        userReservationService.standby(userId, rawDate, timeId, themeId);

        assertThatThrownBy(() -> userReservationService.standby(userId, rawDate, timeId, themeId))
            .isInstanceOf(RoomescapeException.class)
            .hasMessage("이미 대기중인 예약입니다.");
    }

    @DisplayName("성공: 일반유저는 본인의 예약대기를 삭제할 수 있다.")
    @Test
    void deleteStandby() {
        userReservationService.reserve(adminId, rawDate, timeId, themeId);
        userReservationService.standby(userId, rawDate, timeId, themeId);
        Member user = memberRepository.findById(userId).get();

        assertThatCode(() -> userReservationService.deleteStandby(2L, user))
            .doesNotThrowAnyException();
    }

    @DisplayName("성공: 관리자는 다른 회원의 예약대기를 삭제할 수 있다.")
    @Test
    void deleteStandby_ByAdmin() {
        userReservationService.reserve(adminId, rawDate, timeId, themeId);
        userReservationService.standby(userId, rawDate, timeId, themeId);
        Member admin = memberRepository.findById(adminId).get();

        assertThatCode(() -> userReservationService.deleteStandby(2L, admin))
            .doesNotThrowAnyException();
    }

    @DisplayName("실패: 일반유저는 타인의 예약대기를 삭제할 수 없다.")
    @Test
    void deleteStandby_ReservedByOther() {
        userReservationService.reserve(userId, rawDate, timeId, themeId);
        userReservationService.standby(adminId, rawDate, timeId, themeId);
        Member user = memberRepository.findById(userId).get();

        assertThatThrownBy(() -> userReservationService.deleteStandby(2L, user))
            .isInstanceOf(RoomescapeException.class)
            .hasMessage("자신의 예약만 삭제할 수 있습니다.");
    }

    @DisplayName("실패: 예약대기 삭제 메서드로 예약을 삭제할 수 없다.")
    @Test
    void deleteStandby_Cannot_Delete_Reserved() {
        userReservationService.reserve(userId, rawDate, timeId, themeId);
        userReservationService.standby(adminId, rawDate, timeId, themeId);
        Member user = memberRepository.findById(userId).get();

        assertThatThrownBy(() -> userReservationService.deleteStandby(1L, user))
            .isInstanceOf(RoomescapeException.class)
            .hasMessage("예약대기가 존재하지 않아 삭제할 수 없습니다.");
    }

    @DisplayName("성공: 특정 멤버가 예약한 예약 및 예약대기 목록 조회")
    @Test
    void findAllWithRankByMemberId() {
        userReservationService.reserve(adminId, LocalDate.parse("2060-01-01"), timeId, themeId);
        userReservationService.standby(userId, LocalDate.parse("2060-01-01"), timeId, themeId);
        userReservationService.reserve(userId, LocalDate.parse("2060-01-02"), timeId, themeId);
        userReservationService.reserve(userId, LocalDate.parse("2060-01-03"), timeId, themeId);

        List<FindMyReservationResponse> reservations = userReservationService.findMyReservationsWithRank(userId);
        assertThat(reservations)
            .extracting(FindMyReservationResponse::id)
            .containsExactly(2L, 3L, 4L);
        assertThat(reservations)
            .extracting(FindMyReservationResponse::rank)
            .containsExactly(1L, 0L, 0L);
    }
}
