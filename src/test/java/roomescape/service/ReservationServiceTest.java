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
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.global.exception.RoomescapeException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.service.dto.FindReservationWithRankDto;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@Sql(scripts = "/truncate.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class ReservationServiceTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ReservationService reservationService;

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
        Reservation saved = reservationService.reserve(userId, rawDate, timeId, themeId);
        assertThat(saved.getId()).isEqualTo(1L);
    }

    @DisplayName("실패: 존재하지 않는 멤버 ID 입력 시 예외가 발생한다.")
    @Test
    void save_MemberIdDoesntExist() {
        assertThatThrownBy(
            () -> reservationService.reserve(3L, rawDate, timeId, themeId)
        ).isInstanceOf(RoomescapeException.class)
            .hasMessage("입력한 사용자 ID에 해당하는 데이터가 존재하지 않습니다.");
    }

    @DisplayName("실패: 존재하지 않는 시간 ID 입력 시 예외가 발생한다.")
    @Test
    void save_TimeIdDoesntExist() {
        assertThatThrownBy(
            () -> reservationService.reserve(userId, rawDate, 2L, themeId)
        ).isInstanceOf(RoomescapeException.class)
            .hasMessage("입력한 시간 ID에 해당하는 데이터가 존재하지 않습니다.");
    }

    @DisplayName("실패: 중복 예약을 생성하면 예외가 발생한다.")
    @Test
    void save_Duplication() {
        reservationService.reserve(userId, rawDate, timeId, themeId);

        assertThatThrownBy(
            () -> reservationService.reserve(userId, rawDate, timeId, themeId)
        ).isInstanceOf(RoomescapeException.class)
            .hasMessage("해당 시간에 예약이 이미 존재합니다.");
    }

    @DisplayName("실패: 과거 날짜 예약 생성하면 예외 발생 -- 어제")
    @Test
    void save_PastDateReservation() {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        assertThatThrownBy(
            () -> reservationService.reserve(userId, yesterday, timeId, themeId)
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
            () -> reservationService.reserve(userId, today, savedTime.getId(), themeId)
        ).isInstanceOf(RoomescapeException.class)
            .hasMessage("과거 예약을 추가할 수 없습니다.");
    }

    @DisplayName("성공: 예약 대기")
    @Test
    void standby() {
        Reservation reservation = reservationService.standby(userId, rawDate, timeId, themeId);
        assertThat(reservation.getId()).isEqualTo(1L);
    }

    @DisplayName("실패: 본인의 예약에 대기를 걸 수 없다.")
    @Test
    void standby_CantReserveAndThenStandbyForTheSameReservation() {
        reservationService.reserve(userId, rawDate, timeId, themeId);

        assertThatThrownBy(() -> reservationService.standby(userId, rawDate, timeId, themeId))
            .isInstanceOf(RoomescapeException.class)
            .hasMessage("이미 예약하셨습니다. 대기 없이 이용 가능합니다.");
    }

    @DisplayName("실패: 하나의 예약에 두 개 이상 대기를 걸 수 없다.")
    @Test
    void standby_CantStandbyMoreThanOnce() {
        reservationService.reserve(adminId, rawDate, timeId, themeId);
        reservationService.standby(userId, rawDate, timeId, themeId);

        assertThatThrownBy(() -> reservationService.standby(userId, rawDate, timeId, themeId))
            .isInstanceOf(RoomescapeException.class)
            .hasMessage("이미 대기중인 예약입니다.");
    }

    @DisplayName("성공: 예약 삭제")
    @Test
    void deleteReserved() {
        reservationService.reserve(userId, LocalDate.parse("2060-01-01"), timeId, themeId);
        reservationService.reserve(userId, LocalDate.parse("2060-01-02"), timeId, themeId);
        reservationService.reserve(userId, LocalDate.parse("2060-01-03"), timeId, themeId);

        reservationService.deleteById(2L);

        assertThat(reservationService.findAllReserved())
            .extracting(Reservation::getId)
            .containsExactly(1L, 3L);
    }

    @DisplayName("실패: 예약 삭제 메서드로 예약대기를 삭제할 수 없다.")
    @Test
    void deleteReserved_Cannot_Delete_Standby() {
        reservationService.reserve(adminId, rawDate, timeId, themeId);
        reservationService.standby(userId, rawDate, timeId, themeId);

        assertThatThrownBy(() -> reservationService.deleteById(2L))
            .isInstanceOf(RoomescapeException.class)
            .hasMessage("예약이 존재하지 않아 삭제할 수 없습니다.");
    }

    @DisplayName("성공: 일반유저는 본인의 예약대기를 삭제할 수 있다.")
    @Test
    void deleteStandby() {
        reservationService.reserve(adminId, rawDate, timeId, themeId);
        reservationService.standby(userId, rawDate, timeId, themeId);
        Member user = memberRepository.findById(userId).get();

        assertThatCode(() -> reservationService.deleteStandby(2L, user))
            .doesNotThrowAnyException();
    }

    @DisplayName("성공: 관리자는 다른 회원의 예약대기를 삭제할 수 있다.")
    @Test
    void deleteStandby_ByAdmin() {
        reservationService.reserve(adminId, rawDate, timeId, themeId);
        reservationService.standby(userId, rawDate, timeId, themeId);
        Member admin = memberRepository.findById(adminId).get();

        assertThatCode(() -> reservationService.deleteStandby(2L, admin))
            .doesNotThrowAnyException();
    }

    @DisplayName("실패: 일반유저는 타인의 예약대기를 삭제할 수 없다.")
    @Test
    void deleteStandby_ReservedByOther() {
        reservationService.reserve(userId, rawDate, timeId, themeId);
        reservationService.standby(adminId, rawDate, timeId, themeId);
        Member user = memberRepository.findById(userId).get();

        assertThatThrownBy(() -> reservationService.deleteStandby(2L, user))
            .isInstanceOf(RoomescapeException.class)
            .hasMessage("자신의 예약만 삭제할 수 있습니다.");
    }

    @DisplayName("실패: 예약대기 삭제 메서드로 예약을 삭제할 수 없다.")
    @Test
    void deleteStandby_Cannot_Delete_Reserved() {
        reservationService.reserve(userId, rawDate, timeId, themeId);
        reservationService.standby(adminId, rawDate, timeId, themeId);
        Member user = memberRepository.findById(userId).get();

        assertThatThrownBy(() -> reservationService.deleteStandby(1L, user))
            .isInstanceOf(RoomescapeException.class)
            .hasMessage("예약대기가 존재하지 않아 삭제할 수 없습니다.");
    }

    @DisplayName("성공: 모든 멤버의 전체 예약을 조회할 수 있으며, 예약대기는 조회되지 않는다.")
    @Test
    void findAll() {
        reservationService.reserve(userId, LocalDate.parse("2060-01-01"), timeId, themeId);
        reservationService.standby(adminId, LocalDate.parse("2060-01-01"), timeId, themeId);
        reservationService.reserve(adminId, LocalDate.parse("2060-01-02"), timeId, themeId);
        reservationService.standby(userId, LocalDate.parse("2060-01-02"), timeId, themeId);

        assertThat(reservationService.findAllReserved())
            .extracting(Reservation::getId)
            .containsExactly(1L, 3L);
    }

    @DisplayName("성공: 모든 멤버의 전체 예약대기를 조회할 수 있으며, 예약은 조회되지 않는다.")
    @Test
    void findAllStandby() {
        reservationService.reserve(userId, LocalDate.parse("2060-01-01"), timeId, themeId);
        reservationService.standby(adminId, LocalDate.parse("2060-01-01"), timeId, themeId);
        reservationService.reserve(adminId, LocalDate.parse("2060-01-02"), timeId, themeId);
        reservationService.standby(userId, LocalDate.parse("2060-01-02"), timeId, themeId);

        assertThat(reservationService.findAllStandby())
            .extracting(Reservation::getId)
            .containsExactly(2L, 4L);
    }

    @DisplayName("성공: 검색 필터를 통해 예약을 검색할 수 있다.")
    @Test
    void findAllBy() {
        reservationService.reserve(userId, LocalDate.parse("2060-01-01"), timeId, themeId);
        reservationService.reserve(adminId, LocalDate.parse("2060-01-02"), timeId, themeId);
        reservationService.reserve(userId, LocalDate.parse("2060-01-03"), timeId, themeId);
        reservationService.reserve(adminId, LocalDate.parse("2060-01-04"), timeId, themeId);
        reservationService.reserve(userId, LocalDate.parse("2060-01-05"), timeId, themeId);
        reservationService.reserve(adminId, LocalDate.parse("2060-01-06"), timeId, themeId);

        List<Reservation> reservations = reservationService.findAllByFilter(
            themeId, adminId, LocalDate.parse("2060-01-02"), LocalDate.parse("2060-01-05"));

        assertThat(reservations)
            .extracting(Reservation::getId)
            .containsExactly(2L, 4L);
    }

    @DisplayName("실패: 검색 필터에서 시작 날짜가 끝 날짜보다 뒤일 수 없다.")
    @Test
    void findAllBy_FromFuture_ToPast() {
        assertThatThrownBy(
            () -> reservationService.findAllByFilter(
                themeId, adminId, LocalDate.parse("2060-01-02"), LocalDate.parse("2060-01-01"))
        ).isInstanceOf(RoomescapeException.class)
            .hasMessage("날짜 조회 범위가 올바르지 않습니다.");
    }

    @DisplayName("성공: 특정 멤버가 예약한 예약 및 예약대기 목록 조회")
    @Test
    void findAllWithRankByMemberId() {
        reservationService.reserve(adminId, LocalDate.parse("2060-01-01"), timeId, themeId);
        reservationService.standby(userId, LocalDate.parse("2060-01-01"), timeId, themeId);
        reservationService.reserve(userId, LocalDate.parse("2060-01-02"), timeId, themeId);
        reservationService.reserve(userId, LocalDate.parse("2060-01-03"), timeId, themeId);

        List<FindReservationWithRankDto> reservations = reservationService.findMyReservationsWithRank(userId);
        assertThat(reservations)
            .extracting(FindReservationWithRankDto::reservation)
            .extracting(Reservation::getId)
            .containsExactly(2L, 3L, 4L);
        assertThat(reservations)
            .extracting(FindReservationWithRankDto::rank)
            .containsExactly(1L, 0L, 0L);
    }
}
