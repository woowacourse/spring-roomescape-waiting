package roomescape.reservation.service;

import org.springframework.stereotype.Service;
import roomescape.auth.dto.LoginMember;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.MemberReservationCreateRequest;
import roomescape.reservation.dto.ReservationCreateRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.member.repository.MemberRepository;
import roomescape.exception.BadRequestException;
import roomescape.exception.ResourceNotFoundException;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.repository.ReservationTimeRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository,
            MemberRepository memberRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
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
        ReservationTime time = findReservationTimeById(request.timeId());
        Theme theme = findThemeById(request.themeId());

        Reservation reservation = request.toReservation(member, time, theme);

        validateDuplicated(reservation);
        validateRequestedTime(reservation, time);

        Reservation savedReservation = reservationRepository.save(reservation);
        return ReservationResponse.from(savedReservation);
    }

    private void validateDuplicated(Reservation reservation) {
        Optional<Reservation> existsReservation = reservationRepository.findByDateAndTimeIdAndThemeId(
                reservation.getDate(),
                reservation.getTime().getId(),
                reservation.getTheme().getId()
        );

        existsReservation.ifPresent((duplicated) -> {
            if (duplicated.isSameMember(reservation)) {
                throw new BadRequestException("중복된 예약입니다.");
            }
            throw new BadRequestException("이미 예약된 테마입니다.");
        });
    }

    private void validateRequestedTime(Reservation reservation, ReservationTime reservationTime) {
        LocalDateTime requestedDateTime = LocalDateTime.of(reservation.getDate(), reservationTime.getStartAt());
        if (requestedDateTime.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("이미 지난 날짜는 예약할 수 없습니다.");
        }
    }

    public List<ReservationResponse> readReservations() {
        return reservationRepository.findAll().stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> searchReservations(LocalDate start, LocalDate end, Long memberId, Long themeId) {
        return reservationRepository.findByDateBetweenAndMemberIdAndThemeId(start, end, memberId, themeId).stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public ReservationResponse readReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 예약입니다."));
        return ReservationResponse.from(reservation);
    }

    public void deleteReservation(Long id) {
        int deletedCount = reservationRepository.deleteById(id);
        if (deletedCount == 0) {
            throw new ResourceNotFoundException("존재하지 않는 예약입니다.");
        }
    }
}
