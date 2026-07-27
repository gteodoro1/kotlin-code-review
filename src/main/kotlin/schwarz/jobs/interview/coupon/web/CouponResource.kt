package schwarz.jobs.interview.coupon.web

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import schwarz.jobs.interview.coupon.core.domain.Coupon
import schwarz.jobs.interview.coupon.core.services.CouponService
import schwarz.jobs.interview.coupon.core.services.model.Basket
import schwarz.jobs.interview.coupon.web.dto.ApplicationRequestDto
import schwarz.jobs.interview.coupon.web.dto.CouponDto
import schwarz.jobs.interview.coupon.web.dto.CouponRequestDto

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

        if (applicationRequestDto.basket.applicationSuccessful) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build()
        }

        val basket = couponService.apply(applicationRequestDto.basket, applicationRequestDto.code)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok().body(basket)
    }

    @PostMapping("/create")
    fun create(
        @RequestBody @Valid couponDto: CouponDto
    ): ResponseEntity<Void> {

        val coupon = couponService.createCoupon(couponDto)

        return ResponseEntity.ok().build()
    }

    @GetMapping("/coupons")
    fun getCoupons(@RequestBody @Valid couponRequestDto: CouponRequestDto): MutableList<Coupon> {

        return couponService.getCoupons(couponRequestDto)
    }
}