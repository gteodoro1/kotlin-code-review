package schwarz.jobs.interview.coupon

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

//FIXED: uncomment @SpringBootApplication and remove redundant tags
@SpringBootApplication
class CouponApplication

fun main(args: Array<String>) {
	runApplication<CouponApplication>(*args)
}
