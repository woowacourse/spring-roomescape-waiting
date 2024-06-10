package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.fixture.MemberFixture;
import roomescape.system.exception.RoomescapeException;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@Sql(scripts = "/data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/truncate.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
public class WaitingServiceTest {

    private final String rawDate = "2060-01-01";
    private final Long timeId = 1L;
    private final Long themeId = 1L;
    private final Long memberId = 1L;

    @Autowired
    private WaitingService waitingService;

    @DisplayName("성공: 예약 대기 생성")
    @Test
    void saveWaiting() {
        // when
        Reservation saved = waitingService.saveWaiting(memberId, rawDate, timeId, themeId);
        //then
        assertThat(saved.getId()).isEqualTo(7L);
    }


    @DisplayName("실패: 동일한 멤버가 2개 이상의 예약 대기를 생성 시도시 예외 발생.")
    @Test
    void saveWaiting_MemberDuplication() {
        // given
        Reservation saved = waitingService.saveWaiting(memberId, rawDate, timeId, themeId);
        // when & then
        assertThatThrownBy(
            () -> waitingService.saveWaiting(memberId, rawDate, timeId, themeId)
        ).isInstanceOf(RoomescapeException.class)
            .hasMessage("동일한 멤버가 다수의 예약을 생성할 수 없습니다.");
    }

    @DisplayName("성공: 예약 대기 목록 조회")
    @Test
    void findAllWaitingReservations() {
        // when
        List<Reservation> reservations = waitingService.findAllWaitingReservations();
        //then
        assertThat(reservations).hasSize(1);
    }

    @DisplayName("성공: 내 예약 대기를 삭제 한다.")
    @Test
    void deleteWaiting() {
        // given
        Member user = MemberFixture.createUserWithIdTwo();
        // when
        waitingService.deleteWaiting(user, 6L);
        // then
        assertThat(waitingService.findAllWaitingReservations()).hasSize(0);
    }

    @DisplayName("실패: 대기가 아닌 예약을 삭제 시도시 예외 발생.")
    @Test
    void deleteWaiting_NotWaiting() {
        // given
        Member user = MemberFixture.createUserWithIdTwo();
        // when & then
        assertThatThrownBy(() -> waitingService.deleteWaiting(user, 1L))
            .isInstanceOf(RoomescapeException.class)
            .hasMessage("대기가 아닌 예약은 삭제할 수 없습니다.");
    }

    @DisplayName("실패: 내 예약이 아닌 대기를 삭제 시도시 예외 발생.")
    @Test
    void deleteWaiting_NotMine() {
        // given
        Member user = MemberFixture.createUserWithIdThree();
        // when & then
        assertThatThrownBy(() -> waitingService.deleteWaiting(user, 6L))
            .isInstanceOf(RoomescapeException.class)
            .hasMessage("다른 유저의 예약 대기는 삭제할 수 없습니다.");
    }
}
