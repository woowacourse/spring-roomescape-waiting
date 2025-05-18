package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.Member;
import roomescape.domain.MemberRole;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.exception.NotFoundReservationException;
import roomescape.exception.NotFoundReservationTimeException;
import roomescape.persistence.MemberRepository;
import roomescape.persistence.ReservationRepository;
import roomescape.persistence.ReservationTimeRepository;
import roomescape.persistence.ThemeRepository;
import roomescape.service.param.CreateReservationParam;
import roomescape.service.result.MemberResult;
import roomescape.service.result.ReservationResult;
import roomescape.service.result.ReservationTimeResult;
import roomescape.service.result.ThemeResult;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    public static final LocalDate RESERVATION_DATE = LocalDate.now().plusDays(1);

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private ReservationTimeRepository reservationTimeRepository;
    @Mock
    private ThemeRepository themeRepository;
    @Mock
    private MemberRepository memberRepository;
    @InjectMocks
    private ReservationService reservationService;

    @Test
    void 예약을_생성한다() {
        // given
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(12, 0));
        Theme theme = new Theme(1L, "test", "description", "thumbnail");
        Member member = new Member(1L, "name", MemberRole.USER, "email@email.com", "Password1!");
        CreateReservationParam param = new CreateReservationParam(1L, RESERVATION_DATE, 1L, 1L);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(reservationTimeRepository.findById(1L)).thenReturn(Optional.of(reservationTime));
        when(themeRepository.findById(1L)).thenReturn(Optional.of(theme));
        when(reservationRepository.existsByDateAndTimeIdAndThemeId(RESERVATION_DATE, 1L, 1L)).thenReturn(false);
        Reservation saved = new Reservation(1L, member, RESERVATION_DATE, reservationTime, theme,
                ReservationStatus.RESERVED);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(saved);

        // when
        Long createdId = reservationService.create(param, LocalDateTime.now());

        // then
        assertThat(createdId).isEqualTo(1L);
    }

    @Test
    void 예약을_생성할때_timeId가_데이터베이스에_존재하지_않는다면_예외가_발생한다() {
        // given
        CreateReservationParam param = new CreateReservationParam(1L, RESERVATION_DATE, 1L, 1L);
        when(reservationTimeRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reservationService.create(param, LocalDateTime.now()))
                .isInstanceOf(NotFoundReservationTimeException.class)
                .hasMessageContaining("1에 해당하는 정보가 없습니다.");
    }

    @Test
    void 전체_예약을_조회할_수_있다() {
        // given
        Theme theme = new Theme(1L, "test", "description", "thumbnail");
        ReservationTime reservationTime1 = new ReservationTime(1L, LocalTime.of(12, 1));
        ReservationTime reservationTime2 = new ReservationTime(2L, LocalTime.of(13, 1));
        Member member1 = new Member(1L, "name1", MemberRole.USER, "email1@email.com", "Password1!");
        Member member2 = new Member(2L, "name2", MemberRole.USER, "email2@email.com", "Password1!");

        Reservation reservation1 = new Reservation(1L, member1, RESERVATION_DATE, reservationTime1, theme,
                ReservationStatus.RESERVED);
        Reservation reservation2 = new Reservation(2L, member2, RESERVATION_DATE, reservationTime2, theme,
                ReservationStatus.RESERVED);

        when(reservationRepository.findAll()).thenReturn(List.of(reservation1, reservation2));

        // when
        List<ReservationResult> reservationResults = reservationService.findAll();

        // then
        assertThat(reservationResults).isEqualTo(List.of(
                new ReservationResult(1L, new MemberResult(1L, "name1", MemberRole.USER, "email1@email.com"),
                        RESERVATION_DATE,
                        new ReservationTimeResult(1L, LocalTime.of(12, 1)),
                        new ThemeResult(1L, "test", "description", "thumbnail")),
                new ReservationResult(2L, new MemberResult(2L, "name2", MemberRole.USER, "email2@email.com"),
                        RESERVATION_DATE,
                        new ReservationTimeResult(2L, LocalTime.of(13, 1)),
                        new ThemeResult(1L, "test", "description", "thumbnail"))
        ));
    }

    @Test
    void id_로_예약을_찾을_수_있다() {
        // given
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(12, 0));
        Theme theme = new Theme(1L, "test", "description", "thumbnail");
        Member member = new Member(1L, "name1", MemberRole.USER, "email1@email.com", "Password1!");
        Reservation reservation = new Reservation(1L, member, RESERVATION_DATE, reservationTime, theme,
                ReservationStatus.RESERVED);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        // when
        ReservationResult reservationResult = reservationService.findById(1L);

        // then
        assertThat(reservationResult).isEqualTo(
                new ReservationResult(1L, new MemberResult(1L, "name1", MemberRole.USER, "email1@email.com"),
                        RESERVATION_DATE,
                        new ReservationTimeResult(1L, LocalTime.of(12, 0)),
                        new ThemeResult(1L, "test", "description", "thumbnail"))
        );
    }

    @Test
    void id에_해당하는_예약이_없는경우_예외가_발생한다() {
        // given
        when(reservationRepository.findById(2L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reservationService.findById(2L))
                .isInstanceOf(NotFoundReservationException.class)
                .hasMessageContaining("2에 해당하는 reservation 튜플이 없습니다.");
    }
}
