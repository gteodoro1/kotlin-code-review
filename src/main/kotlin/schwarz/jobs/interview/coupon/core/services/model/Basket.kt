package schwarz.jobs.interview.coupon.core.services.model

import java.math.BigDecimal

data class Basket(

    var value: BigDecimal,
    var appliedDiscount: BigDecimal,
    var applicationSuccessful: Boolean,
) {
    fun applyDiscount(discount: BigDecimal) {
        //Fixed: Boolean was inverted
        this.applicationSuccessful = true
        this.appliedDiscount = discount
    }
}
