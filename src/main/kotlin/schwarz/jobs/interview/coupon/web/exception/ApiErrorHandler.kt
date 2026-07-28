package schwarz.jobs.interview.coupon.web.exception

import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import schwarz.jobs.interview.coupon.core.exception.CouponException


@RestControllerAdvice
class ApiErrorHandler {

    private val logger = LoggerFactory.getLogger(ApiErrorHandler::class.java)

    @ExceptionHandler(CouponException::class)
    fun handleCouponException(exception: CouponException): ProblemDetail {


        val (status, errorCode) = when (exception) {
            is CouponException.CouponNotFound ->
                HttpStatus.NOT_FOUND to "COUPON_NOT_FOUND"

            is CouponException.CouponAlreadyExists ->
                HttpStatus.CONFLICT to "COUPON_ALREADY_EXISTS"

            is CouponException.BasketBelowMinimumValue ->
                HttpStatus.UNPROCESSABLE_ENTITY to "BASKET_BELOW_MINIMUM_VALUE"

            is CouponException.NegativeBasketValue ->
                HttpStatus.BAD_REQUEST to "NEGATIVE_BASKET_VALUE"

            is CouponException.BasketAlreadyDiscounted ->
                HttpStatus.CONFLICT to "BASKET_ALREADY_DISCOUNTED"
        }

        return problemDetail(status, errorCode, exception.message)
    }


    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrityViolation(exception: DataIntegrityViolationException): ProblemDetail {

        logger.warn("Request violated a database constraint", exception)

        return problemDetail(
            status = HttpStatus.CONFLICT,
            errorCode = "CONSTRAINT_VIOLATION",
            detail = "The request conflicts with the current state of the resource.",
        )
    }

    private fun problemDetail(status: HttpStatus, errorCode: String, detail: String): ProblemDetail =
        ProblemDetail.forStatusAndDetail(status, detail).apply {
            title = status.reasonPhrase
            setProperty("errorCode", errorCode)
        }
}
