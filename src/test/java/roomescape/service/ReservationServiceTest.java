package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationWithRank;
import roomescape.fixture.MemberFixture;
import roomescape.repository.ReservationTimeRepository;
import roomescape.system.exception.RoomescapeException;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@Sql(scripts = "/data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/truncate.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class ReservationServiceTest {

    private final String rawDate = "2060-01-01";
    private final Long timeId = 1L;
    private final Long themeId = 1L;
    private final Long memberId = 1L;
    @Autowired
    private ReservationService reservationService;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @DisplayName("성공: 예약을 저장하고, 해당 예약을 id값과 함께 반환한다.")
    @Test
    void save() {
        Reservation saved = reservationService.save(memberId, rawDate, timeId, themeId);
        assertThat(saved.getId()).isEqualTo(7L);
    }

    @DisplayName("실패: 존재하지 않는 멤버 ID 입력 시 예외가 발생한다.")
    @Test
    void save_MemberIdDoesntExist() {
        assertThatThrownBy(
            () -> reservationService.save(10L, rawDate, timeId, themeId)
        ).isInstanceOf(RoomescapeException.class)
            .hasMessage("입력한 사용자 ID에 해당하는 데이터가 존재하지 않습니다.");
    }

    @DisplayName("실패: 존재하지 않는 날짜 입력 시 예외가 발생한다.")
    @ParameterizedTest
    @ValueSource(strings = {"2030-13-01", "2030-12-32"})
    void save_IllegalDate(String invalidRawDate) {
        assertThatThrownBy(
            () -> reservationService.save(memberId, invalidRawDate, timeId, themeId)
        ).isInstanceOf(RoomescapeException.class)
            .hasMessage("잘못된 날짜 형식입니다.");
    }

    @DisplayName("실패: 존재하지 않는 시간 ID 입력 시 예외가 발생한다.")
    @Test
    void save_TimeIdDoesntExist() {
        assertThatThrownBy(
            () -> reservationService.save(memberId, rawDate, 10L, themeId)
        ).isInstanceOf(RoomescapeException.class)
            .hasMessage("입력한 시간 ID에 해당하는 데이터가 존재하지 않습니다.");
    }

    @DisplayName("실패: 중복 예약을 생성하면 예외가 발생한다.")
    @Test
    void save_Duplication() {
        reservationService.save(memberId, rawDate, timeId, themeId);

        assertThatThrownBy(
            () -> reservationService.save(memberId, rawDate, timeId, themeId)
        ).isInstanceOf(RoomescapeException.class)
            .hasMessage("해당 시간에 예약이 이미 존재합니다.");
    }

    @DisplayName("실패: 과거 날짜 예약 생성하면 예외 발생 -- 어제")
    @Test
    void save_PastDateReservation() {
        String yesterday = LocalDate.now().minusDays(1).toString();

        assertThatThrownBy(
            () -> reservationService.save(memberId, yesterday, timeId, themeId)
        ).isInstanceOf(RoomescapeException.class)
            .hasMessage("과거 예약을 추가할 수 없습니다.");
    }

    @DisplayName("실패: 같은 날짜, 과거 시간 예약 생성하면 예외 발생 -- 1분 전")
    @Test
    void save_TodayPastTimeReservation() {
        String today = LocalDate.now().toString();
        String oneMinuteAgo = LocalTime.now().minusMinutes(1).toString();

        ReservationTime savedTime = reservationTimeRepository.save(
            new ReservationTime(oneMinuteAgo));

        assertThatThrownBy(
            () -> reservationService.save(memberId, today, savedTime.getId(), themeId)
        ).isInstanceOf(RoomescapeException.class)
            .hasMessage("과거 예약을 추가할 수 없습니다.");
    }

    @DisplayName("성공: 주어진 멤버가 예약한 예약 목록 조회")
    @Test
    void findMyReservations() {
        List<ReservationWithRank> reservations = reservationService.findMyReservations(1L);
        assertThat(reservations).hasSize(3);
    }

    @DisplayName("성공: 예약 대기 생성")
    @Test
    void saveWaiting() {
        // when
        Reservation saved = reservationService.saveWaiting(memberId, rawDate, timeId, themeId);
        //then
        assertThat(saved.getId()).isEqualTo(7L);
    }

    @DisplayName("실패: 동일한 멤버가 2개 이상의 예약 대기를 생성 시도시 예외 발생.")
    @Test
    void saveWaiting_MemberDuplication() {
        // given 
        Reservation saved = reservationService.saveWaiting(memberId, rawDate, timeId, themeId);
        // when & then
        assertThatThrownBy(
            () -> reservationService.saveWaiting(memberId, rawDate, timeId, themeId)
        ).isInstanceOf(RoomescapeException.class)
            .hasMessage("동일한 멤버가 다수의 예약을 생성할 수 없습니다.");
    }

    @DisplayName("성공: 내 예약 대기를 삭제 한다.")
    @Test
    void deleteWaiting() {
        // given
        Member user = MemberFixture.createUserWithIdTwo();
        // when
        reservationService.deleteWaiting(user, 6L);
        //then
        assertThat(reservationService.findAll()).hasSize(5);
    }

    @DisplayName("실패: 대기가 아닌 예약을 삭제 시도시 예외 발생.")
    @Test
    void deleteWaiting_NotWaiting() {
        // given
        Member user = MemberFixture.createUserWithIdTwo();
        // when & then
        assertThatThrownBy(() -> reservationService.deleteWaiting(user, 1L))
            .isInstanceOf(RoomescapeException.class)
            .hasMessage("대기가 아닌 예약은 삭제할 수 없습니다.");
    }

    @DisplayName("실패: 내 예약이 아닌 대기를 삭제 시도시 예외 발생.")
    @Test
    void deleteWaiting_NotMine() {
        // given
        Member user = MemberFixture.createUserWithIdThree();
        // when & then
        assertThatThrownBy(() -> reservationService.deleteWaiting(user, 6L))
            .isInstanceOf(RoomescapeException.class)
            .hasMessage("다른 유저의 예약 대기는 삭제할 수 없습니다.");
    }

    @DisplayName("성공: 예약 대기 목록 조회")
    @Test
    void findAllWaitingReservations() {
        // when
        List<Reservation> reservations = reservationService.findAllWaitingReservations();
        //then
        assertThat(reservations).hasSize(1);
    }
}
