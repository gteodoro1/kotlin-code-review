package schwarz.jobs.interview.coupon.core.services

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import schwarz.jobs.interview.coupon.core.domain.Coupon
import schwarz.jobs.interview.coupon.core.repository.CouponRepository
import schwarz.jobs.interview.coupon.core.services.model.Basket
import schwarz.jobs.interview.coupon.web.dto.CouponDto
import schwarz.jobs.interview.coupon.web.dto.CouponRequestDto
import java.util.Optional

@Service
class CouponService(
    private val couponRepository: CouponRepository
) {

    private val logger = LoggerFactory.getLogger(CouponService::class.java)

    fun getCoupon(code: String) : Optional<Coupon> = couponRepository.findByCode(code)

    fun apply(basket: Basket, code: String): Basket? {

        //FIXED: Since getCoupon returns optional we need to validate if the return has a coupon present
        val coupon = getCoupon(code).orElseThrow { NoSuchElementException("No coupon found for code: $code") }

        if (basket.value.toDouble() >= 0) {

            if (basket.value.toDouble() > 0) {
                //FIXED: functional error not taking into account minBasketValue
                if (basket.value < coupon.minBasketValue) {
                    return null
                }
                basket.applyDiscount(coupon.discount)
            } else if (basket.value.toDouble() == 0.0) {
                return basket
            }
        } else {
            //FIXED: Remove print ln and use Logger instead
            logger.warn("Tried to apply negative discount to basket with value {}", basket.value)
            throw RuntimeException("Can't apply negative discounts")
        }
        return basket
    }

    fun createCoupon(couponDto: CouponDto): Coupon {

        //FIXED: Coupon was built but never persisted
        return couponRepository.save(
            Coupon(
                code = couponDto.code,
                discount = couponDto.discount,
                minBasketValue = couponDto.minBasketValue,
            )
        )
    }

    fun getCoupons(couponRequestDto: CouponRequestDto): MutableList<Coupon> {

        val foundCoupons = mutableListOf<Coupon>()
        //FIXED: Skip not found codes instead of raising error
        couponRequestDto.codes.forEach { couponRepository.findByCode(it).ifPresent { coupon -> foundCoupons.add(coupon) } }
        return foundCoupons
    }

}