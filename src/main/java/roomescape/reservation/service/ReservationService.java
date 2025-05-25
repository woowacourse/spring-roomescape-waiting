package roomescape.reservation.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import roomescape.reservation.entity.Waiting;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.repository.WaitingRepository;
import roomescape.theme.entity.Theme;
import roomescape.theme.repository.ThemeRepository;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final WaitingRepository waitingRepository;

    @Transactional
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

        Reservation reservation = reservationRepository.save(newReservation);
        return ReservationCreateResponse.from(reservation);
    }

    @Transactional
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
        return ReservationAdminCreateResponse.from(reservation);
    }

    @Transactional(readOnly = true)
    public List<ReservationReadResponse> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(ReservationReadResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationReadFilteredResponse> getFilteredReservations(ReservationReadFilteredRequest request) {
        List<Reservation> reservations = reservationRepository.findAllFiltered(
                request.themeId(), request.memberId(), request.dateFrom(), request.dateTo()
        );
        return reservations.stream()
                .map(ReservationReadFilteredResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationReadMemberResponse> getReservationsByMember(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 멤버 입니다."));

        List<ReservationReadMemberResponse> responses = new ArrayList<>();

        getReservationByMember(member, responses);
        getWaitingByMember(member, responses);

        responses.sort(Comparator
                .comparing(ReservationReadMemberResponse::date)
                .thenComparing(ReservationReadMemberResponse::time));

        return responses;
    }

    private void getReservationByMember(Member member, List<ReservationReadMemberResponse> responses) {
        reservationRepository.findAllByMember(member).stream()
                .map(ReservationReadMemberResponse::from)
                .forEach(responses::add);
    }

    private void getWaitingByMember(Member member, List<ReservationReadMemberResponse> responses) {
        for (Waiting waiting : waitingRepository.findAllByMember(member)) {
            List<Waiting> waitings = waitingRepository.findAllByDateAndTimeIdAndThemeIdOrderByCreatedAt(
                    waiting.getDate(), waiting.getTime().getId(), waiting.getTheme().getId());
            int position = 0;
            for (int i = 0; i < waitings.size(); i++) {
                if (waitings.get(i).getId().equals(waiting.getId())) {
                    position = i;
                    break;
                }
            }
            responses.add(ReservationReadMemberResponse.from(waiting, position));
        }
    }

    @Transactional
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
        if (reservationRepository.existsByDateAndTimeIdAndThemeId(
                reservation.getDate(),
                reservation.getTime().getId(),
                reservation.getTheme().getId()
        )) {
            throw new ConflictException("중복된 예약입니다.");
        }
    }
}
