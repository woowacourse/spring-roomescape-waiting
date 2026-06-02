package roomescape.acceptance_test;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.acceptance_test.step.ReservationAcceptanceSteps.ReservationInfo;
import roomescape.reservation.controller.dto.ReservationEditRequest;
import roomescape.reservationtime.controller.dto.ReservationTimeCreateRequest;
import roomescape.theme.controller.dto.ThemeCreateRequest;

import java.time.LocalDate;
import java.time.LocalTime;

import static roomescape.acceptance_test.step.ReservationAcceptanceSteps.*;

public class ReservationAcceptanceTest extends AcceptanceTestSupport {

    private static final LocalDate 현재_날짜 = LocalDate.of(2026, 5, 12);
    private static final LocalDate 예약일 = LocalDate.of(2026, 10, 14);
    private static final LocalDate 변경_예약일 = LocalDate.of(2026, 10, 15);

    @Test
    @DisplayName("관리자 예약 목록 조회")
    public void scenario1() {
        mutableClock.setFixed(현재_날짜);

        // given
        Integer themeId = 테마_생성을_요청하고(new ThemeCreateRequest("테마1", "설명", "섬네일"));
        Integer reservationTimeId = 예약_시간_생성을_요청하고(
                new ReservationTimeCreateRequest(LocalTime.of(10, 30))
        );
        ReservationInfo reservation = 예약_생성을_요청하고("brown", 예약일, reservationTimeId, themeId);

        // when
        ExtractableResponse<Response> response = 관리자_예약_목록_조회를_요청하면();

        // then
        생성한_예약이_포함된_관리자_예약_목록을_응답받는다(response, reservation);
    }

    @Test
    @DisplayName("관리자 예약 취소")
    public void scenario2() {
        mutableClock.setFixed(현재_날짜);

        // given
        ReservationInfo reservation = 예약_생성을_요청하고("brown", 예약일, LocalTime.of(10, 30));

        // when
        ExtractableResponse<Response> cancelResponse = 생성한_예약_취소를_요청하면(reservation.id());

        // then
        생성한_예약_취소가_성공한다(cancelResponse);
    }

    @Test
    @DisplayName("내 예약 목록 조회")
    public void scenario3() {
        mutableClock.setFixed(현재_날짜);

        // given
        ReservationInfo reservation = 특정_사용자_이름으로_예약_생성을_요청하고("brown");

        // when
        ExtractableResponse<Response> response = 내_예약_목록_조회를_요청하면(reservation.guestName());

        // then
        특정_사용자의_예약이_포함된_예약_목록을_응답받는다(response, reservation);
    }

    @Test
    @DisplayName("예약 수정")
    public void scenario4() {
        mutableClock.setFixed(현재_날짜);
        // given
        Integer themeId = 테마_생성을_요청하고(new ThemeCreateRequest("테마1", "설명", "섬네일"));
        Integer reservationTimeId = 예약_시간_생성을_요청하고(new ReservationTimeCreateRequest(LocalTime.of(10, 30)));
        Integer editedReservationTimeId = 변경할_예약_시간_생성을_요청하고(new ReservationTimeCreateRequest(LocalTime.of(11, 30)));
        ReservationInfo reservation = 예약_생성을_요청하고("brown", 예약일, reservationTimeId, themeId);

        ReservationEditRequest editRequest = new ReservationEditRequest(변경_예약일, editedReservationTimeId.longValue());

        // when
        ExtractableResponse<Response> response = 예약_날짜와_시간_수정을_요청하면(reservation, editRequest);

        // then
        예약_수정이_성공한다(response);
    }

    @Test
    @DisplayName("예약 수정 시 이미 확정 예약이 있는 시간으로 변경하면 대기가 된다")
    public void scenario4_2() {
        mutableClock.setFixed(현재_날짜);
        // given
        Integer themeId = 테마_생성을_요청하고(new ThemeCreateRequest("테마1", "설명", "섬네일"));
        Integer targetReservationTimeId = 예약_시간_생성을_요청하고(new ReservationTimeCreateRequest(LocalTime.of(10, 30)));
        Integer myReservationTimeId = 변경할_예약_시간_생성을_요청하고(new ReservationTimeCreateRequest(LocalTime.of(11, 30)));
        예약_생성을_요청하고("brown", 예약일, targetReservationTimeId, themeId);
        ReservationInfo myReservation = 예약_생성을_요청하고("pobi", 예약일, myReservationTimeId, themeId);

        ReservationEditRequest editRequest = new ReservationEditRequest(예약일, targetReservationTimeId.longValue());

        // when
        ExtractableResponse<Response> response = 예약_날짜와_시간_수정을_요청하면(myReservation, editRequest);

        // then
        예약_수정이_성공한다(response);
        내_예약_목록에서_예약_상태를_응답받는다(
                내_예약_목록_조회를_요청하면(myReservation.guestName()),
                myReservation,
                "WAITING"
        );
        내_예약_목록에서_대기_순번을_응답받는다(
                내_예약_목록_조회를_요청하면(myReservation.guestName()),
                myReservation,
                1
        );
    }

    @Test
    @DisplayName("이미 시작된 예약 수정 실패")
    public void scenario5() {
        mutableClock.setFixed(현재_날짜);

        // given
        ReservationInfo reservation = 예약_생성을_요청하고("brown", 예약일, LocalTime.of(10, 30));
        Integer editedReservationTimeId = 변경할_예약_시간_생성을_요청하고(
                new ReservationTimeCreateRequest(LocalTime.of(11, 30))
        );
        현재_시간이_예약_시작_이후가_되고(mutableClock);
        ReservationEditRequest editRequest = new ReservationEditRequest(변경_예약일, editedReservationTimeId.longValue());

        // when
        ExtractableResponse<Response> response = 예약_날짜와_시간_수정을_요청하면(reservation, editRequest);

        // then
        예약_수정_실패_응답을_받는다(response, 422);
    }

    @Test
    @DisplayName("지난 날짜와 시간으로 예약 수정 실패")
    public void scenario6() {
        mutableClock.setFixed(현재_날짜);

        // given
        ReservationInfo reservation = 예약_생성을_요청하고("brown", 예약일, LocalTime.of(10, 30));
        Integer editedReservationTimeId = 변경할_예약_시간_생성을_요청하고(
                new ReservationTimeCreateRequest(LocalTime.of(11, 30))
        );
        현재_시간이_변경하려는_예약_날짜와_시간_이후가_되고(mutableClock);
        ReservationEditRequest editRequest = new ReservationEditRequest(
                LocalDate.of(2026, 10, 10),
                editedReservationTimeId.longValue()
        );

        // when
        ExtractableResponse<Response> response = 지난_날짜와_시간으로_예약_수정을_요청하면(
                reservation,
                editRequest
        );

        // then
        예약_수정_실패_응답을_받는다(response, 422);
    }

    @Test
    @DisplayName("다른 사용자의 예약 수정 실패")
    public void scenario7() {
        mutableClock.setFixed(현재_날짜);

        // given
        Integer themeId = 테마_생성을_요청하고(new ThemeCreateRequest("테마1", "설명", "섬네일"));
        Integer reservationTimeId = 예약_시간_생성을_요청하고(
                new ReservationTimeCreateRequest(LocalTime.of(10, 30))
        );
        Integer editedReservationTimeId = 변경할_예약_시간_생성을_요청하고(
                new ReservationTimeCreateRequest(LocalTime.of(11, 30))
        );
        ReservationInfo otherReservation = 특정_사용자_이름으로_예약_생성을_요청하고(
                "brown",
                예약일,
                reservationTimeId,
                themeId
        );
        ReservationInfo myReservation = 다른_사용자_이름으로_새로운_예약_생성을_요청하고(
                "pobi",
                변경_예약일,
                editedReservationTimeId,
                themeId
        );

        // when
        ExtractableResponse<Response> response = 다른_사용자의_이름으로_예약_수정을_요청하면(
                myReservation,
                otherReservation
        );

        // then
        예약_수정_실패_응답을_받는다(response, 403);
    }

    @Test
    @DisplayName("내 예약 취소")
    public void scenario8() {
        mutableClock.setFixed(현재_날짜);

        // given
        ReservationInfo reservation = 특정_사용자_이름으로_예약_생성을_요청하고("brown");

        // when
        ExtractableResponse<Response> cancelResponse = 내_예약_취소를_요청하면(reservation);

        // then
        내_예약_취소가_성공한다(cancelResponse);
        내_예약_목록_조회_시_취소한_예약은_취소_상태로_응답받는다(내_예약_목록_조회를_요청하면(reservation.guestName()), reservation);
    }

    @Test
    @DisplayName("빈 시간대 예약은 확정된다")
    public void scenario9() {
        mutableClock.setFixed(현재_날짜);

        // given
        ReservationInfo reservation = 예약_생성을_요청하고("brown", 예약일, LocalTime.of(10, 30));

        // then
        예약_상태가_확정으로_응답받는다(reservation);
    }

    @Test
    @DisplayName("이미 확정 예약이 있는 시간대의 예약은 대기가 된다")
    public void scenario10() {
        mutableClock.setFixed(현재_날짜);

        // given
        Integer themeId = 테마_생성을_요청하고(new ThemeCreateRequest("테마1", "설명", "섬네일"));
        Integer reservationTimeId = 예약_시간_생성을_요청하고(new ReservationTimeCreateRequest(LocalTime.of(10, 30)));
        ReservationInfo confirmedReservation = 예약_생성을_요청하고("brown", 예약일, reservationTimeId, themeId);

        // when
        ReservationInfo waitingReservation = 예약_생성을_요청하고("pobi", 예약일, reservationTimeId, themeId);

        // then
        예약_상태가_확정으로_응답받는다(confirmedReservation);
        예약_상태와_대기_순번을_응답받는다(waitingReservation, 1);
        내_예약_목록에서_대기_순번을_응답받는다(
                내_예약_목록_조회를_요청하면(waitingReservation.guestName()),
                waitingReservation,
                1
        );
    }

    @Test
    @DisplayName("같은 시간대의 대기 예약은 생성 순서대로 대기 순번이 부여된다")
    public void scenario11() {
        mutableClock.setFixed(현재_날짜);

        // given
        Integer themeId = 테마_생성을_요청하고(new ThemeCreateRequest("테마1", "설명", "섬네일"));
        Integer reservationTimeId = 예약_시간_생성을_요청하고(new ReservationTimeCreateRequest(LocalTime.of(10, 30)));
        예약_생성을_요청하고("brown", 예약일, reservationTimeId, themeId);

        // when
        ReservationInfo firstWaiting = 예약_생성을_요청하고("pobi", 예약일, reservationTimeId, themeId);
        ReservationInfo secondWaiting = 예약_생성을_요청하고("jason", 예약일, reservationTimeId, themeId);

        // then
        예약_상태와_대기_순번을_응답받는다(firstWaiting, 1);
        예약_상태와_대기_순번을_응답받는다(secondWaiting, 2);
    }

    @Test
    @DisplayName("확정 예약을 취소하면 첫 번째 대기 예약이 확정된다")
    public void scenario12() {
        mutableClock.setFixed(현재_날짜);

        // given
        Integer themeId = 테마_생성을_요청하고(new ThemeCreateRequest("테마1", "설명", "섬네일"));
        Integer reservationTimeId = 예약_시간_생성을_요청하고(new ReservationTimeCreateRequest(LocalTime.of(10, 30)));
        ReservationInfo confirmedReservation = 예약_생성을_요청하고("brown", 예약일, reservationTimeId, themeId);
        ReservationInfo waitingReservation = 예약_생성을_요청하고("pobi", 예약일, reservationTimeId, themeId);

        // when
        ExtractableResponse<Response> cancelResponse = 내_예약_취소를_요청하면(confirmedReservation);

        // then
        내_예약_취소가_성공한다(cancelResponse);
        내_예약_목록에서_예약_상태를_응답받는다(
                내_예약_목록_조회를_요청하면(waitingReservation.guestName()),
                waitingReservation,
                "CONFIRMED"
        );
    }
}
