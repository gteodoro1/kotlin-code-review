package schwarz.jobs.interview.coupon.web

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post



@SpringBootTest(
    properties = ["spring.datasource.url=jdbc:h2:mem:coupon-integration;DB_CLOSE_DELAY=-1"]
)
@AutoConfigureMockMvc
class CouponResourceIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    private fun basketBody(value: String, discount: String = "0", applied: Boolean = false) = """
        {
          "value": $value,
          "appliedDiscount": $discount,
          "applicationSuccessful": $applied
        }
    """.trimIndent()

    @Test
    fun `Should apply a seeded coupon to a qualifying basket`() {

        mockMvc.post("/api/coupons/TEST1/apply") {
            contentType = MediaType.APPLICATION_JSON
            content = basketBody("100")
        }.andExpect {
            status { isOk() }
            jsonPath("$.appliedDiscount") { value(10.00) }
            jsonPath("$.applicationSuccessful") { value(true) }
        }
    }

    @Test
    fun `Should return 404 when the coupon does not exist`() {

        mockMvc.post("/api/coupons/DOES_NOT_EXIST/apply") {
            contentType = MediaType.APPLICATION_JSON
            content = basketBody("100")
        }.andExpect {
            status { isNotFound() }
            content { contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON) }
            jsonPath("$.errorCode") { value("COUPON_NOT_FOUND") }
            jsonPath("$.detail") { value("No coupon exists with code 'DOES_NOT_EXIST'.") }
        }
    }

    @Test
    fun `Should return 422 when the basket is below the coupon minimum`() {

        mockMvc.post("/api/coupons/TEST1/apply") {
            contentType = MediaType.APPLICATION_JSON
            content = basketBody("10")
        }.andExpect {
            status { isUnprocessableEntity() }
            jsonPath("$.errorCode") { value("BASKET_BELOW_MINIMUM_VALUE") }
        }
    }

    @Test
    fun `Should return 400 when the basket value is negative`() {

        mockMvc.post("/api/coupons/TEST1/apply") {
            contentType = MediaType.APPLICATION_JSON
            content = basketBody("-100")
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.errorCode") { value("NEGATIVE_BASKET_VALUE") }
        }
    }

    @Test
    fun `Should return 409 when the basket was already discounted`() {

        mockMvc.post("/api/coupons/TEST1/apply") {
            contentType = MediaType.APPLICATION_JSON
            content = basketBody("100", discount = "5", applied = true)
        }.andExpect {
            status { isConflict() }
            jsonPath("$.errorCode") { value("BASKET_ALREADY_DISCOUNTED") }
        }
    }

    @Test
    fun `Should create a coupon and expose it without the entity id`() {

        mockMvc.post("/api/coupons") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"code":"INTEGRATION1","discount":7,"minBasketValue":20}"""
        }.andExpect {
            status { isOk() }
        }

        mockMvc.get("/api/coupons") {
            param("codes", "INTEGRATION1")
        }.andExpect {
            status { isOk() }
            jsonPath("$[0].code") { value("INTEGRATION1") }
            jsonPath("$[0].discount") { value(7.00) }
            jsonPath("$[0].minBasketValue") { value(20.00) }
            jsonPath("$[0].id") { doesNotExist() }
        }
    }

    @Test
    fun `Should return 409 when creating a coupon whose code already exists`() {

        mockMvc.post("/api/coupons") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"code":"TEST1","discount":5,"minBasketValue":10}"""
        }.andExpect {
            status { isConflict() }
            jsonPath("$.errorCode") { value("COUPON_ALREADY_EXISTS") }
            jsonPath("$.detail") { value("A coupon with code 'TEST1' already exists.") }
        }
    }

    @Test
    fun `Should skip codes that do not exist when listing coupons`() {

        mockMvc.get("/api/coupons") {
            param("codes", "TEST1,DOES_NOT_EXIST")
        }.andExpect {
            status { isOk() }
            jsonPath("$.length()") { value(1) }
            jsonPath("$[0].code") { value("TEST1") }
        }
    }

}
