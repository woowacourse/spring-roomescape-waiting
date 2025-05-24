package roomescape.reservation.service.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.ConflictException;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberEmail;
import roomescape.member.domain.MemberName;
import roomescape.member.domain.Role;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationWait;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationWaitRepository;
import roomescape.reservation.service.dto.CreateReservationServiceRequest;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeDescription;
import roomescape.theme.domain.ThemeName;
import roomescape.theme.domain.ThemeThumbnail;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.repository.ReservationTimeRepository;

@SpringBootTest
@Transactional
class ReservationWaitCommandUseCaseTest {

    @Autowired
    private ReservationWaitCommandUseCase reservationWaitCommandUseCase;

    @Autowired
    private ReservationWaitRepository reservationWaitRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @DisplayName("예약 대기를 생성한다.")
    @Test
    void create() {
        // given
        final ReservationTime reservationTime = reservationTimeRepository.save(
                ReservationTime.withoutId(LocalTime.of(12, 27)));

        final Theme theme = themeRepository.save(
                Theme.withoutId(
                        ThemeName.from("공포"),
                        ThemeDescription.from("지구별 방탈출 최고"),
                        ThemeThumbnail.from("www.making.com")));

        final Member reservationMember = memberRepository.save(
                Member.withoutId(
                        MemberName.from("시소"),
                        MemberEmail.from("123@gmail.com"),
                        Role.MEMBER
                )
        );

        final Member reservationWaitMember = memberRepository.save(
                Member.withoutId(
                        MemberName.from("호떡"),
                        MemberEmail.from("456@gmail.com"),
                        Role.MEMBER
                )
        );

        final Reservation reservation = Reservation.withoutId(
                reservationMember,
                ReservationDate.from(LocalDate.MAX),
                reservationTime,
                theme
        );

        reservationRepository.save(reservation);

        final CreateReservationServiceRequest requestDto = new CreateReservationServiceRequest(
                reservationWaitMember.getId(),
                LocalDate.MAX,
                reservationTime.getId(),
                theme.getId()
        );

        // when
        final ReservationWait reservationWait = reservationWaitCommandUseCase.create(requestDto);

        // then
        final ReservationWait found = reservationWaitRepository.findAll().get(0);

        assertAll(
                () -> assertThat(found.getId()).isEqualTo(reservationWait.getId()),
                () -> assertThat(found.getMember()).isEqualTo(reservationWaitMember),
                () -> assertThat(found.getDate()).isEqualTo(ReservationDate.from(LocalDate.MAX)),
                () -> assertThat(found.getTime()).isEqualTo(reservationTime),
                () -> assertThat(found.getTheme()).isEqualTo(theme)
        );
    }

    @DisplayName("이미 예약을 한 사용자는 예약 대기를 할 수 없다.")
    @Test
    void validateReservationExists() {
        // given
        final ReservationTime reservationTime = reservationTimeRepository.save(
                ReservationTime.withoutId(LocalTime.of(12, 27)));

        final Theme theme = themeRepository.save(
                Theme.withoutId(
                        ThemeName.from("공포"),
                        ThemeDescription.from("지구별 방탈출 최고"),
                        ThemeThumbnail.from("www.making.com")));

        final Member member = memberRepository.save(
                Member.withoutId(
                        MemberName.from("호떡"),
                        MemberEmail.from("456@gmail.com"),
                        Role.MEMBER
                )
        );

        final Reservation reservation = Reservation.withoutId(
                member,
                ReservationDate.from(LocalDate.MAX),
                reservationTime,
                theme
        );

        reservationRepository.save(reservation);

        final CreateReservationServiceRequest requestDto = new CreateReservationServiceRequest(
                member.getId(),
                LocalDate.MAX,
                reservationTime.getId(),
                theme.getId()
        );

        // when & then
        assertThatThrownBy(() -> reservationWaitCommandUseCase.create(requestDto))
                .isInstanceOf(ConflictException.class)
                .hasMessage("이미 해당 예약을 한 사용자입니다.");
    }

    @DisplayName("이미 해당 예약 대기를 한 사용자는 예약 대기를 할 수 없다.")
    @Test
    void validateReservationWaitNotExistsForMember() {
        // given
        final ReservationTime reservationTime = reservationTimeRepository.save(
                ReservationTime.withoutId(LocalTime.of(12, 27)));

        final Theme theme = themeRepository.save(
                Theme.withoutId(
                        ThemeName.from("공포"),
                        ThemeDescription.from("지구별 방탈출 최고"),
                        ThemeThumbnail.from("www.making.com")));

        final Member reservationMember = memberRepository.save(
                Member.withoutId(
                        MemberName.from("시소"),
                        MemberEmail.from("123@gmail.com"),
                        Role.MEMBER
                )
        );

        final Member reservationWaitMember = memberRepository.save(
                Member.withoutId(
                        MemberName.from("호떡"),
                        MemberEmail.from("456@gmail.com"),
                        Role.MEMBER
                )
        );

        final Reservation reservation = Reservation.withoutId(
                reservationMember,
                ReservationDate.from(LocalDate.MAX),
                reservationTime,
                theme
        );

        reservationRepository.save(reservation);

        final CreateReservationServiceRequest requestDto = new CreateReservationServiceRequest(
                reservationWaitMember.getId(),
                LocalDate.MAX,
                reservationTime.getId(),
                theme.getId()
        );

        reservationWaitCommandUseCase.create(requestDto);

        // when & then
        assertThatThrownBy(() -> reservationWaitCommandUseCase.create(requestDto))
                .isInstanceOf(ConflictException.class)
                .hasMessage("이미 해당 예약 대기를 한 사용자입니다.");
    }
}
