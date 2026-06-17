package roomescape.domain.promotion;

import java.time.LocalDate;
import roomescape.common.DomainAssert;
import roomescape.common.vo.Slot;

/**
 * 아웃박스 행: "이 슬롯의 다음 대기자를 승격시켜라"라는 할 일.
 * 취소와 같은 트랜잭션으로 기록되어, 별도 워커가 나중에 멱등하게 실행한다.
 */
public class PromotionTask {
    private final Long id;
    private final Long themeId;
    private final Long timeId;
    private final LocalDate date;
    private final Long storeId;
    private final OutboxStatus status;

    private PromotionTask(Long id, Long themeId, Long timeId, LocalDate date, Long storeId, OutboxStatus status) {
        DomainAssert.notNull(themeId, "테마 식별자는 비어 있을 수 없습니다.");
        DomainAssert.notNull(timeId, "시간 식별자는 비어 있을 수 없습니다.");
        DomainAssert.notNull(date, "날짜는 비어 있을 수 없습니다.");
        DomainAssert.notNull(storeId, "매장 식별자는 비어 있을 수 없습니다.");
        DomainAssert.notNull(status, "상태는 비어 있을 수 없습니다.");
        this.id = id;
        this.themeId = themeId;
        this.timeId = timeId;
        this.date = date;
        this.storeId = storeId;
        this.status = status;
    }

    public static PromotionTask pending(Slot slot) {
        return new PromotionTask(null, slot.getThemeId(), slot.getTimeId(),
                slot.getDate(), slot.getStoreId(), OutboxStatus.PENDING);
    }

    public static PromotionTask reconstruct(Long id, Long themeId, Long timeId, LocalDate date,
                                            Long storeId, OutboxStatus status) {
        return new PromotionTask(id, themeId, timeId, date, storeId, status);
    }

    public PromotionTask withId(Long id) {
        return new PromotionTask(id, themeId, timeId, date, storeId, status);
    }

    public Long getId() {
        return id;
    }

    public Long getThemeId() {
        return themeId;
    }

    public Long getTimeId() {
        return timeId;
    }

    public LocalDate getDate() {
        return date;
    }

    public Long getStoreId() {
        return storeId;
    }

    public OutboxStatus getStatus() {
        return status;
    }
}
