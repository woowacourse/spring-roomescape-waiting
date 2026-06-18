package roomescape.domain.reservationtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationdate.ReservationDateRepository;
import roomescape.domain.reservationtime.dto.ReservationTimeAvailabilityResponse;
import roomescape.domain.reservationtime.dto.TimeCreationRequest;
import roomescape.domain.reservationtime.dto.TimeCreationResponse;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.support.exception.RoomescapeException;

@SpringBootTest
@Sql("/truncate.sql")
class ReservationTimeServiceTest {

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationDateRepository reservationDateRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void 예약_시간을_생성한다() {
        TimeCreationRequest request = new TimeCreationRequest(LocalTime.of(10, 0));

        TimeCreationResponse response = reservationTimeService.createReservationTime(request);

        assertThat(response.startAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    void 중복된_시간_생성_시_예외가_발생한다() {
        LocalTime startAt = LocalTime.of(10, 0);
        reservationTimeService.createReservationTime(new TimeCreationRequest(startAt));

        assertThatThrownBy(() -> reservationTimeService.createReservationTime(new TimeCreationRequest(startAt)))
                .isInstanceOf(RoomescapeException.class);
    }

    @Test
    void 특정_테마와_날짜의_예약_가능_시간을_조회한다() {
        ReservationTime time1 = createTime(LocalTime.of(10, 0));
        ReservationTime time2 = createTime(LocalTime.of(11, 0));
        ReservationDate date = reservationDateRepository.save(ReservationDate.createWithoutId(LocalDate.now().plusDays(1)));
        Theme theme = themeRepository.save(Theme.createWithoutId("테스트테마", "설명", "url"));
        Member tester = memberRepository.save(Member.createWithoutId("테스터"));
        reservationRepository.save(Reservation.createWithoutId(tester, date, time1, theme));

        List<ReservationTimeAvailabilityResponse> responses =
                reservationTimeService.getReservationTimeAvailability(theme.getId(), date.getId());

        assertThat(responses).hasSize(2);
        assertThat(responses.stream().filter(r -> r.timeId().equals(time1.getId())).findFirst().get().available()).isFalse();
        assertThat(responses.stream().filter(r -> r.timeId().equals(time2.getId())).findFirst().get().available()).isTrue();
    }

    @Test
    void 사용_중인_시간을_삭제하려_하면_예외가_발생한다() {
        ReservationTime time = createTime(LocalTime.of(10, 0));
        ReservationDate date = reservationDateRepository.save(ReservationDate.createWithoutId(LocalDate.now().plusDays(1)));
        Theme theme = themeRepository.save(Theme.createWithoutId("테스트테마", "설명", "url"));
        Member tester = memberRepository.save(Member.createWithoutId("테스터"));
        reservationRepository.save(Reservation.createWithoutId(tester, date, time, theme));

        assertThatThrownBy(() -> reservationTimeService.deleteReservationTime(time.getId()))
                .isInstanceOf(RoomescapeException.class);
    }

    private ReservationTime createTime(LocalTime startAt) {
        return reservationTimeRepository.save(ReservationTime.createWithoutId(startAt));
    }
}
