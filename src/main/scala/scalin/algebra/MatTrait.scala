package scalin
package algebra

import spire.algebra._
import spire.syntax.order._
import spire.syntax.cfor._

trait MatTrait[A, MA <: Mat[A]] {

  type Ret = MA

  // builders

  def tabulate(rows: Int, cols: Int)(f: (Int, Int) => A): MA // TODO = factory.tabulate[A](rows, cols)(f)

  def fill(rows: Int, cols: Int)(a: => A): MA = tabulate(rows, cols)( (i, j) => a )

  def colMajor(rows: Int, cols: Int)(elements: A*): MA = {
    require(elements.size == rows * cols)
    tabulate(rows, cols)( (r, c) => elements(r + c * rows) )
  }

  def rowMajor(rows: Int, cols: Int)(elements: A*): MA = {
    require(elements.size == rows * cols)
    tabulate(rows, cols)( (r, c) => elements(c + r * cols) )
  }

  def rowMat(elements: A*): MA = rowMajor(1, elements.size)(elements: _*)

  def colMat(elements: A*): MA = colMajor(elements.size, 1)(elements: _*)

  // monadic-like

  def map[B](lhs: Mat[B])(f: B => A): MA = tabulate(lhs.rows, lhs.cols)( (r, c) => f(lhs(r, c)) )


  def horzcat[L <: Mat[A], R <: Mat[A]](lhs: L, rhs: R): MA = {
    val m = lhs.rows
    require(m == rhs.rows)
    val nl = lhs.cols
    val nr = rhs.cols
    tabulate(m, nl + nr)( (r, c) => if (c < nl) lhs(r, c) else rhs(r, c - nl) )
  }

  def vertcat[L <: Mat[A], R <: Mat[A]](lhs: L, rhs: R): MA = {
    val n = lhs.cols
    require(n == rhs.cols)
    val ml = lhs.rows
    val mr = rhs.rows
    tabulate(ml + mr, n)( (r, c) => if (r < ml) lhs(r, c) else rhs(r - ml, c) )
  }

  def flatten[B <: Mat[A]](lhs: Mat[B]): MA =
    if (lhs.rows == 0 || lhs.cols == 0) sys.error("Cannot flatten matrix with 0 rows or zero cols.")
    else {
      def flatRow(r: Int): MA = {
        if (lhs.cols == 1) map(lhs(r, 0))(identity)
        else {
          var accRow = horzcat(lhs(r, 0), lhs(r, 1))
          cforRange(2 until lhs.cols) { c =>
            accRow = horzcat(accRow, lhs(r, c))
          }
          accRow
        }
      }
      if (lhs.rows == 1) flatRow(0)
      else {
        var acc = vertcat(flatRow(0), flatRow(1))
        cforRange(2 until lhs.rows) { r =>
          acc = vertcat(acc, flatRow(r))
        }
        acc
      }
    }

  def flatMap[B](lhs: Mat[B])(f: B => Mat[A]): MA =
    if (lhs.rows == 0 || lhs.cols == 0) sys.error("Cannot flatten matrix with 0 rows or zero cols.")
    else {
      def flatRow(r: Int): MA = {
        if (lhs.cols == 1) map(f(lhs(r, 0)))(identity)
        else {
          var accRow = horzcat(f(lhs(r, 0)), f(lhs(r, 1)))
          cforRange(2 until lhs.cols) { c =>
            accRow = horzcat(accRow, f(lhs(r, c)))
          }
          accRow
        }
      }
      if (lhs.rows == 1) flatRow(0)
      else {
        var acc = vertcat(flatRow(0), flatRow(1))
        cforRange(2 until lhs.rows) { r =>
          acc = vertcat(acc, flatRow(r))
        }
        acc
      }
    }

  // shufflers

  def t(mat: Mat[A]): MA = tabulate(mat.cols, mat.rows)( (i, j) => mat(j, i) )

  def reshape(vec: Vec[A], rows1: Int, cols1: Int): MA = {
    require(vec.length == rows1 * cols1)
    tabulate(rows1, cols1)( (r1, c1) => vec(r1 + c1 * rows1) )
  }

  // slicers

  def slice(mat: Mat[A], rs: Subscript, cs: Subscript): MA = {
    val ri = rs.forLength(mat.rows)
    val ci = cs.forLength(mat.cols)
    tabulate(ri.length, ci.length)( (k, l) => mat(ri(k), ci(l)) )
  }

  // pointwise functions

  def pointwiseUnary(lhs: Mat[A])(f: A => A) = tabulate(lhs.rows, lhs.cols)( (r, c) => f(lhs(r, c)) )

  def pointwiseBinary(lhs: Mat[A], rhs: Mat[A])(f: (A, A) => A): MA = {
    require(lhs.rows == rhs.rows)
    require(lhs.cols == rhs.cols)
    tabulate(lhs.rows, lhs.cols)( (r, c) => f(lhs(r, c), rhs(r, c)) )
  }

  def booleanBinaryAnd(lhs: Mat[A], rhs: Mat[A])(f: (A, A) => Boolean): Boolean =
    (lhs.rows == rhs.rows && lhs.cols == rhs.cols) && {
      cforRange(0 until lhs.rows) { r =>
        cforRange(0 until lhs.cols) { c =>
          if (!f(lhs(r, c), rhs(r, c))) return false
        }
      }
      true
    }

  def pointwiseBooleanUnary[B](lhs: Mat[B])(f: B => Boolean)(implicit ev: Boolean =:= A): MA =
    tabulate(lhs.rows, lhs.cols)( (r, c) =>  f(lhs(r, c)) )

  def pointwiseBooleanBinary[B](lhs: Mat[B], rhs: Mat[B])(f: (B, B) => Boolean)(implicit ev: Boolean =:= A): MA = {
    require(lhs.rows == rhs.rows && lhs.cols == rhs.cols)
    tabulate(lhs.rows, lhs.cols)( (r, c) =>  f(lhs(r, c), rhs(r, c)) )
  }

  def pointwiseEqual[B](lhs: Mat[B], rhs: B)(implicit ev: Boolean =:= A): MA =
    pointwiseBooleanUnary(lhs)(_ == rhs)

  def pointwiseEqual[B](lhs: Mat[B], rhs: Mat[B])(implicit ev: Boolean =:= A): MA =
    pointwiseBooleanBinary(lhs, rhs)(_ == _)

  def pointwiseNotEqual[B](lhs: Mat[B], rhs: B)(implicit ev: Boolean =:= A): MA =
    pointwiseBooleanUnary(lhs)(_ != rhs)

  def pointwiseNotEqual[B](lhs: Mat[B], rhs: Mat[B])(implicit ev: Boolean =:= A): MA =
    pointwiseBooleanBinary(lhs, rhs)(_ != _)

  def pointwiseEqv[B](lhs: Mat[B], rhs: B)(implicit B: Eq[B], ev: Boolean =:= A): MA =
    pointwiseBooleanUnary(lhs)(_ === rhs)

  def pointwiseEqv[B](lhs: Mat[B], rhs: Mat[B])(implicit B: Eq[B], ev: Boolean =:= A): MA =
    pointwiseBooleanBinary(lhs, rhs)(_ === _)

  def pointwiseNeqv[B](lhs: Mat[B], rhs: B)(implicit B: Eq[B], ev: Boolean =:= A): MA =
    pointwiseBooleanUnary(lhs)(_ =!= rhs)

  def pointwiseNeqv[B](lhs: Mat[B], rhs: Mat[B])(implicit B: Eq[B], ev: Boolean =:= A): MA =
    pointwiseBooleanBinary(lhs, rhs)(_ =!= _)

  // equality functions

    // TODO: remove, and keep only the `equals` default Java method ?
  def equal(lhs: Mat[A], rhs: Mat[A]): Boolean = booleanBinaryAnd(lhs, rhs)(_ == _)

  def eqv(lhs: Mat[A], rhs: Mat[A])(implicit eqv: Eq[A]): Boolean = booleanBinaryAnd(lhs, rhs)(_ === _)

  // def hashCode
}
