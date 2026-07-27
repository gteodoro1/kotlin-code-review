package schwarz.jobs.interview.coupon.core.services

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import schwarz.jobs.interview.coupon.BaseObject.baseCoupon
import schwarz.jobs.interview.coupon.core.domain.Coupon
import schwarz.jobs.interview.coupon.core.repository.CouponRepository
import schwarz.jobs.interview.coupon.core.services.model.Basket
import schwarz.jobs.interview.coupon.web.dto.CouponDto
import schwarz.jobs.interview.coupon.web.dto.CouponRequestDto
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

    @Test
    fun `Should apply a coupon and mark the basket application successful`() {

        every {
            couponRepository.findByCode("coupon1")
        } returns Optional.of(baseCoupon)

        val basket = Basket(
            value = BigDecimal("100.0"),
            appliedDiscount = BigDecimal.ZERO,
            applicationSuccessful = false,
        )

        val result = couponService.apply(basket, "coupon1")!!

        Assertions.assertEquals(baseCoupon.discount, result.appliedDiscount)
        Assertions.assertEquals(true, result.applicationSuccessful)
    }

    @Test
    fun `Should persist a new coupon`() {

        val couponDto = CouponDto(
            code = "coupon1",
            discount = BigDecimal("1.0"),
            minBasketValue = BigDecimal("50.0"),
        )

        val expectedCoupon = Coupon(
            code = couponDto.code,
            discount = couponDto.discount,
            minBasketValue = couponDto.minBasketValue,
        )

        every {
            couponRepository.save(expectedCoupon)
        } returns expectedCoupon

        couponService.createCoupon(couponDto)

        verify { couponRepository.save(expectedCoupon) }
    }

    @Test
    fun `getCoupons should return existing coupons only`() {

        every {
            couponRepository.findByCode("coupon1")
        } returns Optional.of(baseCoupon)

        every {
            couponRepository.findByCode("unknown")
        } returns Optional.empty()

        val coupons = couponService.getCoupons(CouponRequestDto(codes = listOf("coupon1", "unknown")))

        Assertions.assertEquals(listOf(baseCoupon), coupons)
    }
}