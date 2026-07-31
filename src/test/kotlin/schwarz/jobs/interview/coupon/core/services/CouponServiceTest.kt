package schwarz.jobs.interview.coupon.core.services

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.dao.DataIntegrityViolationException
import schwarz.jobs.interview.coupon.BaseObject.baseCoupon
import schwarz.jobs.interview.coupon.core.exception.CouponException
import schwarz.jobs.interview.coupon.core.repository.CouponRepository
import schwarz.jobs.interview.coupon.core.services.model.Basket
import schwarz.jobs.interview.coupon.web.dto.CouponDto
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

        val coupon = couponService.getCoupon("coupon1")

        verify { couponRepository.findByCode(any()) }
        Assertions.assertEquals("coupon1", coupon.get().code)
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

        val exception = Assertions.assertThrows(CouponException.BasketBelowMinimumValue::class.java) {
            couponService.apply(basket, "coupon1")
        }

        Assertions.assertEquals(BigDecimal("10.0"), exception.basketValue)
        Assertions.assertEquals(baseCoupon.minBasketValue, exception.minBasketValue)
    }

    @Test
    fun `Should reject applying an unknown coupon code`() {

        every {
            couponRepository.findByCode("unknown")
        } returns Optional.empty()

        val basket = Basket(
            value = BigDecimal("100.0"),
            appliedDiscount = BigDecimal.ZERO,
            applicationSuccessful = false,
        )

        val exception = Assertions.assertThrows(CouponException.CouponNotFound::class.java) {
            couponService.apply(basket, "unknown")
        }

        Assertions.assertEquals("unknown", exception.code)
    }

    @Test
    fun `Should reject applying a coupon to a basket with negative value`() {

        every {
            couponRepository.findByCode("coupon1")
        } returns Optional.of(baseCoupon)

        val basket = Basket(
            value = BigDecimal("-100.0"),
            appliedDiscount = BigDecimal.ZERO,
            applicationSuccessful = false,
        )

        Assertions.assertThrows(CouponException.NegativeBasketValue::class.java) {
            couponService.apply(basket, "coupon1")
        }
    }

    @Test
    fun `Should reject applying a coupon to an already discounted basket`() {

        every {
            couponRepository.findByCode("coupon1")
        } returns Optional.of(baseCoupon)

        val basket = Basket(
            value = BigDecimal("100.0"),
            appliedDiscount = BigDecimal("5.0"),
            applicationSuccessful = true,
        )

        Assertions.assertThrows(CouponException.BasketAlreadyDiscounted::class.java) {
            couponService.apply(basket, "coupon1")
        }
    }

    @Test
    fun `Should treat a scaled zero basket as zero`() {

        every {
            couponRepository.findByCode("coupon1")
        } returns Optional.of(baseCoupon)

        // BigDecimal("0.00") != BigDecimal.ZERO under equals, so this only passes
        // if the zero check compares with compareTo
        val basket = Basket(
            value = BigDecimal("0.00"),
            appliedDiscount = BigDecimal.ZERO,
            applicationSuccessful = false,
        )

        val result = couponService.apply(basket, "coupon1")

        Assertions.assertEquals(BigDecimal.ZERO, result.appliedDiscount)
        Assertions.assertEquals(false, result.applicationSuccessful)
    }

    @Test
    fun `Should not let a tiny positive basket value pass as zero`() {

        every {
            couponRepository.findByCode("coupon1")
        } returns Optional.of(baseCoupon)

        // 1E-400 rounds to 0.0 as a Double, which would wrongly skip the minBasketValue check
        val basket = Basket(
            value = BigDecimal("1E-400"),
            appliedDiscount = BigDecimal.ZERO,
            applicationSuccessful = false,
        )

        Assertions.assertThrows(CouponException.BasketBelowMinimumValue::class.java) {
            couponService.apply(basket, "coupon1")
        }
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

        val result = couponService.apply(basket, "coupon1")

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

        every { couponRepository.save(any()) } returns baseCoupon

        couponService.createCoupon(couponDto)

        verify {
            couponRepository.save(
                withArg {
                    Assertions.assertEquals(couponDto.code, it.code)
                    Assertions.assertEquals(couponDto.discount, it.discount)
                    Assertions.assertEquals(couponDto.minBasketValue, it.minBasketValue)
                }
            )
        }
    }

    @Test
    fun `Should reject creating a coupon whose code already exists`() {

        val couponDto = CouponDto(
            code = "coupon1",
            discount = BigDecimal("1.0"),
            minBasketValue = BigDecimal("50.0"),
        )

        every { couponRepository.save(any()) } throws
            DataIntegrityViolationException("unique constraint violation on coupon.code")

        val exception = Assertions.assertThrows(CouponException.CouponAlreadyExists::class.java) {
            couponService.createCoupon(couponDto)
        }

        Assertions.assertEquals("coupon1", exception.code)
    }

    @Test
    fun `getCoupons should return existing coupons only`() {

        every {
            couponRepository.findByCode("coupon1")
        } returns Optional.of(baseCoupon)

        every {
            couponRepository.findByCode("unknown")
        } returns Optional.empty()

        val coupons = couponService.getCoupons(listOf("coupon1", "unknown"))

        Assertions.assertEquals(listOf(baseCoupon), coupons)
    }
}