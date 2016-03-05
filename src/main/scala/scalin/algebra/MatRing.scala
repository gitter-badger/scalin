package scalin
package algebra

import spire.algebra._

trait MatRing[A, M[A] <: Mat[A]] extends MatMultiplicativeMonoid[A, M] {

  implicit def scalar: Ring[A]

  import spire.syntax.ring._

  // builder methods

  def zeros(rows: Int, cols: Int): M[A] =
    factory.fill(rows, cols)(scalar.zero)

  def eye(n: Int): M[A] =
    factory.tabulate(n, n)( (r, c) => if (r == c) scalar.one else scalar.zero )

  // additive group methods

  def plus(lhs: Mat[A], rhs: Mat[A]): M[A] = pointwiseBinary(lhs, rhs)(_ + _)

  def negate(lhs: Mat[A]): M[A] = pointwiseUnary(lhs)(-_)

  def minus(lhs: Mat[A], rhs: Mat[A]): M[A] = pointwiseBinary(lhs, rhs)(_ - _)

  def pointwisePlus(lhs: Mat[A], rhs: A): M[A] = pointwiseUnary(lhs)(_ + rhs)

  def pointwiseMinus(lhs: Mat[A], rhs: A): M[A] = pointwiseUnary(lhs)(_ - rhs)

  // ring methods

  def times(lhs: Mat[A], rhs: Mat[A]): M[A] = {
    import spire.syntax.cfor._
    val n = lhs.cols
    require(n == rhs.rows)
    if (n == 0)
      zeros(lhs.rows, rhs.cols)
    else
      factory.tabulate(lhs.rows, rhs.cols) { (r, c) =>
        var sum = lhs(r, 0) * rhs(0, c)
        cforRange(1 until lhs.cols) { k =>
          sum += lhs(r, k) * rhs(k, c)
        }
        sum
      }
  }

}