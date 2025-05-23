package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import roomescape.model.Waiting;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitingRepository;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final WaitingRepository waitingRepository;

    @Transactional
    public ReservationResponseDto saveReservation(ReservationRegisterDto reservationRegisterDto,
                                                  LoginMember loginMember) {
        Reservation reservation = createReservation(reservationRegisterDto, loginMember);
        assertReservationIsNotDuplicated(reservation);

        Reservation savedReservation = reservationRepository.save(reservation);
        return ReservationResponseDto.from(savedReservation);
    }

    public List<ReservationResponseDto> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(ReservationResponseDto::from)
                .toList();
    }

    public List<ReservationResponseDto> searchReservations(ReservationSearchDto reservationSearchDto) {
        Long themeId = reservationSearchDto.themeId();
        Long memberId = reservationSearchDto.memberId();
        LocalDate startDate = reservationSearchDto.startDate();
        LocalDate endDate = reservationSearchDto.endDate();

        return reservationRepository.findByThemeIdAndMemberIdAndDateBetween(
                        themeId,
                        memberId,
                        startDate,
                        endDate).stream()
                .map(ReservationResponseDto::from)
                .toList();
    }

    @Transactional
    public void cancelReservation(Long id) {
        reservationRepository.deleteById(id);
    }

    public List<MemberReservationResponseDto> getReservationsOfMember(LoginMember loginMember) {
        List<Reservation> reservations = reservationRepository.findAllByMemberId(loginMember.id());
        List<Waiting> waitingList = waitingRepository.findAllByMemberId(loginMember.id());

        return getMemberReservationResponseDtos(reservations, waitingList);
    }

    private List<MemberReservationResponseDto> getMemberReservationResponseDtos(final List<Reservation> reservations,
                                                                                final List<Waiting> waitingList) {
        List<MemberReservationResponseDto> reservationResponse = reservations.stream()
                .map(MemberReservationResponseDto::from)
                .toList();

        List<MemberReservationResponseDto> waitingResponse = waitingList.stream()
                .map(MemberReservationResponseDto::from)
                .toList();

        return Stream.concat(reservationResponse.stream(), waitingResponse.stream())
                .toList();
    }

    private Reservation createReservation(ReservationRegisterDto reservationRegisterDto, LoginMember loginMember) {
        ReservationTime time = findTimeById(reservationRegisterDto.timeId());
        Theme theme = findThemeById(reservationRegisterDto.themeId());
        Member member = findMemberById(loginMember.id());

        return reservationRegisterDto.convertToReservation(time, theme, member);
    }

    private ReservationTime findTimeById(final Long id) {
        return reservationTimeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("id 에 해당하는 예약 시각이 존재하지 않습니다."));
    }

    private Theme findThemeById(final Long id) {
        return themeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("id 에 해당하는 테마가 존재하지 않습니다."));
    }

    private Member findMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자에 대한 예약 요청입니다."));
    }

    private void assertReservationIsNotDuplicated(Reservation reservation) {
        reservationRepository.findByDateAndReservationTime(reservation.getDate(), reservation.getReservationTime())
                .ifPresent(foundReservation -> {
                    throw new DuplicatedException("이미 예약이 존재합니다.");
                });
    }

}
