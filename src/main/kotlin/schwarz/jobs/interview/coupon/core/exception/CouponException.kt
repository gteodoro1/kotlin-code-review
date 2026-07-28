package schwarz.jobs.interview.coupon.core.exception

import java.math.BigDecimal

sealed class CouponException(override val message: String) : RuntimeException(message) {

    class CouponNotFound(val code: String) :
        CouponException("No coupon exists with code '$code'.")

    class CouponAlreadyExists(val code: String) :
        CouponException("A coupon with code '$code' already exists.")

    class BasketBelowMinimumValue(val basketValue: BigDecimal, val minBasketValue: BigDecimal) :
        CouponException(
            "Basket value $basketValue is below the minimum of $minBasketValue required by this coupon."
        )

    class NegativeBasketValue(val basketValue: BigDecimal) :
        CouponException("Basket value must not be negative, but was $basketValue.")

    class BasketAlreadyDiscounted(val appliedDiscount: BigDecimal) :
        CouponException("A discount of $appliedDiscount has already been applied to this basket.")
}
