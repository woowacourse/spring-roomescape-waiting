package roomescape.reservation.service;

import org.springframework.stereotype.Service;
import roomescape.exception.ResourceNotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.MemberReservation;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.dto.MemberReservationResponse;
import roomescape.reservation.dto.ReservationCreateRequest;
import roomescape.reservation.repository.MemberReservationRepository;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.repository.ReservationTimeRepository;

import java.time.LocalDate;

@Service
public class ReservationCreateService {

    private final MemberReservationRepository memberReservationRepository;
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public ReservationCreateService(MemberReservationRepository memberReservationRepository,
                                    ReservationRepository reservationRepository,
                                    MemberRepository memberRepository,
                                    ReservationTimeRepository reservationTimeRepository,
                                    ThemeRepository themeRepository) {
        this.memberReservationRepository = memberReservationRepository;
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자입니다."));
    }

    private ReservationTime findReservationTimeById(Long id) {
        return reservationTimeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 예약 시간입니다."));
    }

    private Theme findThemeById(Long id) {
        return themeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 테마입니다."));
    }

    public MemberReservationResponse createReservation(ReservationCreateRequest request) {
        Member member = findMemberById(request.memberId());
        Reservation reservation = findReservationOrSave(request);

        MemberReservation memberReservation = request.toMemberReservation(member, reservation);
        reservation.validateIsBeforeNow();
        validateDuplicated(memberReservation);

        MemberReservation savedMemberReservation = memberReservationRepository.save(memberReservation);
        return MemberReservationResponse.from(savedMemberReservation);
    }

    private Reservation findReservationOrSave(ReservationCreateRequest request) {
        Long timeId = request.timeId();
        Long themeId = request.themeId();
        LocalDate date = request.date();

        return reservationRepository.findByDateAndTimeIdAndThemeId(date, timeId, themeId)
                .orElseGet(() -> createReservation(timeId, themeId, date));
    }

    private Reservation createReservation(Long timeId, Long themeId, LocalDate date) {
        ReservationTime time = findReservationTimeById(timeId);
        Theme theme = findThemeById(themeId);

        Reservation reservation = new Reservation(date, time, theme);
        return reservationRepository.save(reservation);
    }

    private void validateDuplicated(MemberReservation memberReservation) {
        if (memberReservation.isNotWaitingStatus()) {
            validateNotWaitingReservation(memberReservation);
            return;
        }

        validateWaitingReservation(memberReservation);
    }

    private void validateNotWaitingReservation(MemberReservation memberReservation) {
        memberReservationRepository.findByReservationAndStatus(
                        memberReservation.getReservation(),
                        ReservationStatus.CONFIRMATION)
                .ifPresent(memberReservation::validateDuplicated);
    }

    private void validateWaitingReservation(MemberReservation memberReservation) {
        memberReservationRepository.findByReservationAndMember(
                        memberReservation.getReservation(),
                        memberReservation.getMember())
                .ifPresent(memberReservation::validateDuplicated);
    }
}
