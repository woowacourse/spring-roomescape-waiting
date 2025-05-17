package roomescape.time.service.usecase;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.common.exception.AlreadyExistException;
import roomescape.common.exception.NotFoundException;
import roomescape.common.exception.ReferencedByOtherException;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberEmail;
import roomescape.member.domain.MemberName;
import roomescape.member.domain.Role;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.repository.FakeReservationRepository;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.service.usecase.ReservationQueryUseCase;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeDescription;
import roomescape.theme.domain.ThemeName;
import roomescape.theme.domain.ThemeThumbnail;
import roomescape.time.domain.ReservationTime;
import roomescape.time.repository.FakeReservationTimeRepository;
import roomescape.time.repository.ReservationTimeRepository;
import roomescape.time.service.dto.CreateReservationTimeServiceRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationTimeCommandUseCaseTest {

    private ReservationTimeCommandUseCase reservationTimeCommandUseCase;

    private ReservationTimeRepository reservationTimeRepository;
    private ReservationRepository reservationRepository;

    @BeforeEach
    void setUp() {
        reservationTimeRepository = new FakeReservationTimeRepository();
        reservationRepository = new FakeReservationRepository();

        ReservationTimeQueryUseCase reservationTimeQueryUseCase = new ReservationTimeQueryUseCase(
                reservationTimeRepository);
        ReservationQueryUseCase reservationQueryUseCase = new ReservationQueryUseCase(reservationRepository,
                reservationTimeQueryUseCase);
        
        reservationTimeCommandUseCase = new ReservationTimeCommandUseCase(
                reservationTimeRepository,
                reservationQueryUseCase,
                reservationTimeQueryUseCase
        );
    }

    @Test
    @DisplayName("예약 시간을 생성할 수 있다")
    void createReservationTime() {
        // given
        final CreateReservationTimeServiceRequest request = new CreateReservationTimeServiceRequest(
                LocalTime.of(12, 30));

        // when
        final ReservationTime reservationTime = reservationTimeCommandUseCase.create(request);

        // then
        assertThat(reservationTime.getStartAt()).isEqualTo(LocalTime.of(12, 30));
        assertThat(reservationTimeRepository.findById(reservationTime.getId()))
                .isPresent();
    }

    @Test
    @DisplayName("예약 시간을 삭제할 수 있다")
    void deleteReservationTime() {
        // given
        final ReservationTime savedTime = reservationTimeRepository.save(
                ReservationTime.withoutId(LocalTime.of(23, 30)));
        final Long id = savedTime.getId();

        // when
        reservationTimeCommandUseCase.delete(id);

        // then
        assertThat(reservationTimeRepository.findById(id)).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 예약 시간을 삭제하려 하면 예외가 발생한다")
    void deleteNonExistentReservationTime() {
        // given
        final Long id = 100L;

        // when
        // then
        assertThatThrownBy(() -> reservationTimeCommandUseCase.delete(id))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("참조 중인 예약 시간을 삭제하려 하면 예외가 발생한다")
    void deleteRefReservationTime() {
        // given
        final ReservationTime savedTime = reservationTimeRepository.save(
                ReservationTime.withoutId(LocalTime.of(23, 30)));

        final Theme theme = Theme.withoutId(
                ThemeName.from("공포"),
                ThemeDescription.from("지구별 방탈출 최고"),
                ThemeThumbnail.from("www.making.com"));

        final Member member = Member.withoutId(MemberName.from("강산"),
                MemberEmail.from("123@gmail.com"),
                Role.MEMBER);

        final Reservation reservation = reservationRepository.save(Reservation.withoutId(
                member,
                ReservationDate.from(LocalDate.now().plusDays(1L)),
                savedTime,
                theme
        ));

        // when
        // then
        assertThatThrownBy(() -> reservationTimeCommandUseCase.delete(reservation.getTime().getId()))
                .isInstanceOf(ReferencedByOtherException.class)
                .hasMessage("예약에서 참조 중인 시간은 삭제할 수 없습니다.");
    }

    @Test
    @DisplayName("추가하려는 시간이 이미 존재한다면, 예외가 발생한다")
    void existsTime() {
        // given
        final ReservationTime savedTime = reservationTimeRepository.save(
                ReservationTime.withoutId(LocalTime.of(23, 30)));
        final CreateReservationTimeServiceRequest sameTimeRequest = new CreateReservationTimeServiceRequest(
                savedTime.getStartAt());

        // when & then
        assertThatThrownBy(() -> reservationTimeCommandUseCase.create(sameTimeRequest))
                .isInstanceOf(AlreadyExistException.class)
                .hasMessage("추가하려는 시간이 이미 존재합니다.");
    }

}
