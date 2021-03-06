package scalin

import scalin.mutable.dense._
import scalin.syntax.AllSyntax

import spire.algebra.Eq
import spire.math.SafeLong
import spire.implicits._

import org.scalatest.{FunSuite, PropSpec, Matchers}

class DeterminantSuite extends FunSuite with Matchers with AllSyntax {

  test("Wikipedia example: determinant of 3x3 matrix") {
    rowMajor[SafeLong](3,3)(
      -2, 2,-3,
      -1, 1, 3,
      2, 0,-1).determinant shouldBe 18
  }

}
