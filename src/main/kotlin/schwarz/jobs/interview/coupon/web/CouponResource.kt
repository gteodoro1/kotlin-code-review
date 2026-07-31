package schwarz.jobs.interview.coupon.web

import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import schwarz.jobs.interview.coupon.core.services.CouponService
import schwarz.jobs.interview.coupon.core.services.model.Basket
import schwarz.jobs.interview.coupon.web.dto.CouponDto
import schwarz.jobs.interview.coupon.web.dto.CouponResponseDto

@RestController
//FIXED: improved the path
@RequestMapping("/api/coupons")
class CouponResource(
    private val couponService: CouponService,
) {

    @PostMapping("/{code}/apply")
    fun apply(
        @PathVariable code: String,
        @RequestBody @Valid basket: Basket,
    ): ResponseEntity<Basket> {

        //FIXED: move validation logic into the service
        return ResponseEntity.ok(couponService.apply(basket, code))
    }

    @PostMapping
    fun create(
        @RequestBody @Valid couponDto: CouponDto
    ): ResponseEntity<Void> {

        couponService.createCoupon(couponDto)

        return ResponseEntity.ok().build()
    }

    @GetMapping
    //FIXED: remove useles wrap plus follow no body on get convention
    //FIXED: map to a response DTO instead of the JPA entity
    fun getCoupons(@RequestParam codes: List<String>): List<CouponResponseDto> =
        couponService.getCoupons(codes).map(CouponResponseDto::from)
}