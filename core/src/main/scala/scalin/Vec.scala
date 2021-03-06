package scalin

import spire.algebra._

import algebra._

class PointwiseVec[A](val lhs: Vec[A]) extends AnyVal {

  //// Using standard Java methods

  def ==[VB <: Vec[Boolean]](rhs: A)(implicit ev: VecEngine[Boolean, VB]): VB =
    ev.pointwiseEqual(lhs, rhs)

  def ==[VB <: Vec[Boolean]](rhs: Vec[A])(implicit ev: VecEngine[Boolean, VB]): VB =
    ev.pointwiseEqual(lhs, rhs)

  def !=[VB <: Vec[Boolean]](rhs: A)(implicit ev: VecEngine[Boolean, VB]): VB =
    ev.pointwiseNotEqual(lhs, rhs)

  def !=[VB <: Vec[Boolean]](rhs: Vec[A])(implicit ev: VecEngine[Boolean, VB]): VB =
    ev.pointwiseNotEqual(lhs, rhs)

  //// With `A:MultiplicativeMonoid`

  def *[VA <: Vec[A]](rhs: Vec[A])(implicit ev: VecRing[A, VA]): VA = ev.pointwiseTimes(lhs, rhs)

  //// With `A:Ring`

  def +[VA <: Vec[A]](rhs: A)(implicit ev: VecRing[A, VA]): VA = ev.pointwisePlus(lhs, rhs)

  def -[VA <: Vec[A]](rhs: A)(implicit ev: VecRing[A, VA]): VA = ev.pointwiseMinus(lhs, rhs)

  //// With `A:Field`

  def /[VA <: Vec[A]](rhs: Vec[A])(implicit ev: VecField[A, VA]): VA = ev.pointwiseDiv(lhs, rhs)

  //// With `A:Eq`

  def ===[VB <: Vec[Boolean]](rhs: A)(implicit A: Eq[A], ev: VecEngine[Boolean, VB]): VB =
    ev.pointwiseEqv(lhs, rhs)

  def ===[VB <: Vec[Boolean]](rhs: Vec[A])(implicit A: Eq[A], ev: VecEngine[Boolean, VB]): VB =
    ev.pointwiseEqv(lhs, rhs)

  def =!=[VB <: Vec[Boolean]](rhs: A)(implicit A: Eq[A], ev: VecEngine[Boolean, VB]): VB =
    ev.pointwiseNeqv(lhs, rhs)

  def =!=[VB <: Vec[Boolean]](rhs: Vec[A])(implicit A: Eq[A], ev: VecEngine[Boolean, VB]): VB =
    ev.pointwiseNeqv(lhs, rhs)

}

/** Vector trait; `A` is the scalar type. */
trait Vec[A] { lhs =>

  //// Abstract methods

  def length: Int

  def apply(k: Int): A

  //// Standard Java methods

  override def toString: String = Printer.vec(Vec.this)

  override def equals(any: Any): Boolean = any match {
    case rhs: Vec[_] => scalin.impl.Vec.equal(lhs, rhs)
    case _ => false
  }

  override def hashCode = scalin.impl.Vec.hashCode(lhs)

  //// Helper functions

  def pointwise: PointwiseVec[A] = new PointwiseVec[A](lhs)

  def copyIfOverlap(obj: AnyRef): Vec[A]

  //// Conversion/creation

  def to[VA <: Vec[A]](implicit ev: VecEngine[A, VA]): VA = ev.fromVec(lhs)

  def toRowMat[MA <: Mat[A]](implicit ev: MatEngine[A, MA]): MA = ev.toRowMat(lhs)

  def toColMat[MA <: Mat[A]](implicit ev: MatEngine[A, MA]): MA = ev.toColMat(lhs)

  //// Collection-like methods

  def ++[VA <: Vec[A]](rhs: Vec[A])(implicit ev: VecEngine[A, VA]): VA = ev.cat(lhs, rhs)

  def cat[VA <: Vec[A]](rhs: Vec[A])(implicit ev: VecEngine[A, VA]): VA = ev.cat(lhs, rhs)

  def count(f: A => Boolean)(implicit ev: VecEngine[A, _]): Int = ev.count(lhs)(f)

  /** scala.collection-like flatMap. */
  def flatMap[B, VB <: Vec[B]](f: A => Vec[B])(implicit ev: VecEngine[B, VB]): VB = ev.flatMap[A](lhs)(f)

  /** Flatten the vector. */
  def flatten[AA](implicit U: Vec.Unpack.AuxA[A, AA], ev: VecEngine[AA, VAA] forSome { type VAA <: Vec[AA] }): ev.Ret = {
    import U.proof
    // type VAA has been lost, however, if we make VAA a type parameter of flatten, the implicit search fails,
    // probably because we look twice for an instance of Vec[_]
    // this hack recovers the correct return type
    // same hack used in Mat
    ev.flatten[U.V[U.A]](lhs).asInstanceOf[ev.Ret]
  }

  def fold[A1 >: A](z: A1)(op: (A1, A1) => A1)(implicit ev: VecEngine[A, _]): A1 = ev.fold[A1](lhs)(z)(op)

  /** Maps the values of the elements. */
  def map[B, VB <: Vec[B]](f: A => B)(implicit ev: VecEngine[B, VB]): VB = ev.map[A](lhs)(f)

  //// Slices

  def apply[VA <: Vec[A]](sub: Subscript)(implicit ev: VecEngine[A, VA]): VA = ev.slice(lhs, sub)

  //// Shuffling elements around

  def reshape[MA <: Mat[A]](rows: Int, cols: Int)(implicit ev: MatEngine[A, MA]): MA =
    ev.reshape(lhs, rows, cols)

  //// With `A:MultiplicativeMonoid`

  /** Product of all elements. */
  def product(implicit ev: VecMultiplicativeMonoid[A, _]): A = ev.product(lhs)

  /** Vector-scalar product. */
  def *[VA <: Vec[A]](rhs: A)(implicit ev: VecRing[A, VA]): VA = ev.times(lhs, rhs)

  /** Scalar-vector product. */
  def *:[VA <: Vec[A]](realLhs: A)(implicit ev: VecRing[A, VA]): VA = ev.times(realLhs, lhs)

  /** Kronecker product. */
  def kron[VA <: Vec[A]](rhs: Vec[A])(implicit ev: VecMultiplicativeMonoid[A, VA]): VA = ev.kron(lhs, rhs)

  /** Dyadic product by vector, which we don't call outer product, because we don't want to involve complex conjugation. */
  def dyad[MA <: Mat[A]](rhs: Vec[A])(implicit ev: MatMultiplicativeMonoid[A, MA]): MA = ev.dyad(lhs, rhs)

  //// With `A:AdditiveGroup`

  def +[VA <: Vec[A]](rhs: Vec[A])(implicit ev: VecRing[A, VA]): VA = ev.plus(lhs, rhs)

  def -[VA <: Vec[A]](rhs: Vec[A])(implicit ev: VecRing[A, VA]): VA = ev.minus(lhs, rhs)

  def unary_-[VA <: Vec[A]](implicit ev: VecRing[A, VA]): VA = ev.negate(lhs)

  def sum(implicit ev: VecRing[A, _]): A = ev.sum(lhs)

  def nnz(implicit ev: VecRing[A, _], eq: Eq[A]): Int = ev.nnz(lhs)

  //// With `A:Ring`

  /** Vector-matrix product, where vectors are interpreted as row vectors. */
  def *[VA <: Vec[A]](rhs: Mat[A])(implicit ev: VecRing[A, VA]): VA = ev.times(lhs, rhs)

  /** Dot product. Does not perform complex conjugation, thus it is equivalent to the real
    * inner product, but not the complex inner product.*/
  def dot[VA <: Vec[A]](rhs: Vec[A])(implicit ev: VecRing[A, VA]): A = ev.dot(lhs, rhs)

  //// With `A:EuclideanRing`

  def gcd(implicit ev: VecEuclideanRing[A, _]): A = ev.gcd(lhs)

  def lcm(implicit ev: VecEuclideanRing[A, _]): A = ev.lcm(lhs)

  //// With `A:Field`

  def /[VA <: Vec[A]](rhs: A)(implicit ev: VecField[A, VA]): VA = ev.div(lhs, rhs)

}

object Vec {

  trait Unpack[VA] {
    type V[X] <: Vec[X]
    type A
    implicit def proof: Vec[VA] =:= Vec[V[A]]
  }

  object Unpack {
    type AuxA[VA, A0] = Unpack[VA] { type A = A0 }
    def apply[VA](implicit U: Unpack[VA]): U.type {
      type V[X] = U.V[X]
      type A = U.A
    } = U
    implicit def unpack[V0[X] <: Vec[X], A0]: Unpack[V0[A0]] {
      type V[X] = V0[X]
      type A = A0
    } = new Unpack[V0[A0]] {
      type V[X] = V0[X]
      type A = A0
      def proof = implicitly
    }
  }

}

