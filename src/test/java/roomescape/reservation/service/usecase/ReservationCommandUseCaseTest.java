package roomescape.reservation.service.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.NoSuchElementException;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.ConflictException;
import roomescape.common.exception.NotFoundException;
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
class ReservationCommandUseCaseTest {

    @Autowired
    private ReservationCommandUseCase reservationCommandUseCase;
    
    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationWaitRepository reservationWaitRepository;
    
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    
    @Autowired
    private ThemeRepository themeRepository;
    
    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("예약을 생성할 수 있다")
    void createAndFindReservation() {
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
                        MemberName.from("강산"),
                        MemberEmail.from("123@gmail.com"),
                        Role.MEMBER
                )
        );

        final CreateReservationServiceRequest requestDto = new CreateReservationServiceRequest(
                member.getId(),
                LocalDate.of(2025, 8, 5),
                reservationTime.getId(),
                theme.getId());

        // when
        final Reservation reservation = reservationCommandUseCase.create(requestDto);

        // then
        final Reservation found = reservationRepository.findById(reservation.getId())
                .orElseThrow(NoSuchElementException::new);

        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(found.getId()).isEqualTo(reservation.getId());
            softAssertions.assertThat(found.getMember()).isEqualTo(reservation.getMember());
            softAssertions.assertThat(found.getDate()).isEqualTo(reservation.getDate());
            softAssertions.assertThat(found.getTime()).isEqualTo(reservation.getTime());
            softAssertions.assertThat(found.getTheme()).isEqualTo(reservation.getTheme());
        });
    }

    @Test
    @DisplayName("중복된 예약을 생성할 수 없다.")
    void existsReservation() {
        // given
        final Theme theme = themeRepository.save(Theme.withoutId(
                ThemeName.from("공포"),
                ThemeDescription.from("지구별 방탈출 최고"),
                ThemeThumbnail.from("www.making.com")));

        final Member member = memberRepository.save(
                Member.withoutId(
                        MemberName.from("강산"),
                        MemberEmail.from("123@gmail.com"),
                        Role.MEMBER
                )
        );

        final ReservationTime reservationTime = reservationTimeRepository.save(
                ReservationTime.withoutId(LocalTime.of(12, 27)));

        final Reservation savedReservation = reservationCommandUseCase.create(
                new CreateReservationServiceRequest(
                        member.getId(),
                        LocalDate.of(2025, 8, 10),
                        reservationTime.getId(),
                        theme.getId()
                ));

        // when & then
        assertThatThrownBy(() -> reservationCommandUseCase.create(
                new CreateReservationServiceRequest(
                        member.getId(),
                        LocalDate.of(2025, 8, 10),
                        reservationTime.getId(),
                        theme.getId())))
                .isInstanceOf(ConflictException.class)
                .hasMessage("추가하려는 예약이 이미 존재합니다.");
    }

    @Test
    @DisplayName("예약을 삭제할 수 있다")
    void deleteReservation() {
        // given
        final Theme theme = themeRepository.save(Theme.withoutId(
                ThemeName.from("공포"),
                ThemeDescription.from("지구별 방탈출 최고"),
                ThemeThumbnail.from("www.making.com")));

        final Member member = memberRepository.save(
                Member.withoutId(
                        MemberName.from("강산"),
                        MemberEmail.from("123@gmail.com"),
                        Role.MEMBER
                ));

        final ReservationTime reservationTime = reservationTimeRepository.save(
                ReservationTime.withoutId(LocalTime.of(12, 27)));

        final Reservation reservation = reservationRepository.save(
                Reservation.withoutId(
                        member,
                        ReservationDate.from(LocalDate.of(2025, 8, 10)),
                        reservationTime,
                        theme));

        // when
        reservationCommandUseCase.delete(reservation.getId());

        // then
        assertThat(reservationRepository.findById(reservation.getId()).isPresent())
                .isFalse();
    }

    @Test
    @DisplayName("존재하지 않는 예약을 삭제하려 하면 예외가 발생한다")
    void deleteNonExistentReservation() {
        // given
        final Long id = 1000L;

        // when & then
        assertThatThrownBy(() -> reservationCommandUseCase.delete(id))
                .isInstanceOf(NotFoundException.class);
    }

    @DisplayName("예약 삭제 후 예약 대기가 있다면, 가장 첫 번째 예약 대기가 삭제되고 예약으로 승격된다.")
    @Test
    void deleteAndPromotionReservationWait() {
        // given
        final Theme theme = themeRepository.save(Theme.withoutId(
                ThemeName.from("공포"),
                ThemeDescription.from("지구별 방탈출 최고"),
                ThemeThumbnail.from("www.making.com")));

        final Member reservationMember = memberRepository.save(
                Member.withoutId(
                        MemberName.from("강산"),
                        MemberEmail.from("123@gmail.com"),
                        Role.MEMBER
                ));

        final ReservationTime reservationTime = reservationTimeRepository.save(
                ReservationTime.withoutId(LocalTime.of(12, 27)));

        final Reservation reservation = reservationRepository.save(
                Reservation.withoutId(
                        reservationMember,
                        ReservationDate.from(LocalDate.of(2025, 8, 10)),
                        reservationTime,
                        theme));

        final Member reservationWaitMember = memberRepository.save(
                Member.withoutId(
                        MemberName.from("호떡"),
                        MemberEmail.from("456@gmail.com"),
                        Role.MEMBER
                ));

        final ReservationWait reservationWait = reservationWaitRepository.save(
                ReservationWait.withoutId(
                        reservationWaitMember,
                        ReservationDate.from(LocalDate.of(2025, 8, 10)),
                        reservationTime,
                        theme
                ));

        // when
        reservationCommandUseCase.delete(reservation.getId());

        // then
        assertAll(
                () -> assertThat(reservationWaitRepository.findAll()).isEmpty(),
                () -> assertThat(reservationRepository.findAll().get(0))
                        .extracting(Reservation::getInfo)
                        .isEqualTo(reservationWait.getInfo())
        );
    }
}
