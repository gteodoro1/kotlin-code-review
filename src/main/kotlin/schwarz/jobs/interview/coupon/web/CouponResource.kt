package schwarz.jobs.interview.coupon.web

import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import schwarz.jobs.interview.coupon.core.services.CouponService
import schwarz.jobs.interview.coupon.core.services.model.Basket
import schwarz.jobs.interview.coupon.web.dto.ApplicationRequestDto
import schwarz.jobs.interview.coupon.web.dto.CouponDto
import schwarz.jobs.interview.coupon.web.dto.CouponRequestDto
import schwarz.jobs.interview.coupon.web.dto.CouponResponseDto

@RestController
@RequestMapping("/api")
class CouponResource(
    private val couponService: CouponService,
) {

    /**
     * @param applicationRequestDto
     * @return
     *
     */
    // @Operation(summary = "Applies currently active promotions and coupons from the request to the requested Basket - Version 1")
    @PostMapping("/apply")
    fun apply(
        @RequestBody @Valid applicationRequestDto: ApplicationRequestDto
    ): ResponseEntity<Basket> {

        //FIXED: move validation logic into the service
        return ResponseEntity.ok(couponService.apply(applicationRequestDto.basket, applicationRequestDto.code))
    }

    @PostMapping("/create")
    fun create(
        @RequestBody @Valid couponDto: CouponDto
    ): ResponseEntity<Void> {

        couponService.createCoupon(couponDto)

        return ResponseEntity.ok().build()
    }

    @GetMapping("/coupons")
    //FIXED: remove useles wrap plus follow no body on get convention
    //FIXED: map to a response DTO instead of the JPA entity
    fun getCoupons(@RequestParam codes: List<String>): List<CouponResponseDto> =
        couponService.getCoupons(CouponRequestDto(codes = codes)).map(CouponResponseDto::from)
}