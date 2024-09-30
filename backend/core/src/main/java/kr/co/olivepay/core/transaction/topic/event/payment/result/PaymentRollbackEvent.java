package kr.co.olivepay.core.transaction.topic.event.payment.result;

import lombok.Builder;

import java.util.List;

/**
 *
 * @param paymentId
 * @param memberId
 * @param userKey
 * @param failReason
 * @param paymentRollbackDetailEventList
 */
@Builder
public record PaymentRollbackEvent(
        Long paymentId,
        Long memberId,
        String userKey,
        String failReason,
        //결제 취소를 수행해야하는 리스트
        List<PaymentRollbackDetailEvent> paymentRollbackDetailEventList
) {

}
