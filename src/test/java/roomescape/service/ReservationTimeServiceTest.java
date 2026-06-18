package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import roomescape.application.service.ReservationTimeService;
import roomescape.application.service.command.ReservationTimeCommand;
import roomescape.application.service.result.ReservationTimeResult;
import roomescape.domain.ReservationTime;
import roomescape.domain.TimeStatus;
import roomescape.exception.DuplicateEntityException;
import roomescape.service.fake.FakeReservationTimeRepository;

class ReservationTimeServiceTest {

    private FakeReservationTimeRepository reservationTimeRepository;
    private ReservationTimeService reservationTimeService;

    @BeforeEach
    void setUp() {
        this.reservationTimeRepository = new FakeReservationTimeRepository();
        this.reservationTimeService = new ReservationTimeService(this.reservationTimeRepository);
    }

    @Test
    void 새로운_예약_시간을_정상적으로_등록한다() {
        // given: 새로운 예약 시간 정보가 주어짐
        LocalTime startAt = LocalTime.of(10, 0);
        ReservationTimeCommand command = new ReservationTimeCommand(startAt);

        // when: 시간을 등록함
        ReservationTimeResult result = reservationTimeService.register(command);

        // then: 새로운 ID를 발급해서 반환
        assertThat(result)
                .extracting(ReservationTimeResult::id, ReservationTimeResult::startAt)
                .containsExactly(1L, startAt);
    }

    @Test
    void 이미_등록된_시간으로_등록을_시도하면_예외가_발생한다() {
        // given: 10시 예약 시간이 이미 등록되어 있음
        LocalTime startAt = LocalTime.of(10, 0);
        reservationTimeRepository.save(new ReservationTime(startAt));

        ReservationTimeCommand command = new ReservationTimeCommand(startAt);

        // when & then: 중복된 시간 등록 시 DuplicateEntityException 발생
        assertThatThrownBy(() -> reservationTimeService.register(command))
                .isInstanceOf(DuplicateEntityException.class)
                .hasMessageContaining("이미 등록된 예약 시간 입니다.");
    }

    @Test
    void 식별자를_이용해_예약_시간을_비활성화_한다() {
        // given: 삭제할 예약 시간이 저장되어 있음
        ReservationTime saved = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        Long id = saved.getId();

        // when: 해당 ID로 비활성화를 요청함
        reservationTimeService.deactivate(id);

        // then: 비활성화 상태로 저장됨
        ReservationTime reservationTime = reservationTimeRepository.get(id);
        assertThat(reservationTime.getStatus()).isEqualTo(TimeStatus.INACTIVE);
    }

    @Test
    void 식별자를_이용해_예약_시간을_다시_활성화한다() {
        // given: 삭제할 예약 시간이 저장되어 있음
        ReservationTime saved = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        Long id = saved.getId();
        reservationTimeService.deactivate(id);

        // when: 해당 ID로 활성화를 요청함
        reservationTimeService.activate(id);

        // then: 활성화 상태로 저장됨
        ReservationTime reservationTime = reservationTimeRepository.get(id);
        assertThat(reservationTime.getStatus()).isEqualTo(TimeStatus.ACTIVE);
    }

}
