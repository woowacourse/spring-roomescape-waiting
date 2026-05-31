package roomescape.reservationtime.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.business.BusinessException;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.dto.TimeRequest;
import roomescape.reservationtime.dto.TimeResponse;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

@SpringBootTest(properties = {
        "spring.sql.init.data-locations=",
        "spring.datasource.url=jdbc:h2:mem:service-test;DB_CLOSE_DELAY=-1"
})
@Transactional
class ReservationTimeServiceTest {

    @Autowired
    private ReservationTimeService reservationTimeService;
    @Autowired
    private ReservationTimeRepository timeRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ThemeRepository themeRepository;

    @Test
    @DisplayName("시간을 생성하면 응답에 정보가 담기고 DB에 저장된다")
    void 시간_생성_시_응답과_DB에_저장된다() {
        TimeResponse response = reservationTimeService.createTime(new TimeRequest(LocalTime.of(10, 0), LocalTime.of(11, 0)));

        assertThat(response.id()).isNotNull().isPositive();
        assertThat(response.startAt()).isEqualTo(LocalTime.of(10, 0));
        assertThat(timeRepository.findById(response.id())).isPresent();
    }

    @Test
    @DisplayName("전체 시간 목록을 조회한다")
    void 전체_시간_목록을_조회한다() {
        reservationTimeService.createTime(new TimeRequest(LocalTime.of(10, 0), LocalTime.of(11, 0)));
        reservationTimeService.createTime(new TimeRequest(LocalTime.of(14, 0), LocalTime.of(15, 0)));

        assertThat(reservationTimeService.getAllTimes()).hasSize(2);
    }

    @Test
    @DisplayName("ID로 시간을 조회한다")
    void ID로_시간을_조회한다() {
        TimeResponse saved = reservationTimeService.createTime(new TimeRequest(LocalTime.of(10, 0), LocalTime.of(11, 0)));

        assertThat(reservationTimeService.getById(saved.id()).getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    @DisplayName("존재하지 않는 시간을 조회하면 예외가 발생한다")
    void 존재하지_않는_시간_조회_시_예외가_발생한다() {
        TimeResponse saved = reservationTimeService.createTime(new TimeRequest(LocalTime.of(10, 0), LocalTime.of(11, 0)));
        reservationTimeService.deleteById(saved.id());

        assertThatThrownBy(() -> reservationTimeService.getById(saved.id()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("존재하지 않는 시간대입니다.");
    }

    @Test
    @DisplayName("예약이 없는 시간을 삭제하면 DB에서 제거된다")
    void 예약이_없는_시간_삭제_시_DB에서_제거된다() {
        TimeResponse saved = reservationTimeService.createTime(new TimeRequest(LocalTime.of(10, 0), LocalTime.of(11, 0)));

        reservationTimeService.deleteById(saved.id());

        assertThat(timeRepository.findById(saved.id())).isEmpty();
    }

    @Test
    @DisplayName("예약이 존재하는 시간은 삭제할 수 없다")
    void 예약이_존재하는_시간은_삭제할_수_없다() {
        ReservationTime time = timeRepository.save(ReservationTime.restore(null, LocalTime.of(10, 0), LocalTime.of(11, 0)));
        Member member = memberRepository.save(Member.restore(null, "user1", "user1@test.com", "1234"));
        Theme theme = themeRepository.save(Theme.restore(null, "테마A", "설명A", "https://a.com"));
        reservationRepository.save(Reservation.restore(null, member, LocalDate.now().plusDays(1), time, theme));

        assertThatThrownBy(() -> reservationTimeService.deleteById(time.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("예약이 존재하는 시간은 삭제할 수 없습니다.");
    }

    @Test
    @DisplayName("날짜·테마로 예약 가능한 시간만 조회한다")
    void 날짜와_테마로_예약_가능한_시간만_조회한다() {
        ReservationTime reserved = timeRepository.save(ReservationTime.restore(null, LocalTime.of(10, 0), LocalTime.of(11, 0)));
        timeRepository.save(ReservationTime.restore(null, LocalTime.of(14, 0), LocalTime.of(15, 0)));
        Member member = memberRepository.save(Member.restore(null, "user1", "user1@test.com", "1234"));
        Theme theme = themeRepository.save(Theme.restore(null, "테마A", "설명A", "https://a.com"));
        LocalDate date = LocalDate.now().plusDays(1);
        reservationRepository.save(Reservation.restore(null, member, date, reserved, theme));

        List<TimeResponse> available = reservationTimeService.getAvailableTimes(date, theme.getId());

        assertThat(available).hasSize(1);
        assertThat(available.get(0).startAt()).isEqualTo(LocalTime.of(14, 0));
    }
}
