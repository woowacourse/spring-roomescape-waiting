package roomescape.application;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.exception.client.BusinessRuleViolationException;
import roomescape.support.ServiceIntegrationTest;

/**
 * ReservationTimeService 통합 테스트.
 *
 * <p>검증 대상: 삭제 거부 정책 — 예약이 존재하는 시간은 삭제할 수 없다.
 * 이건 서비스만의 협력이다: Repository가 준 existsByTimeId(boolean)를 보고 서비스가 예외로 전환하는 의사결정. 슬라이스는 existsByTimeId의 참/거짓만 보고, 인수는 HTTP
 * 상태만 본다. "boolean → 예외 전환"이라는 결정 자체는 이 자리에서만 검증된다.
 *
 * <p>여기서 검증하지 않는 것(의식적 제외):
 * <ul>
 *   <li>findAvailable — NOT IN의 정확성·테마/날짜 경계·빈 상태는 JdbcReservationTimeRepositoryTest가
 *       더 싸게(@JdbcTest) 그리고 더 넓게(날짜 경계까지) 검증한다. 서비스가 더하는 건 DTO 매핑
 *       (stream.map) 한 줄뿐이라, 같은 회귀를 두 자리에서 사는 잉여였다 → 슬라이스에 일임하고 뺐다.</li>
 *   <li>create·findAll — 도메인 생성 + Repository 호출 + DTO 변환의 단순 위임이라 서비스만의 협력
 *       책임이 없다. 객체 검증은 ReservationTimeTest가, SQL·응답 형태는 슬라이스·인수가 잡는다.</li>
 * </ul>
 *
 * <p>시간 규칙은 미래/과거와 무관한 순수 상태 의존이라 FixedClockConfig가 필요 없다.
 * (시점 판정은 ReservationService의 책임이고, 여기서는 시간 슬롯의 가용성만 본다.)
 */
class ReservationTimeServiceTest extends ServiceIntegrationTest {

    private static final LocalDate DATE = LocalDate.of(2050, 12, 31);

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Nested
    @DisplayName("시간 삭제 거부 정책")
    class DeletePolicy {

        @Test
        @DisplayName("[상태 의존] 예약이 존재하는 시간은 삭제할 수 없다")
        void 예약_있는_시간_삭제_거부() {
            Long timeId = fixture.insertTime(LocalTime.of(10, 0));
            Long themeId = fixture.insertTheme("테마A");
            fixture.insertReservation("브라운", DATE, timeId, themeId);

            assertThatThrownBy(() -> reservationTimeService.delete(timeId))
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .hasMessage("예약이 존재하는 시간은 삭제할 수 없습니다.");
        }

        @Test
        @DisplayName("예약이 없는 시간은 삭제할 수 있다")
        void 예약_없는_시간_삭제_허용() {
            Long timeId = fixture.insertTime(LocalTime.of(15, 0));

            assertThatCode(() -> reservationTimeService.delete(timeId))
                    .doesNotThrowAnyException();
        }
    }

}
