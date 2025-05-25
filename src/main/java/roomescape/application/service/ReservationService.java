package roomescape.application.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.common.exception.DuplicatedException;
import roomescape.dto.LoginMember;
import roomescape.dto.request.ReservationRegisterDto;
import roomescape.dto.request.ReservationSearchDto;
import roomescape.dto.response.MemberReservationResponseDto;
import roomescape.dto.response.ReservationResponseDto;
import roomescape.model.Member;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.model.Theme;
import roomescape.model.Waiting;
import roomescape.persistence.repository.MemberRepository;
import roomescape.persistence.repository.ReservationRepository;
import roomescape.persistence.repository.ReservationTimeRepository;
import roomescape.persistence.repository.ThemeRepository;
import roomescape.persistence.repository.WaitingRepository;
import roomescape.persistence.vo.Period;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final WaitingRepository waitingRepository;

    public ReservationResponseDto saveReservation(ReservationRegisterDto reservationRegisterDto,
                                                  LoginMember loginMember) {
        Reservation reservation = createReservation(reservationRegisterDto, loginMember);
        assertReservationIsNotDuplicated(reservation);

        Reservation savedReservation = reservationRepository.save(reservation);
        return new ReservationResponseDto(savedReservation);
    }

    public List<ReservationResponseDto> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(ReservationResponseDto::new)
                .toList();
    }

    public List<ReservationResponseDto> searchReservations(ReservationSearchDto reservationSearchDto) {
        Long themeId = reservationSearchDto.themeId();
        Long memberId = reservationSearchDto.memberId();
        LocalDate startDate = reservationSearchDto.startDate();
        LocalDate endDate = reservationSearchDto.endDate();

        return reservationRepository.findForThemeAndMemberInPeriod(
                        themeId,
                        memberId,
                        new Period(startDate, endDate)
                ).stream()
                .map(ReservationResponseDto::new)
                .toList();
    }

    public void cancelReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id);
        reservationRepository.deleteById(id);

        promoteNextWaitingToReservation(reservation);
    }

    private void promoteNextWaitingToReservation(Reservation reservation) {
        Optional<Waiting> optionalNextWaiting = waitingRepository.findNextWaiting(
                reservation.getDate(),
                reservation.getReservationTime(),
                reservation.getTheme()
        );

        if (optionalNextWaiting.isEmpty()) {
            return;
        }

        Waiting nextWaiting = optionalNextWaiting.get();

        promoteToReservation(nextWaiting);
        waitingRepository.delete(nextWaiting);
    }

    private void promoteToReservation(Waiting nextWaiting) {
        Reservation convertedReservation = convertToReservation(nextWaiting);
        reservationRepository.save(convertedReservation);
    }

    public List<MemberReservationResponseDto> getReservationsOfMember(LoginMember loginMember) {
        List<Reservation> reservations = reservationRepository.findForMember(loginMember.id());

        return reservations.stream()
                .map(MemberReservationResponseDto::new)
                .toList();
    }

    private Reservation createReservation(ReservationRegisterDto reservationRegisterDto, LoginMember loginMember) {
        ReservationTime time = reservationTimeRepository.findById(reservationRegisterDto.timeId());
        Theme theme = themeRepository.findById(reservationRegisterDto.themeId());
        Member member = memberRepository.findById(loginMember.id());

        return reservationRegisterDto.convertToReservation(time, theme, member);
    }

    private void assertReservationIsNotDuplicated(Reservation reservation) {
        if (reservationRepository.isDuplicatedForDateAndReservationTime(reservation.getDate(),
                reservation.getReservationTime())) {
            throw new DuplicatedException("이미 예약이 존재합니다.");
        }
    }

    private Reservation convertToReservation(Waiting nextWaiting) {
        return new Reservation(
                nextWaiting.getReservationDate(),
                nextWaiting.getReservationTime(),
                nextWaiting.getTheme(),
                nextWaiting.getPendingReservation().getMember(),
                nextWaiting.getRegisteredAt().toLocalDate()
        );
    }
}
