package schwarz.jobs.interview.coupon.core.services

import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import schwarz.jobs.interview.coupon.core.domain.Coupon
import schwarz.jobs.interview.coupon.core.exception.CouponException
import schwarz.jobs.interview.coupon.core.repository.CouponRepository
import schwarz.jobs.interview.coupon.core.services.model.Basket
import schwarz.jobs.interview.coupon.web.dto.CouponDto
import schwarz.jobs.interview.coupon.web.dto.CouponRequestDto
import java.math.BigDecimal
import java.util.Optional

@Service
class CouponService(
    private val couponRepository: CouponRepository
) {

    private val logger = LoggerFactory.getLogger(CouponService::class.java)

    fun getCoupon(code: String) : Optional<Coupon> = couponRepository.findByCode(code)

    //FIXED: every failure now raises a CouponException the web layer maps to an explanatory
    // status
    fun apply(basket: Basket, code: String): Basket {

        //FIXED: Since getCoupon returns optional we need to validate if the return has a coupon present
        val coupon = getCoupon(code).orElseThrow { CouponException.CouponNotFound(code) }

        if (basket.applicationSuccessful) {
            throw CouponException.BasketAlreadyDiscounted(basket.appliedDiscount)
        }

         if (basket.value < BigDecimal.ZERO) {
            logger.warn("Tried to apply coupon {} to a basket with negative value {}", code, basket.value)
            throw CouponException.NegativeBasketValue(basket.value)
        }

        // compareTo rather than ==, because BigDecimal equality is scale sensitive: 0.00 != 0
        if (basket.value.compareTo(BigDecimal.ZERO) == 0) {
            return basket
        }

        //FIXED: functional error not taking into account minBasketValue
        if (basket.value < coupon.minBasketValue) {
            throw CouponException.BasketBelowMinimumValue(basket.value, coupon.minBasketValue)
        }

        basket.applyDiscount(coupon.discount)
        return basket
    }

    fun createCoupon(couponDto: CouponDto): Coupon =
        try {
            //FIXED: Coupon was built but never persisted
            couponRepository.save(
                Coupon(
                    code = couponDto.code,
                    discount = couponDto.discount,
                    minBasketValue = couponDto.minBasketValue,
                )
            )
        } catch (exception: DataIntegrityViolationException) {
            logger.info("Rejected creation of coupon {}: code already exists", couponDto.code)
            throw CouponException.CouponAlreadyExists(couponDto.code)
        }

    //FIXED: change MutableList to List
    fun getCoupons(couponRequestDto: CouponRequestDto): List<Coupon> {

        val foundCoupons = mutableListOf<Coupon>()
        //FIXED: Skip not found codes instead of raising error
        couponRequestDto.codes.forEach { couponRepository.findByCode(it).ifPresent { coupon -> foundCoupons.add(coupon) } }
        return foundCoupons
    }

}