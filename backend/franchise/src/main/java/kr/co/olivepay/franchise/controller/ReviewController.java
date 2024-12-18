package kr.co.olivepay.franchise.controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import kr.co.olivepay.core.global.dto.res.PageResponse;
import kr.co.olivepay.core.util.CommonUtil;
import kr.co.olivepay.franchise.dto.req.ReviewCreateReq;
import kr.co.olivepay.franchise.dto.res.EmptyReviewRes;
import kr.co.olivepay.franchise.dto.res.FranchiseReviewRes;
import kr.co.olivepay.franchise.dto.res.UserReviewRes;
import kr.co.olivepay.franchise.global.enums.NoneResponse;
import kr.co.olivepay.franchise.global.response.Response;
import kr.co.olivepay.franchise.global.response.SuccessResponse;
import kr.co.olivepay.franchise.service.ReviewService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/franchises/reviews")
@RequiredArgsConstructor
public class ReviewController {

	private final ReviewService reviewService;

	@PostMapping("/user")
	@Operation(description = """
		리뷰를 등록합니다. \n
		작성자 id, 가맹점 id, 내용, 평점을 필요로 합니다.
		""", summary = "리뷰 등록")
	public ResponseEntity<Response<NoneResponse>> registerReview(
		@RequestHeader HttpHeaders headers,
		@RequestBody ReviewCreateReq request) {
		Long memberId = CommonUtil.getMemberId(headers);
		SuccessResponse<NoneResponse> response = reviewService.registerReview(memberId, request);
		return Response.success(response);
	}

	@DeleteMapping("/user/{reviewId}")
	@Operation(description = """
		리뷰 id에 해당하는 리뷰를 삭제합니다.
		""", summary = "리뷰 삭제")
	public ResponseEntity<Response<NoneResponse>> deleteReview(
		@RequestHeader HttpHeaders headers,
		@PathVariable Long reviewId
	) {
		Long memberId = CommonUtil.getMemberId(headers);
		SuccessResponse<NoneResponse> response = reviewService.removeReview(reviewId);
		return Response.success(response);
	}

	@GetMapping("/user")
	@Operation(description = """
		내가 작성한 모든 리뷰를 조회합니다. \n
		20개 단위로 페이징 처리가 이뤄집니다.
		""", summary = "내가 작성한 리뷰 조회")
	public ResponseEntity<Response<PageResponse<List<FranchiseReviewRes>>>> getMyReviewList(
		@RequestHeader HttpHeaders headers,
		@RequestParam(required = false) Long index
	) {
		Long memberId = CommonUtil.getMemberId(headers);
		SuccessResponse<PageResponse<List<FranchiseReviewRes>>> response = reviewService.getMyReviewList(memberId, index);
		return Response.success(response);
	}

	@GetMapping("/{franchiseId}")
	@Operation(description = """
		특정 가맹점에 대한 모든 리뷰를 조회합니다. \n
		20개 단위로 페이징 처리가 이뤄집니다.
		""", summary = "가맹점 리뷰 조회")
	public ResponseEntity<Response<PageResponse<List<UserReviewRes>>>> getFranchiseReviewList(
		@PathVariable Long franchiseId,
		@RequestParam(required = false) Long index
	) {
		SuccessResponse<PageResponse<List<UserReviewRes>>> response = reviewService.getFranchiseReviewList(franchiseId, index);
		return Response.success(response);
	}

	@GetMapping("/available")
	@Operation(description = """
		사용자가 결제 내용은 있지만 작성하지 않은 모든 리뷰를 조회합니다.
		""", summary = "미작성 리뷰 조회")
	public ResponseEntity<Response<List<EmptyReviewRes>>> getAvailableReviewList(
		@RequestHeader HttpHeaders headers
	) {
		Long memberId = CommonUtil.getMemberId(headers);
		SuccessResponse<List<EmptyReviewRes>> response = reviewService.getAvailableReviewList(memberId);
		return Response.success(response);
	}

}
