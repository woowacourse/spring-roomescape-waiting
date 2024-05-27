package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import roomescape.DBTest;
import roomescape.TestFixture;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Status;
import roomescape.domain.Theme;
import roomescape.exception.BadRequestException;
import roomescape.exception.NotFoundException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.dto.request.ReservationCreateRequest;
import roomescape.service.dto.response.ReservationResponse;

class ReservationServiceTest extends DBTest {

    @Autowired
    private ReservationService reservationService;

    @DisplayName("예약을 추가한다.")
    @Test
    void save() {
        // given
        Theme theme = themeRepository.save(TestFixture.getTheme1());
        ReservationTime time = timeRepository.save(TestFixture.getReservationTime10AM());

        // when
        Member member2 = memberRepository.save(TestFixture.getMember2());
        ReservationCreateRequest request = new ReservationCreateRequest(member2.getId(), TestFixture.TOMORROW,
                time.getId(),
                theme.getId());
        ReservationResponse saved = reservationService.save(request);

        // then
        assertThat(reservationRepository.findById(saved.id())).isPresent();
    }

    @DisplayName("예약을 추가할 때 같은 날짜, 시간, 테마에 대한 예약이 존재하는 경우 예외가 발생한다.")
    @Test
    void isReservationExistWhenSave() {
        // given -> member1이 먼저 예약한 상황
        Member member1 = memberRepository.save(TestFixture.getMember1());
        Theme theme = themeRepository.save(TestFixture.getTheme1());
        LocalDate date = LocalDate.now();
        ReservationTime time = timeRepository.save(TestFixture.getReservationTime10AM());

        reservationRepository.save(new Reservation(member1, date, time, theme, Status.CONFIRMED));

        // when
        Member member2 = memberRepository.save(TestFixture.getMember2());
        ReservationCreateRequest request = new ReservationCreateRequest(member2.getId(), date, time.getId(),
                theme.getId());

        // then
        assertThatThrownBy(() -> reservationService.save(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("예약");
    }

    @DisplayName("예약(대기)를 추가할 때 입력된 예약 시간이 존재하지 않는 경우 예외가 발생한다.")
    @Test
    void isTimeNotExist() {
        // given
        ReservationTime time = timeRepository.save(TestFixture.getReservationTime10AM());
        Member member = memberRepository.save(TestFixture.getMember1());
        LocalDate date = LocalDate.now();
        Theme theme = themeRepository.save(TestFixture.getTheme1());

        // when
        ReservationCreateRequest request = new ReservationCreateRequest(member.getId(), date, time.getId() + 1,
                theme.getId());

        // then
        assertThatThrownBy(() -> reservationService.save(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("timeId = " + (time.getId() + 1));
    }


    @DisplayName("예약(대기)를 추가할 때 입력된 멤버가 존재하지 않는 경우 예외가 발생한다.")
    @Test
    void isMemberNotExist() {
        // given
        ReservationTime time = timeRepository.save(TestFixture.getReservationTime10AM());
        Member member = memberRepository.save(TestFixture.getMember1());
        LocalDate date = LocalDate.now().plusDays(1);
        Theme theme = themeRepository.save(TestFixture.getTheme1());

        // when
        ReservationCreateRequest request = new ReservationCreateRequest(member.getId() + 1, date, time.getId(),
                theme.getId());

        // then
        assertThatThrownBy(() -> reservationService.save(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("memberId = " + (member.getId() + 1));
    }


    @DisplayName("예약(대기)를 추가할 때 입력된 테마가 존재하지 않는 경우 예외가 발생한다.")
    @Test
    void isThemeNotExist() {
        // given
        ReservationTime time = timeRepository.save(TestFixture.getReservationTime10AM());
        Member member = memberRepository.save(TestFixture.getMember1());
        LocalDate date = LocalDate.now().plusDays(1);
        Theme theme = themeRepository.save(TestFixture.getTheme1());

        // when
        ReservationCreateRequest request = new ReservationCreateRequest(member.getId(), date, time.getId(),
                theme.getId() + 1);

        // then
        assertThatThrownBy(() -> reservationService.save(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("themeId = " + (theme.getId() + 1));
    }

    @DisplayName("지나간 날짜에 대해서는 예약(대기)를 추가할 수 없다.")
    @Test
    void cantSaveReservationForPastDate() {
        // given
        ReservationTime time = timeRepository.save(TestFixture.getReservationTime10AM());
        Member member = memberRepository.save(TestFixture.getMember1());
        LocalDate date = LocalDate.now().minusDays(1);
        Theme theme = themeRepository.save(TestFixture.getTheme1());

        // when
        ReservationCreateRequest request = new ReservationCreateRequest(member.getId(), date, time.getId(),
                theme.getId());

        // then
        assertThatThrownBy(() -> reservationService.save(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("지나간 날짜와 시간에 대한 예약을 생성할 수 없습니다.");
        assertThatThrownBy(() -> reservationService.saveWaiting(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("지나간 날짜와 시간에 대한 예약을 생성할 수 없습니다.");
    }

    @DisplayName("예약을 했다면 대기를 추가할 수 없다.")
    @Test
    void cantSaveWaitingIfReserved() {
        // given
        ReservationTime time = timeRepository.save(TestFixture.getReservationTime10AM());
        Member member = memberRepository.save(TestFixture.getMember1());
        LocalDate date = LocalDate.now().plusDays(1);
        Theme theme = themeRepository.save(TestFixture.getTheme1());
        ReservationCreateRequest request = new ReservationCreateRequest(member.getId(), date, time.getId(),
                theme.getId());

        // when
        reservationService.save(request);

        // then
        assertThatThrownBy(() -> reservationService.saveWaiting(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("예약");
    }

    @DisplayName("모든 예약을 조회한다.")
    @Test
    void findAllReservations() {
        // given
        Member member = memberRepository.save(TestFixture.getMember1());
        ReservationTime time = timeRepository.save(TestFixture.getReservationTime10AM());
        Theme theme = themeRepository.save(TestFixture.getTheme1());

        reservationRepository.save(new Reservation(member, TestFixture.TOMORROW, time, theme, Status.CONFIRMED));
        reservationRepository.save(
                new Reservation(member, TestFixture.THE_DAY_AFTER_TOMORROW, time, theme, Status.WAITING));

        // when
        List<ReservationResponse> responses = reservationService.findAll().responses();

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses).extracting(ReservationResponse::date).containsExactly(TestFixture.TOMORROW);
    }

    @DisplayName("예약 대기 목록은 날짜, 시간, 테마 이름, 예약 ID 순으로 정렬된다.")
    @Test
    void findAllWaiting() {
        // given
        Member member = memberRepository.save(TestFixture.getMember1());
        ReservationTime time1 = timeRepository.save(TestFixture.getReservationTime10AM());
        ReservationTime time2 = timeRepository.save(TestFixture.getReservationTime11AM());
        Theme theme1 = themeRepository.save(TestFixture.getTheme1());
        Theme theme2 = themeRepository.save(TestFixture.getTheme2());

        //  3 -> 4 -> 5 -> 1 -> 6 -> 2 순으로 정렬되어야 한다.
        Reservation r1 = reservationRepository.save(
                new Reservation(member, TestFixture.THE_DAY_AFTER_TOMORROW, time1, theme1, Status.WAITING));
        Reservation r2 = reservationRepository.save(
                new Reservation(member, TestFixture.THE_DAY_AFTER_TOMORROW, time2, theme1, Status.WAITING));
        Reservation r3 = reservationRepository.save(
                new Reservation(member, TestFixture.TOMORROW, time1, theme1, Status.WAITING));
        Reservation r4 = reservationRepository.save(
                new Reservation(member, TestFixture.TOMORROW, time2, theme1, Status.WAITING));
        Reservation r5 = reservationRepository.save(
                new Reservation(member, TestFixture.TOMORROW, time2, theme2, Status.WAITING));
        Reservation r6 = reservationRepository.save(
                new Reservation(member, TestFixture.THE_DAY_AFTER_TOMORROW, time1, theme1, Status.WAITING));

        // when
        List<ReservationResponse> responses = reservationService.findAllWaiting().responses();

        // then
        assertThat(responses).hasSize(6);
        assertThat(responses).extracting(ReservationResponse::id)
                .containsExactly(r3.getId(), r4.getId(), r5.getId(), r1.getId(), r6.getId(), r2.getId());
        assertThat(responses).isSortedAccordingTo(Comparator.comparing(ReservationResponse::date)
                .thenComparing(response -> response.time().startAt())
                .thenComparing(response -> response.theme().name())
                .thenComparing(ReservationResponse::id));
    }

    @DisplayName("날짜로 예약을 조회할 때 종료 날짜가 시작 날짜보다 빠르면 예외가 발생한다.")
    @Test
    void isEndDateBeforeStartDate() {
        // given
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().minusDays(1);

        // when & then
        assertThatThrownBy(() -> reservationService.findBy(null, null, startDate, endDate))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("종료 날짜가 시작 날짜 이전일 수 없습니다.");
    }

    @DisplayName("예약 대기를 승인할 때 대기 중인 예약이 없으면 예외가 발생한다.")
    @Test
    void isWaitingReservationExist() {
        // given
        Member member = memberRepository.save(TestFixture.getMember1());
        ReservationTime time = timeRepository.save(TestFixture.getReservationTime10AM());
        Theme theme = themeRepository.save(TestFixture.getTheme1());

        // when
        Reservation waiting = reservationRepository.save(
                new Reservation(member, TestFixture.TOMORROW, time, theme, Status.WAITING));

        // then
        assertThatThrownBy(() -> reservationService.approveWaiting(waiting.getId() + 1))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("대기중인 예약을 찾을 수 없습니다. id = " + (waiting.getId() + 1));
    }

    @DisplayName("기존 예약이 존재하는 상태에서 예약 대기를 승인하면 예외가 발생한다.")
    @Test
    void isReservationExist_WhenApproveWaiting() {
        // given
        Member member = memberRepository.save(TestFixture.getMember1());
        ReservationTime time = timeRepository.save(TestFixture.getReservationTime10AM());
        Theme theme = themeRepository.save(TestFixture.getTheme1());

        // when
        Member member2 = memberRepository.save(TestFixture.getMember2());
        reservationRepository.save(new Reservation(member2, TestFixture.TOMORROW, time, theme, Status.CONFIRMED));

        Reservation reservation = reservationRepository.save(
                new Reservation(member, TestFixture.TOMORROW, time, theme, Status.WAITING));

        // then
        assertThatThrownBy(() -> reservationService.approveWaiting(reservation.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("예약");
    }

    @DisplayName("예약 대기를 승인한다.")
    @Test
    void approveWaiting() {
        // given
        Member member = memberRepository.save(TestFixture.getMember1());
        ReservationTime time = timeRepository.save(TestFixture.getReservationTime10AM());
        Theme theme = themeRepository.save(TestFixture.getTheme1());

        Reservation waiting = reservationRepository.save(
                new Reservation(member, TestFixture.TOMORROW, time, theme, Status.WAITING));

        // when
        reservationService.approveWaiting(waiting.getId());

        // then
        Reservation confirmed = reservationRepository.findById(waiting.getId()).orElseThrow();
        assertThat(confirmed.getStatus()).isEqualTo(Status.CONFIRMED);
    }

    @DisplayName("예약 대기를 취소한다.")
    @Test
    void cancelWaiting() {
        // given
        Member member = memberRepository.save(TestFixture.getMember1());
        ReservationTime time = timeRepository.save(TestFixture.getReservationTime10AM());
        Theme theme = themeRepository.save(TestFixture.getTheme1());

        Reservation waiting = reservationRepository.save(
                new Reservation(member, TestFixture.TOMORROW, time, theme, Status.WAITING));

        // when
        reservationService.cancelWaiting(waiting.getId());

        // then
        assertThat(reservationRepository.findById(waiting.getId())).isEmpty();
    }

    @DisplayName("예약 대기를 취소할 때, 대기 중인 예약이 없으면 예외가 발생한다.")
    @Test
    void isWaitingReservationExist_WhenCancelWaiting() {
        // given
        Member member = memberRepository.save(TestFixture.getMember1());
        ReservationTime time = timeRepository.save(TestFixture.getReservationTime10AM());
        Theme theme = themeRepository.save(TestFixture.getTheme1());

        // when
        Reservation waiting = reservationRepository.save(
                new Reservation(member, TestFixture.TOMORROW, time, theme, Status.WAITING));

        // then
        assertThatThrownBy(() -> reservationService.cancelWaiting(waiting.getId() + 1))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("대기중인 예약을 찾을 수 없습니다. id = " + (waiting.getId() + 1));
    }

    @TestConfiguration
    static class ReservationServiceTestConfig {

        @Bean
        public ReservationService reservationService(ReservationRepository reservationRepository,
                                                     ReservationTimeRepository timeRepository,
                                                     ThemeRepository themeRepository,
                                                     MemberRepository memberRepository) {
            return new ReservationService(reservationRepository, timeRepository, themeRepository, memberRepository);
        }
    }
}
