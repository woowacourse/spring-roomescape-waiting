package roomescape.reservation.service;

import org.springframework.stereotype.Service;
import roomescape.auth.dto.LoginMember;
import roomescape.exception.BadRequestException;
import roomescape.exception.ResourceNotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.MemberReservation;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.dto.MemberReservationCreateRequest;
import roomescape.reservation.dto.MyReservationResponse;
import roomescape.reservation.dto.ReservationCreateRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.repository.MemberReservationRepository;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.repository.ReservationTimeRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final MemberReservationRepository memberReservationRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository,
            MemberRepository memberRepository, MemberReservationRepository memberReservationRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.memberReservationRepository = memberReservationRepository;
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

    public ReservationResponse createReservation(MemberReservationCreateRequest request, LoginMember loginMember) {
        ReservationCreateRequest createRequest = ReservationCreateRequest.of(request, loginMember);
        return createReservation(createRequest);
    }

    public ReservationResponse createReservation(ReservationCreateRequest request) {
        Member member = findMemberById(request.memberId());
        Reservation reservation = reservationRepository.findByDateAndTimeIdAndThemeId(
                request.date(),
                request.timeId(),
                request.themeId()
        ).orElseGet(() -> {
            ReservationTime time = findReservationTimeById(request.timeId());
            Theme theme = findThemeById(request.themeId());
            return reservationRepository.save(new Reservation(request.date(), time, theme));
        });

        MemberReservation memberReservation = request.toMemberReservation(member, reservation);

        reservation.validateIsBeforeNow();
        validateDuplicated(memberReservation);

        MemberReservation savedMemberReservation = memberReservationRepository.save(memberReservation);
        return ReservationResponse.from(savedMemberReservation);
    }

    private void validateDuplicated(MemberReservation memberReservation) {
        if (memberReservation.getStatus().isWaiting()) {
            return;
        }

        Optional<MemberReservation> existsMemberReservation = memberReservationRepository.findByMemberAndReservationAndStatus(
                memberReservation.getMember(),
                memberReservation.getReservation(),
                ReservationStatus.CONFIRMATION
        );

        existsMemberReservation.ifPresent((duplicated) -> {
            if (duplicated.isSameMember(memberReservation.getMember())) {
                throw new BadRequestException("중복된 예약입니다.");
            }
            throw new BadRequestException("이미 예약된 테마입니다.");
        });
    }

    public List<ReservationResponse> readReservations() {
        return memberReservationRepository.findAll().stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<MyReservationResponse> readMemberReservations(LoginMember loginMember) {
        return memberReservationRepository.findByMemberId(loginMember.id()).stream()
                .map(MyReservationResponse::from)
                .toList();

    }

    public List<ReservationResponse> searchReservations(LocalDate start, LocalDate end, Long memberId, Long themeId) {
        List<Reservation> reservations = reservationRepository.findByDateBetweenAndThemeId(start, end, themeId);

        return memberReservationRepository.findByMemberIdAndReservations(memberId, reservations).stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public ReservationResponse readReservation(Long id) {
        MemberReservation memberReservation = memberReservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 예약입니다."));
        return ReservationResponse.from(memberReservation);
    }

    public void deleteReservation(Long id) {
        memberReservationRepository.deleteById(id);
    }
}
