package kr.co.olivepay.payment.service.Impl;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import kr.co.olivepay.core.card.dto.req.CardSearchReq;
import kr.co.olivepay.core.card.dto.res.PaymentCardSearchRes;
import kr.co.olivepay.core.card.dto.res.enums.CardType;
import kr.co.olivepay.core.franchise.dto.req.FranchiseIdListReq;
import kr.co.olivepay.core.franchise.dto.res.FranchiseMinimalRes;
import kr.co.olivepay.core.franchise.dto.res.FranchiseMyDonationRes;
import kr.co.olivepay.core.funding.dto.req.FundingCreateReq;
import kr.co.olivepay.core.global.dto.res.PageResponse;
import kr.co.olivepay.core.member.dto.req.UserPinCheckReq;
import kr.co.olivepay.core.member.dto.res.UserKeyRes;
import kr.co.olivepay.payment.client.CardServiceClient;
import kr.co.olivepay.payment.client.FranchiseServiceClient;
import kr.co.olivepay.payment.client.FundingServiceClient;
import kr.co.olivepay.payment.client.MemberServiceClient;
import kr.co.olivepay.payment.dto.req.PaymentCreateReq;
import kr.co.olivepay.payment.dto.res.PaymentHistoryFranchiseRes;
import kr.co.olivepay.payment.dto.res.PaymentHistoryRes;
import kr.co.olivepay.payment.entity.Payment;
import kr.co.olivepay.payment.entity.PaymentDetail;
import kr.co.olivepay.payment.entity.enums.PaymentType;
import kr.co.olivepay.payment.global.enums.ErrorCode;
import kr.co.olivepay.payment.global.enums.NoneResponse;
import kr.co.olivepay.payment.global.enums.SuccessCode;
import kr.co.olivepay.payment.global.handler.AppException;
import kr.co.olivepay.payment.global.response.Response;
import kr.co.olivepay.payment.global.response.SuccessResponse;
import kr.co.olivepay.payment.mapper.PaymentMapper;
import kr.co.olivepay.payment.repository.PaymentRepository;
import kr.co.olivepay.payment.service.PaymentDetailService;
import kr.co.olivepay.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

	private static final int PAGE_SIZE = 20;

	private final PaymentRepository paymentRepository;
	private final PaymentMapper paymentMapper;

	private final PaymentDetailService paymentDetailService;
	private final CardServiceClient cardServiceClient;
	private final FundingServiceClient fundingServiceClient;
	private final FranchiseServiceClient franchiseServiceClient;
	private final MemberServiceClient memberServiceClient;

	@Override
	@Transactional
	public SuccessResponse<NoneResponse> pay(Long memberId, PaymentCreateReq request) {
		String userKey = validatePaymentPin(request.pin(), memberId);
		List<PaymentCardSearchRes> cardList = getPaymentCards(memberId, request);
		Payment payment = createPayment(memberId, request);
		List<PaymentDetail> paymentDetails = paymentDetailService.createPaymentDetails(payment, request, cardList);
		paymentDetailService.processCardPayments(userKey, paymentDetails, cardList, payment.getFranchiseId());
		processRemainingCouponAmount(request, paymentDetails);
		return new SuccessResponse<>(SuccessCode.PAYMENT_REGISTER_SUCCESS, NoneResponse.NONE);
	}

	private void processRemainingCouponAmount(PaymentCreateReq request, List<PaymentDetail> paymentDetails) {
		if (request.couponUserId() != null) {
			Long inputCouponAmount = request.couponUnit();
			Long usedCouponAmount = getCouponUsedAmount(paymentDetails);
			Long remainingAmount = inputCouponAmount - usedCouponAmount;

			if (remainingAmount > 0) {
				createFundingForRemainingAmount(request.couponUserId(), remainingAmount);
			}
		}
	}

	private Long getCouponUsedAmount(List<PaymentDetail> paymentDetails) {
		return paymentDetails.stream()
							 .filter(detail -> PaymentType.COUPON.equals(detail.getPaymentType()))
							 .map(PaymentDetail::getAmount)
							 .findFirst()
							 .orElse(0L);
	}

	private void createFundingForRemainingAmount(Long couponUserId, Long remainingAmount) {
		try {
			FundingCreateReq fundingCreateReq = FundingCreateReq.builder()
																.couponUserId(couponUserId)
																.amount(remainingAmount)
																.build();
			fundingServiceClient.createFunding(fundingCreateReq);
		} catch (Exception e) {
			throw e;
		}
	}

	private String validatePaymentPin(String pin, Long memberId) {
		try {
			UserPinCheckReq userPinCheckReq = UserPinCheckReq.builder()
															 .pin(pin)
															 .build();
			UserKeyRes userKeyRes = memberServiceClient.checkUserPin(memberId, userPinCheckReq)
													   .data();
			return userKeyRes.userKey();
		} catch (Exception e) {
			throw e;
		}
	}

	private List<PaymentCardSearchRes> getPaymentCards(Long memberId, PaymentCreateReq request) {
		CardSearchReq cardSearchReq = CardSearchReq.builder()
												   .cardId(request.cardId())
												   .isPublic(request.couponUserId() != null)
												   .build();
		List<PaymentCardSearchRes> cards = cardServiceClient.getPaymentCardList(memberId, cardSearchReq)
															.getBody()
															.data();
		return sortCardsByPriority(cards);
	}

	private List<PaymentCardSearchRes> sortCardsByPriority(List<PaymentCardSearchRes> cardList) {
		Map<CardType, Integer> priorityMap = Map.of(CardType.DREAMTREE, 1, CardType.COUPON, 2, CardType.DIFFERENCE, 3);

		return cardList.stream()
					   .sorted(Comparator.comparingInt(
						   card -> priorityMap.getOrDefault(card.cardType(), Integer.MAX_VALUE)))
					   .collect(Collectors.toList());
	}

	private Payment createPayment(Long memberId, PaymentCreateReq request) {
		Payment payment = paymentMapper.toEntity(memberId, request);
		paymentRepository.save(payment);
		return payment;
	}

	@Override
	public SuccessResponse<PageResponse<List<PaymentHistoryFranchiseRes>>> getUserPaymentHistory(Long memberId,
		Long lastPaymentId) {
		List<Payment> payments = fetchUserPayments(memberId, lastPaymentId);
		Map<Long, String> franchiseMap = getFranchiseMap(payments);
		List<PaymentHistoryFranchiseRes> historyResList = mapToPaymentHistoryFranchiseRes(payments, franchiseMap);

		Long nextCursor = payments.size() > PAGE_SIZE ? payments.get(PAGE_SIZE - 1)
																.getId() : lastPaymentId;
		PageResponse<List<PaymentHistoryFranchiseRes>> response = new PageResponse<>(nextCursor, historyResList);
		return new SuccessResponse<>(SuccessCode.USER_PAYMENT_HISTORY_SUCCESS, response);
	}

	@Override
	public SuccessResponse<PageResponse<List<PaymentHistoryRes>>> getFranchisePaymentHistory(Long memberId,
		Long franchiseId, Long lastPaymentId) {
		validateOwnership(memberId, franchiseId);
		List<Payment> payments = fetchFranchisePayments(franchiseId, lastPaymentId);
		List<PaymentHistoryRes> historyResList = mapToPaymentHistoryRes(payments);

		Long nextCursor = payments.size() > PAGE_SIZE ? payments.get(PAGE_SIZE - 1)
																.getId() : lastPaymentId;
		PageResponse<List<PaymentHistoryRes>> response = new PageResponse<>(nextCursor, historyResList);
		return new SuccessResponse<>(SuccessCode.FRANCHISE_PAYMENT_HISTORY_SUCCESS, response);

	}

	private List<Payment> fetchUserPayments(Long memberId, Long lastPaymentId) {
		if (lastPaymentId == null) {
			return paymentRepository.findByMemberIdOrderByIdDesc(memberId, PageRequest.of(0, PAGE_SIZE + 1));
		} else {
			return paymentRepository.findByMemberIdAndIdLessThanOrderByIdDesc(memberId, lastPaymentId,
				PageRequest.of(0, PAGE_SIZE + 1));
		}
	}

	private List<Payment> fetchFranchisePayments(Long franchiseId, Long lastPaymentId) {
		if (lastPaymentId == null) {
			return paymentRepository.findByFranchiseIdOrderByIdDesc(franchiseId, PageRequest.of(0, PAGE_SIZE + 1));
		} else {
			return paymentRepository.findByFranchiseIdAndIdLessThanOrderByIdDesc(franchiseId, lastPaymentId,
				PageRequest.of(0, PAGE_SIZE + 1));
		}
	}

	private Map<Long, String> getFranchiseMap(List<Payment> payments) {
		try {
			List<Long> franchiseIds = payments.stream()
											  .map(Payment::getFranchiseId)
											  .distinct()
											  .collect(Collectors.toList());
			FranchiseIdListReq request = FranchiseIdListReq.builder()
														   .franchiseIdList(franchiseIds)
														   .build();
			List<FranchiseMyDonationRes> franchiseList = franchiseServiceClient.getFranchiseListByFranchiseIdList(
																				   request)
																			   .data();
			return franchiseList.stream()
								.collect(
									Collectors.toMap(FranchiseMyDonationRes::franchiseId, FranchiseMyDonationRes::name,
										(existing, replacement) -> replacement));
		} catch (Exception e) {
			throw new AppException(ErrorCode.FRANCHISE_FEIGN_CLIENT_ERROR);
		}
	}

	private List<PaymentHistoryFranchiseRes> mapToPaymentHistoryFranchiseRes(List<Payment> payments,
		Map<Long, String> franchiseMap) {
		return payments.stream()
					   .map(payment -> paymentMapper.toPaymentHistoryFranchiseRes(payment,
						   franchiseMap.get(payment.getFranchiseId()),
						   paymentDetailService.getPaymentDetails(payment.getId())))
					   .collect(Collectors.toList());
	}

	private List<PaymentHistoryRes> mapToPaymentHistoryRes(List<Payment> payments) {
		return payments.stream()
					   .map(payment -> paymentMapper.toPaymentHistoryRes(payment,
						   paymentDetailService.getPaymentDetails(payment.getId())))
					   .collect(Collectors.toList());
	}

	private void validateOwnership(Long memberId, Long franchiseId) {
		try {
			Response<FranchiseMinimalRes> response = franchiseServiceClient.getFranchiseByMemberId(memberId);
			FranchiseMinimalRes franchiseData = response.data();
			if (franchiseData == null || !Objects.equals(franchiseData.id(), franchiseId)) {
				throw new AppException(ErrorCode.OWNERSHIP_REQUIRED);
			}
		} catch (Exception e) {
			throw new AppException(ErrorCode.FRANCHISE_FEIGN_CLIENT_ERROR);
		}
	}
}