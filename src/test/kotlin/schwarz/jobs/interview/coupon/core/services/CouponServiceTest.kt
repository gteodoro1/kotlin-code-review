package schwarz.jobs.interview.coupon.core.services

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import schwarz.jobs.interview.coupon.BaseObject.baseCoupon
import schwarz.jobs.interview.coupon.core.repository.CouponRepository
import schwarz.jobs.interview.coupon.core.services.model.Basket
import java.math.BigDecimal
import java.util.Optional

class CouponServiceTest {

    private val couponRepository = mockk<CouponRepository>()
    //FIXED: Use real service instance instead of mock
    private val couponService = CouponService(couponRepository)

    @Test
    fun `Should get a coupon`() {

        every {
            couponRepository.findByCode("coupon1")
        } returns Optional.of(baseCoupon)

        val coupon = couponService.getCoupon("coupon1")!!

        verify { couponRepository.findByCode(any()) }
        Assertions.assertEquals(coupon.get().code, "coupon1")
    }

    @Test
    fun `Should not apply coupon when basket value is below minBasketValue`() {

        every {
            couponRepository.findByCode("coupon1")
        } returns Optional.of(baseCoupon)

        val basket = Basket(
            value = BigDecimal("10.0"),
            appliedDiscount = BigDecimal.ZERO,
            applicationSuccessful = false,
        )

        val result = couponService.apply(basket, "coupon1")

        Assertions.assertNull(result)
    }
}