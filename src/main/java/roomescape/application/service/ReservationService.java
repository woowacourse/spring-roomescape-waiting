package roomescape.application.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.common.exception.DuplicatedException;
import roomescape.common.exception.NotFoundException;
import roomescape.dto.LoginMember;
import roomescape.dto.request.ReservationRegisterDto;
import roomescape.dto.request.ReservationSearchDto;
import roomescape.dto.response.MemberReservationResponseDto;
import roomescape.dto.response.ReservationResponseDto;
import roomescape.model.Member;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.model.Theme;
import roomescape.infrastructure.db.MemberJpaRepository;
import roomescape.infrastructure.db.ReservationJpaRepository;
import roomescape.infrastructure.db.ReservationTimeJpaRepository;
import roomescape.infrastructure.db.ThemeJpaRepository;

@Service
public class ReservationService {

    private final ReservationJpaRepository reservationJpaRepository;
    private final ReservationTimeJpaRepository reservationTimeJpaRepository;
    private final ThemeJpaRepository themeJpaRepository;
    private final MemberRepository memberRepository;

    public ReservationResponseDto saveReservation(ReservationRegisterDto reservationRegisterDto,
                                                  LoginMember loginMember) {
        Reservation reservation = createReservation(reservationRegisterDto, loginMember);
        assertReservationIsNotDuplicated(reservation);

        Reservation savedReservation = reservationJpaRepository.save(reservation);
        return new ReservationResponseDto(savedReservation);
    }

    public List<ReservationResponseDto> getAllReservations() {
        return reservationJpaRepository.findAll().stream()
                .map(ReservationResponseDto::new)
                .toList();
    }

    public List<ReservationResponseDto> searchReservations(ReservationSearchDto reservationSearchDto) {
        Long themeId = reservationSearchDto.themeId();
        Long memberId = reservationSearchDto.memberId();
        LocalDate startDate = reservationSearchDto.startDate();
        LocalDate endDate = reservationSearchDto.endDate();

        return reservationJpaRepository.findByThemeIdAndMemberIdAndDateBetween(
                        themeId,
                        memberId,
                        startDate,
                        endDate).stream()
                .map(ReservationResponseDto::new)
                .toList();
    }

    public void cancelReservation(Long id) {
        reservationJpaRepository.deleteById(id);
    }

    public List<MemberReservationResponseDto> getReservationsOfMember(LoginMember loginMember) {
        List<Reservation> reservations = reservationJpaRepository.findByMemberId(loginMember.id());

        return reservations.stream()
                .map(MemberReservationResponseDto::new)
                .toList();
    }

    private Reservation createReservation(ReservationRegisterDto reservationRegisterDto, LoginMember loginMember) {
        ReservationTime time = findTimeById(reservationRegisterDto.timeId());
        Theme theme = findThemeById(reservationRegisterDto.themeId());
        Member member = memberRepository.findById(loginMember.id());

        return reservationRegisterDto.convertToReservation(time, theme, member);
    }

    private ReservationTime findTimeById(final Long id) {
        return reservationTimeJpaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("id 에 해당하는 예약 시각이 존재하지 않습니다."));
    }

    private Theme findThemeById(final Long id) {
        return themeJpaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("id 에 해당하는 테마가 존재하지 않습니다."));
    }

    private Member findMemberById(Long id) {
        return memberJpaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자에 대한 예약 요청입니다."));
    }

    private void assertReservationIsNotDuplicated(Reservation reservation) {
        reservationJpaRepository.findByDateAndReservationTime(reservation.getDate(), reservation.getReservationTime())
                .ifPresent(foundReservation -> {
                    throw new DuplicatedException("이미 예약이 존재합니다.");
                });
    }

}
