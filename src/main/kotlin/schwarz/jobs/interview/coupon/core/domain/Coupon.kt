package schwarz.jobs.interview.coupon.core.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name = "coupon")
@SequenceGenerator(
    name = "CouponSequenceGenerator",
    sequenceName = "coupon_seq",
    allocationSize = 1000,
)
class Coupon(

    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "CouponSequenceGenerator"
    )
    var id: Long? = null,

    @Column(name = "code", unique = true, nullable = false)
    var code: String,

    @Column(name = "discount", precision = 10, scale = 2, nullable = false)
    var discount: BigDecimal,

    @Column(name = "minBasketValue", precision = 10, scale = 2, nullable = false)
    var minBasketValue: BigDecimal,
) {

    override fun equals(other: Any?): Boolean = other is Coupon && code == other.code

    override fun hashCode(): Int = code.hashCode()

}
