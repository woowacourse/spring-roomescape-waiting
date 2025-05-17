package roomescape.reservation.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.global.error.exception.BadRequestException;
import roomescape.global.error.exception.ConflictException;
import roomescape.global.error.exception.NotFoundException;
import roomescape.member.entity.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.dto.request.ReservationAdminCreateRequest;
import roomescape.reservation.dto.request.ReservationCreateRequest;
import roomescape.reservation.dto.request.ReservationReadFilteredRequest;
import roomescape.reservation.dto.response.ReservationAdminCreateResponse;
import roomescape.reservation.dto.response.ReservationCreateResponse;
import roomescape.reservation.dto.response.ReservationReadFilteredResponse;
import roomescape.reservation.dto.response.ReservationReadMemberResponse;
import roomescape.reservation.dto.response.ReservationReadResponse;
import roomescape.reservation.entity.Reservation;
import roomescape.reservation.entity.ReservationTime;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.theme.entity.Theme;
import roomescape.theme.repository.ThemeRepository;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public ReservationCreateResponse createReservation(Long memberId, ReservationCreateRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 멤버 입니다."));
        ReservationTime time = reservationTimeRepository.findById(request.timeId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 시간 입니다."));
        Theme theme = themeRepository.findById(request.themeId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 테마 입니다."));

        Reservation newReservation = new Reservation(request.date(), time, theme, member);
        validateDateTime(newReservation);
        validateDuplicated(newReservation);

        Reservation saved = reservationRepository.save(newReservation);
        return ReservationCreateResponse.from(saved, theme);
    }

    public ReservationAdminCreateResponse createReservationByAdmin(ReservationAdminCreateRequest request) {
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 멤버 입니다."));
        ReservationTime time = reservationTimeRepository.findById(request.timeId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 시간 입니다."));
        Theme theme = themeRepository.findById(request.themeId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 테마 입니다."));

        Reservation newReservation = new Reservation(request.date(), time, theme, member);
        validateDateTime(newReservation);
        validateDuplicated(newReservation);

        Reservation reservation = reservationRepository.save(newReservation);
        return ReservationAdminCreateResponse.from(reservation, theme);
    }

    public List<ReservationReadResponse> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(ReservationReadResponse::from)
                .toList();
    }

    public List<ReservationReadFilteredResponse> getFilteredReservations(ReservationReadFilteredRequest request) {
        List<Reservation> reservations = reservationRepository.findAllByThemeIdAndMemberIdAndDateBetween(
                request.themeId(), request.memberId(), request.dateFrom(), request.dateTo()
        );

        return reservations.stream()
                .map(ReservationReadFilteredResponse::from)
                .toList();
    }

    public List<ReservationReadMemberResponse> getReservationsByMember(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 멤버 입니다."));
        List<Reservation> reservations = reservationRepository.findAllByMember(member);
        return reservations.stream()
                .map(ReservationReadMemberResponse::from)
                .toList();
    }

    public void deleteReservation(Long id) {
        reservationRepository.deleteById(id);
    }

    private void validateDateTime(Reservation reservation) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reservationDateTime = reservation.getDateTime();
        if (reservationDateTime.isBefore(now)) {
            throw new BadRequestException("과거 날짜는 예약할 수 없습니다.");
        }
    }

    private void validateDuplicated(Reservation reservation) {
        if (reservationRepository.existsByDateAndTimeId(reservation.getDate(), reservation.getTime().getId())) {
            throw new ConflictException("해당 날짜와 시간에 이미 예약이 존재합니다.");
        }
    }
}
