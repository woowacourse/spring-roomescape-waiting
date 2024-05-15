package roomescape.service;

import static java.time.LocalDate.now;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import static roomescape.model.Role.MEMBER;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import roomescape.controller.request.ReservationTimeRequest;
import roomescape.controller.response.IsReservedTimeResponse;
import roomescape.exception.BadRequestException;
import roomescape.exception.DuplicatedException;
import roomescape.exception.NotFoundException;
import roomescape.model.Member;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.model.Theme;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationTimeServiceTest {

    @Autowired
    private final ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private final ReservationRepository reservationRepository;
    @Autowired
    private final ThemeRepository themeRepository;
    @Autowired
    private final MemberRepository memberRepository;
    @Autowired
    private final ReservationTimeService reservationTimeService;

    @Autowired
    public ReservationTimeServiceTest(ReservationTimeRepository reservationTimeRepository,
                                      ReservationRepository reservationRepository, ThemeRepository themeRepository,
                                      MemberRepository memberRepository,
                                      ReservationTimeService reservationTimeService) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.reservationTimeService = reservationTimeService;
    }

    @DisplayName("모든 예약 시간을 반환한다")
    @Test
    void should_return_all_reservation_times() {
        reservationTimeRepository.save(new ReservationTime(1L, LocalTime.of(11, 0)));
        reservationTimeRepository.save(new ReservationTime(2L, LocalTime.of(12, 0)));

        List<ReservationTime> reservationTimes = reservationTimeService.findAllReservationTimes();

        assertThat(reservationTimes).hasSize(2);
    }

    @DisplayName("아이디에 해당하는 예약 시간을 반환한다.")
    @Test
    void should_get_reservation_time() {
        reservationTimeRepository.save(new ReservationTime(1L, LocalTime.of(11, 0)));
        reservationTimeRepository.save(new ReservationTime(2L, LocalTime.of(12, 0)));

        ReservationTime reservationTime = reservationTimeService.findReservationTime(2);

        assertThat(reservationTime.getStartAt()).isEqualTo(LocalTime.of(12, 0));
    }

    @DisplayName("예약 시간을 추가한다")
    @Test
    void should_add_reservation_times() {
        reservationTimeService.addReservationTime(new ReservationTimeRequest(LocalTime.of(13, 0)));

        List<ReservationTime> allReservationTimes = reservationTimeRepository.findAll();

        assertThat(allReservationTimes).hasSize(1);
    }

    @DisplayName("예약 시간을 삭제한다")
    @Test
    void should_remove_reservation_times() {
        reservationTimeRepository.save(new ReservationTime(1L, LocalTime.of(12, 0)));
        reservationTimeRepository.save(new ReservationTime(2L, LocalTime.of(13, 0)));

        reservationTimeService.deleteReservationTime(1);

        List<ReservationTime> allReservationTimes = reservationTimeRepository.findAll();
        assertThat(allReservationTimes).hasSize(1);
    }

    @DisplayName("존재하지 않는 시간이면 예외를 발생시킨다.")
    @Test
    void should_throw_exception_when_not_exist_id() {
        assertThatThrownBy(() -> reservationTimeService.deleteReservationTime(10000000))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("[ERROR] id(10000000)에 해당하는 예약 시간이 존재하지 않습니다.");
    }

    @DisplayName("존재하는 시간이면 예외가 발생하지 않는다.")
    @Test
    void should_not_throw_exception_when_exist_id() {
        reservationTimeRepository.save(new ReservationTime(1L, LocalTime.of(12, 0)));

        assertThatCode(() -> reservationTimeService.deleteReservationTime(1))
                .doesNotThrowAnyException();
    }

    @DisplayName("특정 시간에 대해 예약이 존재하는데, 그 시간을 삭제하려 할 때 예외가 발생한다.")
    @Test
    void should_throw_exception_when_exist_reservation_using_time() {
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(12, 0));
        Theme theme = new Theme("name", "공포", "미스터리");
        Member member = new Member(1L, "배키", MEMBER, "dmsgml@email.com", "1234");
        reservationTimeRepository.save(reservationTime);
        themeRepository.save(theme);
        memberRepository.save(member);

        reservationRepository.save(
                new Reservation(now().plusDays(2), reservationTime, theme, member));

        assertThatThrownBy(() -> reservationTimeService.deleteReservationTime(1))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("[ERROR] 해당 시간에 예약이 존재하여 삭제할 수 없습니다.");
    }

    @DisplayName("존재하는 시간을 추가하려 할 때 예외가 발생한다.")
    @Test
    void should_throw_exception_when_add_exist_time() {
        LocalTime reservedTime = LocalTime.of(10, 0);
        reservationTimeRepository.save(new ReservationTime(1L, reservedTime));

        ReservationTimeRequest request = new ReservationTimeRequest(reservedTime);

        assertThatThrownBy(() -> reservationTimeService.addReservationTime(request))
                .isInstanceOf(DuplicatedException.class)
                .hasMessage("[ERROR] 이미 존재하는 시간입니다.");
    }

    @DisplayName("예약 가능 상태를 담은 시간 정보를 반환한다.")
    @Test
    void should_return_times_with_book_state() {
        ReservationTime reservedTime = new ReservationTime(1L, LocalTime.of(12, 0));
        ReservationTime notReservedTime = new ReservationTime(2L, LocalTime.of(13, 0));
        reservationTimeRepository.save(reservedTime);
        reservationTimeRepository.save(notReservedTime);

        Theme theme = new Theme(1L, "배키", "드라마", "hello.jpg");
        themeRepository.save(theme);

        Member member = new Member(1L, "배키", MEMBER, "email@email.com", "1234");
        memberRepository.save(member);

        LocalDate reservedDate = now().plusDays(2);
        reservationRepository.save(new Reservation(reservedDate, reservedTime, theme, member));

        System.out.println("reservationRepository = " + reservationRepository.findAll().size());

        List<IsReservedTimeResponse> times = reservationTimeService.getIsReservedTime(reservedDate, 1L);

        assertSoftly(softAssertions -> {
            softAssertions.assertThat(times).hasSize(2);
            softAssertions.assertThat(times).containsOnly(
                    new IsReservedTimeResponse(1L, reservedTime.getStartAt(), true),
                    new IsReservedTimeResponse(2L, notReservedTime.getStartAt(), false));
        });
    }
}
