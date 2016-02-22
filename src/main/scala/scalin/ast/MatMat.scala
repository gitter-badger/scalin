package scalin
package ast

import spire.algebra._

object MatMat {

  case class Linear[A](lhs: AbstractMat[A], rhs: AbstractMat[A], op: (A, A) => A) extends AbstractMat[A] {

    require(lhs.rows == rhs.rows && lhs.cols == rhs.cols)

    def rows: Int = lhs.rows

    def cols: Int = lhs.cols

    def nextNonZeroInCol(r: Int, c: Int) =
      spire.math.min(lhs.nextNonZeroInCol(r, c), rhs.nextNonZeroInCol(r, c))

    def nextNonZeroInRow(r: Int, c: Int) =
      spire.math.min(lhs.nextNonZeroInRow(r, c), rhs.nextNonZeroInRow(r, c))

    def touch(node: AbstractNode) = lhs.touch(node).merge(rhs.touch(node))

    def apply(r: Int, c: Int) = op(lhs(r, c), rhs(r, c))

  }

  case class Times[A](lhs: AbstractMat[A], rhs: AbstractMat[A])(implicit A: Semiring[A]) extends AbstractMat[A] {

    require(lhs.cols == rhs.rows)

    def nextNonZeroInCol(r: Int, c: Int): Int = r + 1

    def nextNonZeroInRow(r: Int, c: Int): Int = c + 1

    def apply(r: Int, c: Int): A = {
      import spire.syntax.ring._
      
      var res = A.zero
      val n = lhs.cols
      var k = spire.math.min(
        lhs.nextNonZeroInRow(r, -1),
        rhs.nextNonZeroInCol(-1, c)
      )
      while (k < n) {
        res += lhs(r, k) * rhs(k, c)
        k = spire.math.min(
          lhs.nextNonZeroInRow(r, k),
          rhs.nextNonZeroInCol(k, c)
        )
      }
      res
    }

    def rows: Int = lhs.rows

    def cols: Int = rhs.cols

    def touch(node: AbstractNode) =
      if (lhs.touch(node).isClean && rhs.touch(node).isClean) Touch.Clean() else Touch.Multi()

  }

}
