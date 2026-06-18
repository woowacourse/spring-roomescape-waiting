package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.dto.ReservationUpdateRequest;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationdate.ReservationDateRepository;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeRepository;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.domain.waitingreservation.WaitingReservation;
import roomescape.domain.waitingreservation.WaitingReservationRepository;

@SpringBootTest
@Sql("/truncate.sql")
class ReservationUpdatingIntegrationTest {

    @Autowired
    private ReservationService reservationService;

    @MockitoSpyBean
    private ReservationRepository reservationRepository;

    @MockitoSpyBean
    private WaitingReservationRepository waitingReservationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationDateRepository reservationDateRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    private Slot originSlot;
    private Slot updateSlot;
    private Reservation originReservation;
    private Member tester;

    @BeforeEach
    void setUp() {
        tester = memberRepository.save(Member.createWithoutId("테스터"));
        originSlot = insertSlot(LocalDate.now().plusDays(2), LocalTime.of(10, 0), "공포");

        originReservation = reservationRepository.save(
            Reservation.createWithoutId(tester, originSlot.date(), originSlot.time(), originSlot.theme())
        );

        updateSlot = insertSlot(LocalDate.now().plusDays(3), LocalTime.of(10, 0), "공포");
    }

    @Test
    void 사용자가_본인의_예약을_수정하면_같은_슬롯의_1순위_대기가_예약으로_변경된다() {
        Member isan = memberRepository.save(Member.createWithoutId("이산"));
        Member gorae = memberRepository.save(Member.createWithoutId("고래"));

        ReservationUpdateRequest updateRequest = new ReservationUpdateRequest(
            updateSlot.date().getId(), updateSlot.time().getId());

        WaitingReservation firstWaiting = waitingReservationRepository.save(
            waiting(isan, originSlot, LocalDateTime.of(2026, 5, 6, 10, 0))
        );
        WaitingReservation secondWaiting = waitingReservationRepository.save(
            waiting(gorae, originSlot, LocalDateTime.of(2026, 5, 7, 10, 0))
        );

        reservationService.updateReservation(originReservation.getId(), updateRequest);

        assertThat(reservationRepository.findByMemberId(tester.getId())).hasSize(1);
        assertThat(reservationRepository.findByMemberId(isan.getId())).hasSize(1);
        assertThat(reservationRepository.existsByDateIdAndTimeIdAndThemeId(
            originSlot.date().getId(), originSlot.time().getId(), originSlot.theme().getId())).isTrue();
        assertThat(reservationRepository.existsByDateIdAndTimeIdAndThemeId(
            updateSlot.date().getId(), updateSlot.time().getId(), originSlot.theme().getId())).isTrue();
        assertThat(waitingReservationRepository.findById(firstWaiting.getId())).isEmpty();
        assertThat(waitingReservationRepository.findById(secondWaiting.getId())).isPresent();
    }

    @Test
    void 예약_수정_중_1순위_예약_대기_추가가_실패하면_전체가_롤백된다() {
        Member isan = memberRepository.save(Member.createWithoutId("이산"));

        ReservationUpdateRequest updateRequest = new ReservationUpdateRequest(
            updateSlot.date().getId(), updateSlot.time().getId());

        WaitingReservation firstWaiting = waitingReservationRepository.save(
            waiting(isan, originSlot, LocalDateTime.of(2026, 5, 6, 10, 0))
        );

        doThrow(new RuntimeException())
            .when(reservationRepository)
            .save(any(Reservation.class));

        assertThatThrownBy(() -> reservationService.updateReservation(originReservation.getId(), updateRequest))
            .isInstanceOf(RuntimeException.class);

        assertThat(reservationRepository.findById(originReservation.getId())).isPresent();
        assertThat(waitingReservationRepository.findById(firstWaiting.getId())).isPresent();
        assertThat(reservationRepository.findByMemberId(isan.getId())).isEmpty();
    }

    @Test
    void 예약_수정_중_1순위_예약_대기_삭제가_실패하면_전체가_롤백된다() {
        Member isan = memberRepository.save(Member.createWithoutId("이산"));

        ReservationUpdateRequest updateRequest = new ReservationUpdateRequest(
            updateSlot.date().getId(), updateSlot.time().getId());

        WaitingReservation firstWaiting = waitingReservationRepository.save(
            waiting(isan, originSlot, LocalDateTime.of(2026, 5, 6, 10, 0))
        );

        doThrow(new RuntimeException()).when(waitingReservationRepository).deleteById(firstWaiting.getId());

        assertThatThrownBy(() -> reservationService.updateReservation(originReservation.getId(), updateRequest))
            .isInstanceOf(RuntimeException.class);

        assertThat(reservationRepository.findById(originReservation.getId())).isPresent();
        assertThat(waitingReservationRepository.findById(firstWaiting.getId())).isPresent();
        assertThat(reservationRepository.findByMemberId(isan.getId())).isEmpty();
    }

    private WaitingReservation waiting(Member member, Slot slot, LocalDateTime createdAt) {
        return WaitingReservation.createWithoutId(member, slot.date(), slot.time(), slot.theme(), createdAt);
    }

    private Slot insertSlot(LocalDate playDay, LocalTime startAt, String themeName) {
        ReservationDate date = reservationDateRepository.save(ReservationDate.createWithoutId(playDay));
        ReservationTime time = reservationTimeRepository.save(ReservationTime.createWithoutId(startAt));
        Theme theme = themeRepository.save(Theme.createWithoutId(themeName, "테마 내용", "/themes/" + themeName));
        return new Slot(date, time, theme);
    }

    private record Slot(ReservationDate date, ReservationTime time, Theme theme) {
    }
}
