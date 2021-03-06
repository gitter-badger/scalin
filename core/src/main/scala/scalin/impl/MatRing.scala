package scalin
package impl

import spire.algebra._
import spire.syntax.cfor._
import spire.syntax.ring._

trait MatRing[A, MA <: Mat[A]]
    extends scalin.algebra.MatRing[A, MA]
    with scalin.impl.MatMultiplicativeMonoid[A, MA] {

  //// Creation

  def zeros(rows: Int, cols: Int): MA =
    fillConstant(rows, cols)(scalar.zero)

  def eye(n: Int): MA =
    tabulate(n, n)( (r, c) => if (r == c) scalar.one else scalar.zero )

  //// Additive group methods

  def plus(lhs: Mat[A], rhs: Mat[A]): MA = pointwiseBinary(lhs, rhs)(_ + _)

  def negate(lhs: Mat[A]): MA = pointwiseUnary(lhs)(-_)

  def minus(lhs: Mat[A], rhs: Mat[A]): MA = pointwiseBinary(lhs, rhs)(_ - _)

  def pointwisePlus(lhs: Mat[A], rhs: A): MA = pointwiseUnary(lhs)(_ + rhs)

  def pointwiseMinus(lhs: Mat[A], rhs: A): MA = pointwiseUnary(lhs)(_ - rhs)

  def trace(lhs: Mat[A]): A = {
    val n = lhs.nRows
    require(n == lhs.nCols)
    if (n == 0) scalar.zero else {
      var sum = lhs(0, 0)
      cforRange(1 until n) { k =>
        sum += lhs(k, k)
      }
      sum
    }
  }

  def sum(lhs: Mat[A]): A = fold(lhs)(scalar.zero)(scalar.plus)

  //// Ring methods

  def times(lhs: Mat[A], rhs: Mat[A]): MA = {
    import spire.syntax.cfor._
    val n = lhs.nCols
    require(n == rhs.nRows)
    if (n == 0)
      zeros(lhs.nRows, rhs.nCols)
    else
      tabulate(lhs.nRows, rhs.nCols) { (r, c) =>
        var sum = lhs(r, 0) * rhs(0, c)
        cforRange(1 until lhs.nCols) { k =>
          sum += lhs(r, k) * rhs(k, c)
        }
        sum
      }
  }

  def frobenius(lhs: Mat[A], rhs: Mat[A]): A = {
    val nr = lhs.nRows
    require(nr == rhs.nRows)
    val nc = lhs.nCols
    require(nc == rhs.nCols)
    import spire.syntax.cfor._
    var sum = scalar.zero
    cforRange(0 until nr) { r =>
      cforRange(0 until nc) { c =>
        sum += lhs(r, c) * rhs(r, c)
      }
    }
    sum
  }

  def nnz(lhs: Mat[A])(implicit ev: Eq[A]): Int = count(lhs)(scalar.isZero(_))

}
