package roomescape.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.*;
import roomescape.infrastructure.*;
import roomescape.service.request.WaitingAppRequest;
import roomescape.service.response.WaitingAppResponse;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.Fixture.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Sql(scripts = "/truncate.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Transactional
class WaitingServiceTest {

    @Autowired
    private WaitingService waitingService;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private WaitingRepository waitingRepository;

    private final List<Member> members = List.of(
            new Member(
                    new MemberName("감자"),
                    new MemberEmail("111@aaa.com"),
                    new MemberPassword("asd"),
                    MemberRole.USER
            ),
            new Member(
                    new MemberName("고구마"),
                    new MemberEmail("222@aaa.com"),
                    new MemberPassword("asd"),
                    MemberRole.USER
            ),
            new Member(
                    new MemberName("단호박"),
                    new MemberEmail("333@aaa.com"),
                    new MemberPassword("asd"),
                    MemberRole.USER
            )
    );

    @Test
    @DisplayName("올바르게 예약 대기를 생성한다.")
    void save() {
        Member otherMember = memberRepository.save(members.get(0));
        Member member =memberRepository.save(members.get(1));
        String date = LocalDate.now().plusDays(1).toString();
        Theme theme = themeRepository.save(VALID_THEME);
        ReservationTime time = reservationTimeRepository.save(VALID_RESERVATION_TIME);
        reservationRepository.save(new Reservation(otherMember, new ReservationDate(date), time, theme));

        WaitingAppRequest request = new WaitingAppRequest(date, time.getId(), theme.getId(), member.getId());
        WaitingAppResponse response = waitingService.save(request);
        Waiting expectedWaiting = waitingRepository.findById(response.id()).get();

        assertThat(response).isEqualTo(new WaitingAppResponse(expectedWaiting));
    }

    @Test
    @DisplayName("본인이 예약한 날짜, 시간, 테마에 대해서 대기 생성 시 예외가 발생한다.")
    void saveWithSelfReservationExist() {
        Member member = memberRepository.save(members.get(0));
        String date = LocalDate.now().plusDays(1).toString();
        Theme theme = themeRepository.save(VALID_THEME);
        ReservationTime time = reservationTimeRepository.save(VALID_RESERVATION_TIME);
        reservationRepository.save(new Reservation(member, new ReservationDate(date), time, theme));

        WaitingAppRequest request = new WaitingAppRequest(date, time.getId(), theme.getId(), member.getId());

        assertThatThrownBy(() -> waitingService.save(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("본인이 예약한 날짜, 시간, 테마에 대해서는 대기를 생성할 수 없습니다.");
    }

    @Test
    @DisplayName("예약이 없는 날짜, 시간, 테마에 대해서 대기 생성 시 예외가 발생한다.")
    void saveWithNoReservationExist() {
        Member member = memberRepository.save(members.get(0));
        String date = LocalDate.now().plusDays(1).toString();
        Theme theme = themeRepository.save(VALID_THEME);
        ReservationTime time = reservationTimeRepository.save(VALID_RESERVATION_TIME);

        WaitingAppRequest request = new WaitingAppRequest(date, time.getId(), theme.getId(), member.getId());

        assertThatThrownBy(() -> waitingService.save(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("예약이 존재하지 않는 날짜, 시간, 테마에 대해서는 대기를 생성할 수 없습니다.");
    }

    @Test
    @DisplayName("날짜, 시간, 테마에 대해서 중복된 대기 생성 시 예외가 발생한다.")
    void saveWithDuplicatedWaiting() {
        Member otherMember = memberRepository.save(members.get(0));
        Member member =memberRepository.save(members.get(1));
        String date = LocalDate.now().plusDays(1).toString();
        Theme theme = themeRepository.save(VALID_THEME);
        ReservationTime time = reservationTimeRepository.save(VALID_RESERVATION_TIME);
        reservationRepository.save(new Reservation(otherMember, new ReservationDate(date), time, theme));
        waitingRepository.save(new Waiting(member, new ReservationDate(date), time, theme));

        WaitingAppRequest request = new WaitingAppRequest(date, time.getId(), theme.getId(), member.getId());

        assertThatThrownBy(() -> waitingService.save(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("동일한 사용자의 중복된 예약 대기를 생성할 수 없습니다. ");
    }

    @Test
    @DisplayName("지나간 시간에 대한 대기 생성 시 예외가 발생한다.")
    void saveWithPastTime() {
        Member otherMember = memberRepository.save(members.get(0));
        Member member =memberRepository.save(members.get(1));
        String date = LocalDate.now().minusDays(1).toString();
        Theme theme = themeRepository.save(VALID_THEME);
        ReservationTime time = reservationTimeRepository.save(VALID_RESERVATION_TIME);
        reservationRepository.save(new Reservation(otherMember, new ReservationDate(date), time, theme));

        WaitingAppRequest request = new WaitingAppRequest(date, time.getId(), theme.getId(), member.getId());

        assertThatThrownBy(() -> waitingService.save(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("지나간 시간에 대한 에약 대기는 생성할 수 없습니다.");
    }
}
