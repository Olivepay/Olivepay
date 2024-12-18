package kr.co.olivepay.payment.dto.req;

import jakarta.validation.constraints.*;
import kr.co.olivepay.payment.validator.PositiveOrNull;
import kr.co.olivepay.payment.validator.ValidCouponUnit;
import lombok.Builder;

@Builder
public record PaymentCreateReq(
	@NotNull(message = "가맹점 ID는 필수 입력값입니다.")
	@Positive(message = "가맹점 ID는 양수여야 합니다.")
	Long franchiseId,

	@NotNull(message = "결제 금액은 필수 입력값입니다.")
	@Positive(message = "결제 금액은 양수여야 합니다.")
	Long amount,

	@NotNull(message = "간편 결제 비밀번호는 필수 입력값입니다.")
	@Pattern(regexp = "^\\d{6}$", message = "간편 결제 비밀번호는 6자리 숫자여야 합니다.")
	String pin,

	@PositiveOrNull(message = "카드 ID는 null이거나 양수여야 합니다.")
	Long cardId,

	@PositiveOrNull(message = "쿠폰-유저 ID는 null이거나 양수여야 합니다.")
	Long couponUserId,

	@ValidCouponUnit(message = "쿠폰 유닛은 null이거나 2000, 4000이어야 합니다.")
	Long couponUnit
) {

}