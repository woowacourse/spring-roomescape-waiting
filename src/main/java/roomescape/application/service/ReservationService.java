package roomescape.application.service;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.common.exception.DuplicatedException;
import roomescape.common.exception.NotFoundException;
import roomescape.dto.LoginMember;
import roomescape.dto.request.ReservationRegisterDto;
import roomescape.dto.request.ReservationSearchDto;
import roomescape.dto.response.MemberReservationResponseDto;
import roomescape.dto.response.ReservationResponseDto;
import roomescape.infrastructure.db.ReservationTimeJpaRepository;
import roomescape.model.Member;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.model.Theme;
import roomescape.persistence.MemberRepository;
import roomescape.persistence.ReservationRepository;
import roomescape.persistence.ThemeRepository;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeJpaRepository reservationTimeJpaRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public ReservationResponseDto saveReservation(ReservationRegisterDto reservationRegisterDto,
                                                  LoginMember loginMember) {
        Reservation reservation = createReservation(reservationRegisterDto, loginMember);
        assertReservationIsNotDuplicated(reservation);

        Reservation savedReservation = reservationRepository.saveReservation(reservation);
        return new ReservationResponseDto(savedReservation);
    }

    public List<ReservationResponseDto> getAllReservations() {
        return reservationRepository.findAllReservations().stream()
                .map(ReservationResponseDto::new)
                .toList();
    }

    public List<ReservationResponseDto> searchReservations(ReservationSearchDto reservationSearchDto) {
        Long themeId = reservationSearchDto.themeId();
        Long memberId = reservationSearchDto.memberId();
        LocalDate startDate = reservationSearchDto.startDate();
        LocalDate endDate = reservationSearchDto.endDate();

        return reservationRepository.findReservationsForThemeAndMemberInPeriod(
                        themeId,
                        memberId,
                        startDate,
                        endDate).stream()
                .map(ReservationResponseDto::new)
                .toList();
    }

    public void cancelReservation(Long id) {
        reservationRepository.deleteById(id);
    }

    public List<MemberReservationResponseDto> getReservationsOfMember(LoginMember loginMember) {
        List<Reservation> reservations = reservationRepository.findReservationForMember(loginMember.id());

        return reservations.stream()
                .map(MemberReservationResponseDto::new)
                .toList();
    }

    private Reservation createReservation(ReservationRegisterDto reservationRegisterDto, LoginMember loginMember) {
        ReservationTime time = findTimeById(reservationRegisterDto.timeId());
        Theme theme = themeRepository.findById(reservationRegisterDto.themeId());
        Member member = memberRepository.findById(loginMember.id());

        return reservationRegisterDto.convertToReservation(time, theme, member);
    }

    private void assertReservationIsNotDuplicated(Reservation reservation) {
        reservationRepository.findDuplicatedReservationByDateAndTime(reservation.getDate(),
                        reservation.getReservationTime())
                .ifPresent(foundReservation -> {
                    throw new DuplicatedException("이미 예약이 존재합니다.");
                });
    }

    private ReservationTime findTimeById(final Long id) {
        return reservationTimeJpaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("id 에 해당하는 예약 시각이 존재하지 않습니다."));
    }
}
