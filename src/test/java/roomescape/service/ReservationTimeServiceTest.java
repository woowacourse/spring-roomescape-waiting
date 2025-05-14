package roomescape.service;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import roomescape.TestFixture;
import roomescape.domain.*;
import roomescape.exception.DeletionNotAllowedException;
import roomescape.exception.NotFoundReservationTimeException;
import roomescape.service.param.CreateReservationTimeParam;
import roomescape.service.result.ReservationTimeResult;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.TestFixture.DEFAULT_DATE;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReservationTimeServiceTest {

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void 예약_시간을_생성할_수_있다() {
        //given & when
        ReservationTimeResult reservationTimeResult = reservationTimeService.create(new CreateReservationTimeParam(LocalTime.of(12, 1)));
        Optional<ReservationTime> result = reservationTimeRepository.findById(reservationTimeResult.id());

        //then
        assertAll(
                () -> assertThat(result).isPresent(),
                () -> assertThat(result.get().getStartAt()).isEqualTo(reservationTimeResult.startAt())
        );
    }

    @Test
    void id에_해당하는_예약_시간을_찾을_수_있다() {
        //given
        ReservationTime reservationTime = reservationTimeRepository.save(TestFixture.createDefaultReservationTime());

        //when
        ReservationTimeResult reservationTimeResult = reservationTimeService.findById(reservationTime.getId());

        //then
        assertThat(reservationTimeResult).isEqualTo(new ReservationTimeResult(reservationTimeResult.id(), reservationTimeResult.startAt()));
    }

    @Test
    void id에_해당하는_예약_시간이_없는경우_예외가_발생한다() {
        //given & when & then
        assertThatThrownBy(() -> reservationTimeService.findById(1L))
                .isInstanceOf(NotFoundReservationTimeException.class)
                .hasMessage("1에 해당하는 reservation_time 튜플이 없습니다.");
    }

    @Test
    void 전체_예약_시간을_조회할_수_있다() {
        //given
        ReservationTime reservationTime1 = reservationTimeRepository.save(TestFixture.createDefaultReservationTimeByTime(LocalTime.of(12, 0)));
        ReservationTime reservationTime2 = reservationTimeRepository.save(TestFixture.createDefaultReservationTimeByTime(LocalTime.of(13, 0)));

        //when
        List<ReservationTimeResult> reservationTimeResults = reservationTimeService.findAll();

        //then
        assertAll(
                () -> assertThat(reservationTimeResults).hasSize(2),
                () -> assertThat(reservationTimeResults).isEqualTo(List.of(
                        new ReservationTimeResult(reservationTime1.getId(), reservationTime1.getStartAt()),
                        new ReservationTimeResult(reservationTime2.getId(), reservationTime2.getStartAt())
                        ))
                );

    }

    @Test
    void id에_해당하는_예약_시간을_삭제한다() {
        //given
        ReservationTime reservationTime = reservationTimeRepository.save(TestFixture.createDefaultReservationTime());

        //when
        Long id = reservationTime.getId();
        reservationTimeService.deleteById(id);

        //then
        assertThat(reservationTimeRepository.findById(id)).isEmpty();
    }

    @Test
    void time_id를_사용하는_예약이_존재하면_예외를_던진다() {
        //given
        Theme theme = themeRepository.save(TestFixture.createDefaultTheme());
        ReservationTime reservationTime = reservationTimeRepository.save(TestFixture.createDefaultReservationTime());
        Member member = memberRepository.save(TestFixture.createDefaultMember());
        Reservation reservation = TestFixture.createDefaultReservation(member, DEFAULT_DATE, reservationTime, theme);
        reservationRepository.save(reservation);

        //when & then
        assertThatThrownBy(() -> reservationTimeService.deleteById(reservationTime.getId()))
                .isInstanceOf(DeletionNotAllowedException.class)
                .hasMessage("해당 예약 시간에 예약이 존재합니다.");
    }
}