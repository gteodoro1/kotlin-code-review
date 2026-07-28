package schwarz.jobs.interview.coupon.web.dto

import schwarz.jobs.interview.coupon.core.domain.Coupon
import java.math.BigDecimal

/**
 * What this API exposes about a coupon.
 *
 * Deliberately not the Coupon entity: the persistence model is free to change - and to grow
 * columns clients have no business seeing - without altering this contract. Notably the
 * surrogate id stays internal; `code` is the identifier clients already use.
 */
data class CouponResponseDto(

    val code: String,
    val discount: BigDecimal,
    val minBasketValue: BigDecimal,
) {
    companion object {

        fun from(coupon: Coupon) = CouponResponseDto(
            code = coupon.code,
            discount = coupon.discount,
            minBasketValue = coupon.minBasketValue,
        )
    }
}
