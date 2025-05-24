package roomescape.unit.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dto.request.CreateReservationRequest;
import roomescape.dto.request.CreateWaitReservationRequest;
import roomescape.dto.request.LoginMemberRequest;
import roomescape.dto.response.MyReservationResponse;
import roomescape.dto.response.ReservationResponse;
import roomescape.dto.response.ReservationWaitResponse;
import roomescape.entity.Member;
import roomescape.entity.Reservation;
import roomescape.entity.ReservationTime;
import roomescape.entity.Theme;
import roomescape.exception.custom.InvalidReservationException;
import roomescape.global.ReservationStatus;
import roomescape.global.Role;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.ReservationService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ReservationServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ReservationTimeRepository reservationTimeRepository;

    @Mock
    private ThemeRepository themeRepository;

    @InjectMocks
    private ReservationService reservationService;

    private Member member;
    private LoginMemberRequest loginMemberRequest;
    private ReservationTime time;
    private Theme theme;

    @BeforeEach
    void setup() {
        member = new Member(1L, "test", "test@email.com", "1234", Role.USER);
        loginMemberRequest = new LoginMemberRequest(member.getId(), member.getName(), member.getRole());
        time = new ReservationTime(1L, LocalTime.of(10, 0));
        theme = new Theme(1L, "name", "description", "thumbnail");
    }

    @Test
    void 예약_전체를_조회할_수_있다() {
        //given
        Reservation reservation = new Reservation(1L, member, LocalDate.now(), time, theme, ReservationStatus.RESERVED);
        Reservation reservation2 = new Reservation(2L, new Member(2L, "test2", "test2@email.com", "1234", Role.USER),
                LocalDate.now(), time, theme, ReservationStatus.RESERVED);

        when(reservationRepository.findAll())
                .thenReturn(List.of(reservation, reservation2));

        //when
        List<ReservationResponse> actual = reservationService.findAll();

        //then
        assertThat(actual).hasSize(2);
        assertThat(actual).extracting("name").containsExactlyInAnyOrder("test", "test2");
    }

    @Test
    void 예약을_추가한다() {
        //given
        CreateReservationRequest request = new CreateReservationRequest(LocalDate.now().plusDays(1),
                time.getId(), theme.getId());

        when(memberRepository.findFetchById(any(Long.class)))
                .thenReturn(Optional.of(member));
        when(reservationTimeRepository.findById(any(Long.class)))
                .thenReturn(Optional.of(time));
        when(themeRepository.findById(any(Long.class)))
                .thenReturn(Optional.of(theme));

        //when
        ReservationResponse actual = reservationService.addReservation(request, loginMemberRequest);

        //then
        assertAll(
                () -> assertThat(actual.time()).isEqualTo(time.getStartAt()),
                () -> assertThat(actual.themeName()).isEqualTo(theme.getName()),
                () -> assertThat(actual.name()).isEqualTo(member.getName())
        );
    }

    @Test
    void 예약을_삭제할_수_있다() {
        //given
        Reservation reservation = new Reservation(LocalDate.of(3000, 1, 1), time, theme, ReservationStatus.RESERVED);

        when(reservationRepository.findById(any(Long.class)))
                .thenReturn(Optional.of(reservation));
        when(memberRepository.findNextReserveMember(any(LocalDate.class), any(Long.class), any(Long.class),
                any(ReservationStatus.class), any(PageRequest.class)))
                .thenReturn(List.of());

        //when
        reservationService.deleteReservation(1L);

        //then
        assertThat(member.getReservations()).doesNotContain(reservation);
        assertThat(reservation.getMember()).isNull();
    }

    @Test
    @Transactional
    void 중복_예약은_불가능하다() {
        //given
        LocalDate targetDate = LocalDate.of(3000, 1, 1);
        member.reserve(targetDate, time, theme, ReservationStatus.RESERVED);

        when(memberRepository.findFetchById(any(Long.class)))
                .thenReturn(Optional.of(member));
        when(reservationTimeRepository.findById(any(Long.class)))
                .thenReturn(Optional.of(time));
        when(themeRepository.findById(any(Long.class)))
                .thenReturn(Optional.of(theme));

        //when & then
        assertThatThrownBy(() -> reservationService.addReservation(
                new CreateReservationRequest(targetDate, time.getId(), theme.getId()), loginMemberRequest))
                .isInstanceOf(InvalidReservationException.class);
    }

    @Test
    void 대상_유저의_예약_전체를_조회할_수_있다() {
        //given
        LocalDate date = LocalDate.of(3000, 1, 1);
        when(memberRepository.findFetchById(any(Long.class)))
                .thenReturn(Optional.of(member));
        when(reservationTimeRepository.findById(any(Long.class)))
                .thenReturn(Optional.of(time));
        when(themeRepository.findById(any(Long.class)))
                .thenReturn(Optional.of(theme));

        Reservation reservation2 = new Reservation(2L, new Member(2L, "test2", "test2@email.com", "1234", Role.USER),
                LocalDate.now(), time, theme, ReservationStatus.RESERVED);

        CreateReservationRequest request = new CreateReservationRequest(date, time.getId(), theme.getId());

        reservationService.addReservation(request, loginMemberRequest);

        //when
        List<MyReservationResponse> actual = reservationService.findAllReservationOfMember(1L);

        //then
        MyReservationResponse notContained = new MyReservationResponse(reservation2.getId(),
                reservation2.getTheme().getName(),
                reservation2.getDate(),
                reservation2.getStartAt(),
                reservation2.getStatus().renderText(0));

        assertAll(
                () -> assertThat(actual).hasSize(1),
                () -> assertThat(actual).doesNotContain(notContained)
        );
    }

    @Test
    void 예약_대기를_추가한다() {
        //given
        CreateWaitReservationRequest request = new CreateWaitReservationRequest(LocalDate.now().plusDays(1),
                time.getId(), theme.getId());

        when(memberRepository.findFetchById(any(Long.class)))
                .thenReturn(Optional.of(member));
        when(reservationTimeRepository.findById(any(Long.class)))
                .thenReturn(Optional.of(time));
        when(themeRepository.findById(any(Long.class)))
                .thenReturn(Optional.of(theme));

        //when
        ReservationWaitResponse actual = reservationService.addWaitReservation(request, loginMemberRequest);

        //then
        assertAll(
                () -> assertThat(actual.startAt()).isEqualTo(time.getStartAt()),
                () -> assertThat(actual.theme()).isEqualTo(theme.getName()),
                () -> assertThat(actual.name()).isEqualTo(member.getName())
        );
    }

    @Test
    void 예약_대기를_삭제할_수_있다() {
        //given
        Reservation reservation = new Reservation(LocalDate.of(3000, 1, 1), time, theme, ReservationStatus.WAIT);

        when(reservationRepository.findById(any(Long.class)))
                .thenReturn(Optional.of(reservation));

        //when
        reservationService.deleteReservation(1L);

        //then
        assertThat(member.getReservations()).doesNotContain(reservation);
        assertThat(reservation.getMember()).isNull();
    }

    @Test
    @Transactional
    void 중복_예약_대기는_불가능하다() {
        //given
        LocalDate targetDate = LocalDate.of(3000, 1, 1);
        member.reserve(targetDate, time, theme, ReservationStatus.WAIT);

        when(memberRepository.findFetchById(any(Long.class)))
                .thenReturn(Optional.of(member));
        when(reservationTimeRepository.findById(any(Long.class)))
                .thenReturn(Optional.of(time));
        when(themeRepository.findById(any(Long.class)))
                .thenReturn(Optional.of(theme));

        //when & then
        assertThatThrownBy(() -> reservationService.addWaitReservation(
                new CreateWaitReservationRequest(targetDate, time.getId(), theme.getId()), loginMemberRequest))
                .isInstanceOf(InvalidReservationException.class);
    }

    @Test
    void 기존_예약_삭제_시_첫번째_대기가_예약된다() {
        //given
        Member reserved = new Member(1L, "reserved", "reserved", "1234", Role.USER);
        LocalDate date = LocalDate.of(3000, 1, 1);

        Reservation reserve = reserved.reserve(date, time, theme, ReservationStatus.RESERVED);
        Reservation wait = member.reserve(date, time, theme, ReservationStatus.WAIT);

        when(reservationRepository.findById(any(Long.class)))
                .thenReturn(Optional.of(reserve));
        when(memberRepository.findNextReserveMember(any(LocalDate.class), any(Long.class), any(Long.class),
                any(ReservationStatus.class), any(PageRequest.class)))
                .thenReturn(List.of(member));

        //when
        reservationService.deleteReservation(1L);

        //then
        assertAll(
                () -> assertThat(member.getReservations()).contains(wait),
                () -> assertThat(wait.getMember()).isEqualTo(member),
                () -> assertThat(wait.getStatus()).isEqualTo(ReservationStatus.RESERVED)
        );
    }
}
