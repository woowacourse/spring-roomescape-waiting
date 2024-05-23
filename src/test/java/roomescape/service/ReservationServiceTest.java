package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;
import roomescape.dto.LoginMember;
import roomescape.dto.request.AdminReservationRequest;
import roomescape.dto.request.ReservationRequest;
import roomescape.dto.response.ReservationResponse;
import roomescape.fixture.MemberFixtures;
import roomescape.fixture.ReservationFixtures;
import roomescape.fixture.ThemeFixtures;
import roomescape.fixture.TimeSlotFixtures;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.TimeSlotRepository;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Sql(value = "classpath:test-db-clean.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class ReservationServiceTest {

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private TimeSlotRepository timeSlotRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationService reservationService;

    @DisplayName("예약을 삭제하면 예약 대기 1번째가 자동으로 예약된다.")
    @Test
    void delete() {
        //given
        LocalDate curDate = LocalDate.now();
        Member member = memberRepository.save(MemberFixtures.createAdminMember("daon", "test@email.com"));
        Member member2 = memberRepository.save(MemberFixtures.createAdminMember("daon", "test2@email.com"));
        TimeSlot timeSlot = timeSlotRepository.save(TimeSlotFixtures.createReservationTime(LocalTime.now()));
        Theme theme = themeRepository.save(ThemeFixtures.createDefaultTheme());
        Reservation reservation = reservationRepository.save(
                ReservationFixtures.createBookingReservation(member, curDate, timeSlot, theme));
        Reservation reservation2 = reservationRepository.save(
                ReservationFixtures.createPendingReservation(member2, curDate, timeSlot, theme));

        //when
        reservationService.delete(reservation.getId());
        Reservation result = reservationRepository.getReservationBy(reservation2.getId());

        //then
        assertThat(result.getStatus()).isEqualTo(ReservationStatus.BOOKING);
    }

    @DisplayName("예약을 추가할때 데이터가 존재하지 않으면 BOOKING으로 등록된다.")
    @Test
    void create() {
        //given
        LocalDate curDate = LocalDate.now();
        Member member = memberRepository.save(MemberFixtures.createAdminMember("daon", "test@email.com"));
        TimeSlot timeSlot = timeSlotRepository.save(TimeSlotFixtures.createReservationTime(LocalTime.now()));
        Theme theme = themeRepository.save(ThemeFixtures.createDefaultTheme());
        LoginMember loginMember = new LoginMember(member.getId());
        ReservationRequest request = new ReservationRequest(curDate, timeSlot.getId(), theme.getId());

        //when
        ReservationResponse response = reservationService.create(loginMember, request, LocalDateTime.now());
        Reservation result = reservationRepository.getReservationBy(response.id());

        //then
        assertThat(result.getStatus()).isEqualTo(ReservationStatus.BOOKING);
    }

    @DisplayName("예약을 추가할 때 데이터가 존재하면 PENDING으로 등록된다.")
    @Test
    void createWhenAlreadyExist() {
        //given
        LocalDate curDate = LocalDate.now();
        Member member = memberRepository.save(MemberFixtures.createAdminMember("daon", "test@email.com"));
        Member member2 = memberRepository.save(MemberFixtures.createAdminMember("daon", "test2@email.com"));
        TimeSlot timeSlot = timeSlotRepository.save(TimeSlotFixtures.createReservationTime(LocalTime.now()));
        Theme theme = themeRepository.save(ThemeFixtures.createDefaultTheme());
        reservationRepository.save(ReservationFixtures.createBookingReservation(member, curDate, timeSlot, theme));
        LoginMember loginMember = new LoginMember(member2.getId());
        ReservationRequest request = new ReservationRequest(curDate, timeSlot.getId(), theme.getId());

        //when
        ReservationResponse response = reservationService.create(loginMember, request, LocalDateTime.now());
        Reservation result = reservationRepository.getReservationBy(response.id());

        //then
        assertThat(result.getStatus()).isEqualTo(ReservationStatus.PENDING);
    }

    @DisplayName("동일한 회원이 같은 테마, 날짜, 시간으로 예약하면 예외가 발생한다.")
    @Test
    void createWhenDuplicated() {
        //given
        LocalDate curDate = LocalDate.now();
        Member member = memberRepository.save(MemberFixtures.createAdminMember("daon", "test@email.com"));
        TimeSlot timeSlot = timeSlotRepository.save(TimeSlotFixtures.createReservationTime(LocalTime.now()));
        Theme theme = themeRepository.save(ThemeFixtures.createDefaultTheme());
        reservationRepository.save(ReservationFixtures.createBookingReservation(member, curDate, timeSlot, theme));
        LoginMember loginMember = new LoginMember(member.getId());
        ReservationRequest request = new ReservationRequest(curDate, timeSlot.getId(), theme.getId());

        //when //then
        assertThatThrownBy(() -> reservationService.create(loginMember, request, LocalDateTime.now()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("관리자 권한으로 예약을 추가할때 데이터가 존재하지 않으면 BOOKING으로 등록된다.")
    @Test
    void createWithAdmin() {
        //given
        LocalDate curDate = LocalDate.now();
        Member member = memberRepository.save(MemberFixtures.createAdminMember("daon", "test@email.com"));
        TimeSlot timeSlot = timeSlotRepository.save(TimeSlotFixtures.createReservationTime(LocalTime.now()));
        Theme theme = themeRepository.save(ThemeFixtures.createDefaultTheme());
        AdminReservationRequest request = new AdminReservationRequest(member.getId(), curDate, timeSlot.getId(),
                theme.getId());

        //when
        ReservationResponse response = reservationService.create(request, LocalDateTime.now());
        Reservation result = reservationRepository.getReservationBy(response.id());

        //then
        assertThat(result.getStatus()).isEqualTo(ReservationStatus.BOOKING);
    }

    @DisplayName("예약을 추가할 때 데이터가 존재하면 PENDING으로 등록된다.")
    @Test
    void createWhenAlreadyExistWithAdmin() {
        //given
        LocalDate curDate = LocalDate.now();
        Member member = memberRepository.save(MemberFixtures.createAdminMember("daon", "test@email.com"));
        Member member2 = memberRepository.save(MemberFixtures.createAdminMember("daon", "test2@email.com"));
        TimeSlot timeSlot = timeSlotRepository.save(TimeSlotFixtures.createReservationTime(LocalTime.now()));
        Theme theme = themeRepository.save(ThemeFixtures.createDefaultTheme());
        reservationRepository.save(ReservationFixtures.createBookingReservation(member, curDate, timeSlot, theme));
        AdminReservationRequest request = new AdminReservationRequest(member2.getId(), curDate, timeSlot.getId(),
                theme.getId());

        //when
        ReservationResponse response = reservationService.create(request, LocalDateTime.now());
        Reservation result = reservationRepository.getReservationBy(response.id());

        //then
        assertThat(result.getStatus()).isEqualTo(ReservationStatus.PENDING);
    }

    @DisplayName("동일한 회원이 같은 테마, 날짜, 시간으로 예약하면 예외가 발생한다.")
    @Test
    void createWhenDuplicatedWithAdmin() {
        //given
        LocalDate curDate = LocalDate.now();
        Member member = memberRepository.save(MemberFixtures.createAdminMember("daon", "test@email.com"));
        TimeSlot timeSlot = timeSlotRepository.save(TimeSlotFixtures.createReservationTime(LocalTime.now()));
        Theme theme = themeRepository.save(ThemeFixtures.createDefaultTheme());
        reservationRepository.save(ReservationFixtures.createBookingReservation(member, curDate, timeSlot, theme));
        AdminReservationRequest request = new AdminReservationRequest(member.getId(), curDate, timeSlot.getId(),
                theme.getId());

        //when //then
        assertThatThrownBy(() -> reservationService.create(request, LocalDateTime.now()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

