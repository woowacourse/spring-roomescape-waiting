package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.common.exception.BusinessRuleViolationException;
import roomescape.common.vo.Name;
import roomescape.common.vo.Slot;
import roomescape.member.Member;
import roomescape.member.MemberRole;
import roomescape.reservation.Reservation;
import roomescape.store.Store;
import roomescape.theme.Theme;
import roomescape.time.Time;
import roomescape.waiting.Waiting;
import roomescape.waiting.Waitings;

class WaitingsTest {

    @Test
    @DisplayName("서로 다른 슬롯의 대기는 하나의 대기 목록으로 묶을 수 없다")
    void throwsWhenWaitingsHaveDifferentSlots() {
        Member member = new Member(1L, "유저", "user@test.com", "password", MemberRole.USER);
        Theme theme = new Theme(1L, new Name("테마"), "http://thumbnail", "설명");
        Store store = new Store(1L, "강남점");
        Time time = new Time(1L, LocalTime.of(13, 0));

        Waiting first = Waiting.reconstruct(1L, member, LocalDate.now().plusDays(1), time, theme, store);
        Waiting second = Waiting.reconstruct(2L, member, LocalDate.now().plusDays(2), time, theme, store);

        assertThatThrownBy(() -> new Waitings(first.getSlot(), List.of(first, second)))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    @DisplayName("대기 목록의 슬롯과 다른 예약에는 대기를 생성할 수 없다")
    void throwsWhenCreatingWaitingForDifferentReservationSlot() {
        Member reserver = new Member(1L, "예약자", "reserver@test.com", "password", MemberRole.USER);
        Member waitingMember = new Member(2L, "유저", "user@test.com", "password", MemberRole.USER);
        Theme theme = new Theme(1L, new Name("테마"), "http://thumbnail", "설명");
        Store store = new Store(1L, "강남점");
        Time time = new Time(1L, LocalTime.of(13, 0));
        Reservation reservation = Reservation.createByUser(
                reserver, LocalDate.now().plusDays(2), time, theme, store, LocalDateTime.now());
        Slot otherSlot = new Slot(LocalDate.now().plusDays(1), time, theme, store);
        Waitings waitings = new Waitings(otherSlot, new ArrayList<>());

        assertThatThrownBy(() -> waitings.enqueue(waitingMember, reservation, LocalDateTime.now()))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    @DisplayName("이미 지난 슬롯에는 대기를 생성할 수 없다")
    void throwsWhenSlotIsPast() {
        Member reserver = new Member(1L, "예약자", "reserver@test.com", "password", MemberRole.USER);
        Member waitingMember = new Member(2L, "유저", "user@test.com", "password", MemberRole.USER);
        Theme theme = new Theme(1L, new Name("테마"), "http://thumbnail", "설명");
        Store store = new Store(1L, "강남점");
        Time time = new Time(1L, LocalTime.of(13, 0));
        LocalDate date = LocalDate.now().plusDays(1);
        Reservation reservation = Reservation.createByUser(
                reserver, date, time, theme, store, LocalDateTime.now());
        Waitings waitings = new Waitings(reservation.getSlot(), new ArrayList<>());

        LocalDateTime afterSlot = LocalDateTime.of(date, LocalTime.of(13, 0)).plusMinutes(1);

        assertThatThrownBy(() -> waitings.enqueue(waitingMember, reservation, afterSlot))
                .isInstanceOf(BusinessRuleViolationException.class);
    }
}
