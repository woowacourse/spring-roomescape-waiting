package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Slot;
import roomescape.domain.Theme;
import roomescape.domain.exception.RoomEscapeException;
import roomescape.dto.ReservationTimeRequest;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.SlotRepository;
import roomescape.repository.ThemeRepository;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationTimeServiceTest {

    private static final LocalTime TEN = LocalTime.of(10, 0);

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private SlotRepository slotRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void 예약시간을_추가한다() {
        ReservationTimeRequest request = new ReservationTimeRequest(TEN);

        ReservationTime reservationTime = reservationTimeService.addReservationTime(request);

        assertThat(reservationTime.getId()).isNotNull();
        assertThat(reservationTime.getStartAt()).isEqualTo(TEN);
    }

    @Test
    void 모든_예약시간을_조회한다() {
        ReservationTimeRequest request = new ReservationTimeRequest(TEN);
        reservationTimeService.addReservationTime(request);

        List<ReservationTime> reservationTimes = reservationTimeService.getReservationTimes();

        assertThat(reservationTimes).hasSize(1);
        assertThat(reservationTimes.getFirst().getStartAt()).isEqualTo(TEN);
    }

    @Test
    void id에_맞는_예약시간을_조회한다() {
        ReservationTimeRequest request = new ReservationTimeRequest(TEN);
        Long saveId = reservationTimeService.addReservationTime(request).getId();

        ReservationTime reservationTime = reservationTimeService.getReservationTime(saveId);

        assertThat(reservationTime.getId()).isEqualTo(saveId);
        assertThat(reservationTime.getStartAt()).isEqualTo(TEN);
    }

    @Test
    void 예약시간을_삭제한다() {
        ReservationTimeRequest request = new ReservationTimeRequest(TEN);
        Long saveId = reservationTimeService.addReservationTime(request).getId();

        reservationTimeService.deleteReservationTime(saveId);

        assertThatThrownBy(() -> reservationTimeService.getReservationTime(saveId))
            .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 예약_목록에_삭제할_시간이_존재한다면_예약시간을_삭제할_수_없다() {
        ReservationTimeRequest request = new ReservationTimeRequest(TEN);
        ReservationTime reservationTime = reservationTimeService.addReservationTime(request);

        Long themeId = themeRepository.save(new Theme("방탈출 제목", "방탈출 설명", "thumbnail.png")).getId();
        Optional<Theme> theme = themeRepository.findById(themeId);

        reservationRepository.save(createReservation(
            "브라운",
            LocalDate.now().plusDays(1),
            reservationTime,
            theme.get()
        ));

        assertThatThrownBy(() -> reservationTimeService.deleteReservationTime(reservationTime.getId()))
            .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 없는_예약시간을_삭제할_수_없다() {
        assertThatThrownBy(() -> reservationTimeService.deleteReservationTime(1L))
            .isInstanceOf(RoomEscapeException.class);
    }

    private Reservation createReservation(String name, LocalDate date, ReservationTime time, Theme theme) {
        Slot slot = slotRepository.getOrCreate(Slot.of(date, time, theme));
        Member member = memberRepository.findByName(name)
            .orElseGet(() -> memberRepository.save(new Member(name)));
        return new Reservation(member, slot);
    }
}
