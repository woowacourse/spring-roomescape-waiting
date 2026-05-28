package roomescape.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import roomescape.application.dto.command.ReservationCreateCommand;
import roomescape.application.dto.command.ReservationUpdateCommand;
import roomescape.application.dto.result.MyReservationResult;
import roomescape.application.dto.result.ReservationResult;
import roomescape.exception.client.BusinessRuleViolationException;
import roomescape.exception.client.ResourceNotFoundException;
import roomescape.support.FixedClockConfig;
import roomescape.support.ServiceIntegrationTest;

/**
 * ReservationService 통합 테스트.
 *
 * <p>검증 대상을 두 종류로 나눠 본다:
 * <ul>
 *   <li>시스템 상태에 의존하는 규칙(중복 예약 거부) — 실제 DB로만 진짜를 알 수 있다.</li>
 *   <li>여러 repository가 협력하는 시나리오(예약 취소 → 첫 대기 자동 승격) — 결과를 DB로 확인한다.</li>
 * </ul>
 *
 * <p>시간 결정성: 과거/미래의 의미가 흔들리지 않도록 @Import(FixedClockConfig)로
 * 고정 시계(2026-05-13 12:00) 기반 정책을 @Primary 주입한다.
 * (과거 거부 "규칙 자체"는 FutureOnlyPolicyTest가 단위로 검증했으므로, 여기서는 그 규칙이
 * 서비스 흐름에 연결됐는지만 본다.)
 *
 * <p>자동 승격의 "원자성"(중간 실패 시 전체 롤백)은 이 사이클에서 검증하지 않는다.
 * @Transactional이 코드에 들어오는 사이클 2의 검증 대상이다. 여기서는 정상 흐름의 "결과"만 본다.
 */
@Import(FixedClockConfig.class)
class ReservationServiceTest extends ServiceIntegrationTest {

    private static final LocalDate FUTURE = FixedClockConfig.TODAY.plusDays(10);

    @Autowired
    private ReservationService reservationService;

    private Long timeId;
    private Long themeId;

    @BeforeEach
    void setUpSlot() {
        timeId = fixture.insertTime(LocalTime.of(10, 0));
        themeId = fixture.insertTheme("테마A");
    }

    @Nested
    @DisplayName("예약 생성")
    class Create {

        @Test
        @DisplayName("미래 슬롯에 정상 예약이 생성된다")
        void 정상_생성() {
            assertThatCode(() -> reservationService.create(
                    new ReservationCreateCommand("브라운", FUTURE, timeId, themeId)))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("[상태 의존] 같은 날짜+시간+테마에 이미 예약이 있으면 거부된다")
        void 중복_예약_거부() {
            reservationService.create(new ReservationCreateCommand("브라운", FUTURE, timeId, themeId));

            assertThatThrownBy(() -> reservationService.create(
                    new ReservationCreateCommand("모카", FUTURE, timeId, themeId)))
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .hasMessage("해당 시간은 이미 예약되었습니다. 다른 시간을 선택해 주세요.");
        }

        @Test
        @DisplayName("같은 날짜+시간이라도 테마가 다르면 각각 허용된다")
        void 다른_테마_허용() {
            Long themeB = fixture.insertTheme("테마B");
            reservationService.create(new ReservationCreateCommand("브라운", FUTURE, timeId, themeId));

            assertThatCode(() -> reservationService.create(
                    new ReservationCreateCommand("모카", FUTURE, timeId, themeB)))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("[시간 규칙 연결] 과거 날짜는 정책에 의해 거부된다")
        void 과거_거부() {
            LocalDate yesterday = FixedClockConfig.TODAY.minusDays(1);

            assertThatThrownBy(() -> reservationService.create(
                    new ReservationCreateCommand("브라운", yesterday, timeId, themeId)))
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .hasMessage("지나간 날짜, 시간으로는 예약할 수 없습니다.");
        }

        @Test
        @DisplayName("존재하지 않는 테마 ID → 404성 예외")
        void 존재하지_않는_테마() {
            assertThatThrownBy(() -> reservationService.create(
                    new ReservationCreateCommand("브라운", FUTURE, timeId, 9999L)))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("존재하지 않는 테마입니다.");
        }
    }

    @Nested
    @DisplayName("예약 취소 → 첫 대기 자동 승격")
    class CancelAndPromote {

        @Test
        @DisplayName("예약 취소 시 첫 대기가 예약으로 승격되고 나머지 순번이 당겨진다")
        void 자동_승격() {
            // given: 예약 1건 + 대기 3건
            Long reservationId = fixture.insertReservation("브라운", FUTURE, timeId, themeId);
            Long w1 = fixture.insertWaiting("콘", FUTURE, timeId, themeId, 1);
            Long w2 = fixture.insertWaiting("모카", FUTURE, timeId, themeId, 2);
            Long w3 = fixture.insertWaiting("핀", FUTURE, timeId, themeId, 3);

            // when: 예약자가 취소
            reservationService.deleteByOwner(reservationId, "브라운");

            // then
            // 1) 첫 대기(콘)가 예약자가 됐다
            assertThat(fixture.findReservationOwner(FUTURE, timeId, themeId)).isEqualTo("콘");
            // 1-1) 승격은 "같은 슬롯(날짜·시간·테마)"으로 이뤄진다.
            //      reservation 테이블엔 UNIQUE(date,time_id,theme_id)가 걸려 있어,
            //      삭제가 먼저 커밋되지 않으면 승격 save가 제약 위반으로 터진다.
            //      즉 이 단언이 "삭제 선행 → 같은 자리 승격"이 성립함을 드러낸다.
            //      (사이클 2에서 @Transactional이 들어오면 이 순서가 트랜잭션 경계 문제가 된다)
            assertThat(fixture.findReservationCount(FUTURE, timeId, themeId)).isEqualTo(1);
            // 2) 콘의 대기는 사라졌다
            assertThat(fixture.existsWaiting(w1)).isFalse();
            // 3) 모카·핀의 순번이 한 칸씩 당겨졌다
            assertThat(fixture.findWaitingOrder(w2)).isEqualTo(1);
            assertThat(fixture.findWaitingOrder(w3)).isEqualTo(2);
        }

        @Test
        @DisplayName("대기가 없으면 예약 취소는 단순 삭제로 끝난다")
        void 대기_없을때_단순_취소() {
            Long reservationId = fixture.insertReservation("브라운", FUTURE, timeId, themeId);

            reservationService.deleteByOwner(reservationId, "브라운");

            assertThat(fixture.findReservationCount(FUTURE, timeId, themeId)).isZero();
        }
    }

    @Nested
    @DisplayName("내 예약+대기 조회 (findMyReservationsAndWaitings)")
    class MyReservationsAndWaitings {

        // 이 메서드는 단순 위임이 아니다. 예약 목록과 대기 목록을 각각 조회해 한 리스트로 합치고,
        // 날짜·시간으로 다시 정렬하는 로직(results.sort)이 서비스 안에 있다. 그 병합·정렬을 검증한다.

        @Test
        @DisplayName("예약과 대기가 status로 구분되어 함께 조회된다")
        void 예약과_대기_구분() {
            Long timeId11 = fixture.insertTime(LocalTime.of(11, 0));
            // 브라운: 예약 1건(10:00) + 대기 1건(다른 슬롯 11:00)
            fixture.insertReservation("브라운", FUTURE, timeId, themeId);
            fixture.insertReservation("콘", FUTURE, timeId11, themeId);          // 11:00은 콘이 예약
            fixture.insertWaiting("브라운", FUTURE, timeId11, themeId, 1);        // 브라운은 11:00 대기

            List<MyReservationResult> result =
                    reservationService.findMyReservationsAndWaitings("브라운");

            assertThat(result).hasSize(2);
            assertThat(result).extracting(MyReservationResult::getStatus)
                    .containsExactlyInAnyOrder(
                            MyReservationResult.Status.RESERVED,
                            MyReservationResult.Status.WAITING);
        }

        @Test
        @DisplayName("예약과 대기가 섞여도 날짜·시간 순으로 정렬된다")
        void 병합_후_정렬() {
            Long timeId11 = fixture.insertTime(LocalTime.of(11, 0));
            LocalDate earlier = FUTURE;
            LocalDate later = FUTURE.plusDays(1);

            // 일부러 늦은 날짜를 '예약', 이른 날짜를 '대기'로 만들어
            // "예약 먼저, 대기 나중" 같은 단순 이어붙이기면 깨지도록 구성한다.
            fixture.insertReservation("브라운", later, timeId, themeId);        // 늦은 날짜 = 예약
            fixture.insertReservation("콘", earlier, timeId11, themeId);        // earlier 11:00은 콘 예약
            fixture.insertWaiting("브라운", earlier, timeId11, themeId, 1);     // 이른 날짜 = 대기

            List<MyReservationResult> result =
                    reservationService.findMyReservationsAndWaitings("브라운");

            // 이른 날짜(대기)가 먼저, 늦은 날짜(예약)가 나중 — status와 무관하게 날짜 순
            assertThat(result).extracting(MyReservationResult::getDate)
                    .containsExactly(earlier, later);
            assertThat(result.get(0).getStatus()).isEqualTo(MyReservationResult.Status.WAITING);
            assertThat(result.get(1).getStatus()).isEqualTo(MyReservationResult.Status.RESERVED);
        }

        @Test
        @DisplayName("대기에는 순번이 함께 담기고, 예약에는 순번이 없다(null)")
        void 대기_순번_포함() {
            Long timeId11 = fixture.insertTime(LocalTime.of(11, 0));
            fixture.insertReservation("브라운", FUTURE, timeId, themeId);
            fixture.insertReservation("콘", FUTURE, timeId11, themeId);
            fixture.insertWaiting("브라운", FUTURE, timeId11, themeId, 2);

            List<MyReservationResult> result =
                    reservationService.findMyReservationsAndWaitings("브라운");

            MyReservationResult reserved = result.stream()
                    .filter(r -> r.getStatus() == MyReservationResult.Status.RESERVED)
                    .findFirst().orElseThrow();
            MyReservationResult waiting = result.stream()
                    .filter(r -> r.getStatus() == MyReservationResult.Status.WAITING)
                    .findFirst().orElseThrow();

            assertThat(reserved.getWaitingOrder()).isNull();
            assertThat(waiting.getWaitingOrder()).isEqualTo(2);
        }

        @Test
        @DisplayName("해당 이름의 예약·대기가 없으면 빈 목록")
        void 없으면_빈_목록() {
            assertThat(reservationService.findMyReservationsAndWaitings("없는사람")).isEmpty();
        }
    }

    @Nested
    @DisplayName("예약 변경 (updateByOwner)")
    class Update {

        // 변경은 분기가 많은 유스케이스다. 각 분기가 "시스템 상태(이미 저장된 예약/시간)"에 의존하므로
        // 도메인 단위가 아니라 서비스 통합으로 검증한다. (시간 과거 판정만 정책이 담당)

        @Test
        @DisplayName("본인의 미래 예약을 다른 날짜·시간으로 변경할 수 있다")
        void 정상_변경() {
            Long timeId11 = fixture.insertTime(LocalTime.of(11, 0));
            Long reservationId = fixture.insertReservation("브라운", FUTURE, timeId, themeId);

            ReservationResult updated = reservationService.updateByOwner(
                    new ReservationUpdateCommand(reservationId, "브라운", FUTURE.plusDays(1), timeId11));

            assertThat(updated.getDate()).isEqualTo(FUTURE.plusDays(1));
            assertThat(updated.getTime().getStartAt()).isEqualTo(LocalTime.of(11, 0));
        }

        @Test
        @DisplayName("같은 시간으로의 변경도 허용된다 (자기 자신과는 충돌하지 않음)")
        void 같은_시간_변경_허용() {
            Long reservationId = fixture.insertReservation("브라운", FUTURE, timeId, themeId);

            assertThatCode(() -> reservationService.updateByOwner(
                    new ReservationUpdateCommand(reservationId, "브라운", FUTURE, timeId)))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("[분기] 존재하지 않거나 본인 예약이 아니면 404성 예외")
        void 본인_아님_거부() {
            Long reservationId = fixture.insertReservation("브라운", FUTURE, timeId, themeId);

            // 콘이 브라운의 예약을 변경하려 함
            assertThatThrownBy(() -> reservationService.updateByOwner(
                    new ReservationUpdateCommand(reservationId, "콘", FUTURE, timeId)))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("존재하지 않는 예약입니다.");
        }

        @Test
        @DisplayName("[분기] 변경하려는 시간이 다른 사람에 의해 이미 예약됨 → 거부")
        void 변경_시간_충돌_거부() {
            Long timeId11 = fixture.insertTime(LocalTime.of(11, 0));
            // 브라운의 예약(10:00)과 콘의 예약(11:00)이 같은 테마·날짜에 공존
            Long myReservation = fixture.insertReservation("브라운", FUTURE, timeId, themeId);
            fixture.insertReservation("콘", FUTURE, timeId11, themeId);

            // 브라운이 자기 예약을 콘의 시간(11:00)으로 변경 시도 → 충돌
            assertThatThrownBy(() -> reservationService.updateByOwner(
                    new ReservationUpdateCommand(myReservation, "브라운", FUTURE, timeId11)))
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .hasMessage("해당 시간은 이미 예약되었습니다. 다른 시간을 선택해 주세요.");
        }

        @Test
        @DisplayName("[분기] 존재하지 않는 시간으로 변경 → 404성 예외")
        void 존재하지_않는_시간으로_변경() {
            Long reservationId = fixture.insertReservation("브라운", FUTURE, timeId, themeId);

            assertThatThrownBy(() -> reservationService.updateByOwner(
                    new ReservationUpdateCommand(reservationId, "브라운", FUTURE.plusDays(1), 9999L)))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("존재하지 않는 시간입니다.");
        }

        @Test
        @DisplayName("[시간 규칙 연결] 이미 지난 예약은 변경할 수 없다")
        void 지난_예약_변경_거부() {
            // 고정 시계(TODAY) 기준 과거에 직접 예약을 심는다
            LocalDate past = FixedClockConfig.TODAY.minusDays(1);
            Long reservationId = fixture.insertReservation("브라운", past, timeId, themeId);

            assertThatThrownBy(() -> reservationService.updateByOwner(
                    new ReservationUpdateCommand(reservationId, "브라운", FUTURE, timeId)))
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .hasMessage("이미 지난 예약은 변경할 수 없습니다.");
        }

        @Test
        @DisplayName("[시간 규칙 연결] 변경하려는 시간이 과거면 거부된다")
        void 변경_대상이_과거_거부() {
            Long reservationId = fixture.insertReservation("브라운", FUTURE, timeId, themeId);
            LocalDate past = FixedClockConfig.TODAY.minusDays(1);

            assertThatThrownBy(() -> reservationService.updateByOwner(
                    new ReservationUpdateCommand(reservationId, "브라운", past, timeId)))
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .hasMessage("지나간 날짜·시간으로는 변경할 수 없습니다.");
        }
    }
}
