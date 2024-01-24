# Enum

この章では、IxiaSで提供している列挙型（Enum）を実装するためのユーティリティの使い方を説明します。

以降のコード例では、以下のimportを想定しています。
```scala
import ixias.util._
```

IxiaSでは、列挙型を実装するためのユーティリティとして、`Enum`と`EnumStatus`,`EnumBitFlags`を提供しています。

Scalaにはネイティブの列挙型が存在しますが、Nextbeatではネイティブの列挙型を使用することは推奨していません。

理由としては主に以下があります。

- Enumerationで定義された個々の型は、Enumerationの`Value`という型に統一されてしまうため
- 個々のEnumerationにプロパティを持たせることが難しいため
- 個々のEnumerationにメソッドを追加することが難しいため

上記理由により、Nextbeatでは社内共通ライブラリであるIxiaSでEnumを実装し、列挙型を簡単に構築できるようにしています。

## ScalaのEnumeration

ScalaのEnumerationを使用する場合、以下のように定義します。

```shell
scala> object Color extends Enumeration {
     |   val Red, Green, Blue, White, Black = Value
     | }
object Color
```

定義したEnumerationを使用して見ると以下のように`Value`型として扱われているのがわかります。

```shell
scala> val color = Color.Red
val color: Color.Value = Red
```

> Enumerationで定義された個々の型は、Enumerationの`Value`という型に統一されてしまうため

型が統一されてしまうため以下のように特定のEnum値のみを受け付けるということができません。

```shell
scala> val color: Color.Red = Color.Red
                        ^
       error: type Red is not a member of object Color
```

特定のEnum値のみを受け付けるためには、以下のように`Value`型でパターンマッチ等を行い想定したEnum値以外を受け取った場合に例外を発生させるというような手法を取らなければいけません。

※ 良いサンプルがあれば修正お願いします！

```scala
def colorToString(color: Color.Value): String = color match {
  case Color.Blue  => "青"
  case _           => throw new IllegalArgumentException("想定外の色です")
}
```

これでは型の制約を十分に受けることができません。

> - 個々のEnumerationにプロパティを持たせることが難しいため
> - 個々のEnumerationにメソッドを追加することが難しいため

またネイティブのEnumは`Value`型で固定されており、個々のプロパティは`val`で定義を行います。
そのため、個々のEnumにメソッドを追加したりプロパティを渡すができません。

もし上記の特性をもったEnumを実装したい場合は、`Value`型を継承したクラスの作成と`Value`型の暗黙の型変換等を組み合わせて実装を行う必要があります。

```scala
import scala.language.implicitConversions

object Planet extends Enumeration {
  
  // Value型を継承した新たなクラスを作成
  protected case class PlanetVal(mass: Double, radius: Double) extends super.Val {
    def surfaceGravity: Double = Planet.G * mass / (radius * radius)
    def surfaceWeight(otherMass: Double): Double = otherMass * surfaceGravity
  }

  // 暗黙の型変換を定義し、Value型からPlanetVal型への変換を行う
  implicit def valueToPlanetVal(x: Value): PlanetVal = x.asInstanceOf[PlanetVal]

  val G: Double = 6.67300E-11
  val Mercury = PlanetVal(3.303e+23, 2.4397e6)
  val Venus   = PlanetVal(4.869e+24, 6.0518e6)
  val Earth   = PlanetVal(5.976e+24, 6.37814e6)
  val Mars    = PlanetVal(6.421e+23, 3.3972e6)
  val Jupiter = PlanetVal(1.9e+27, 7.1492e7)
  val Saturn  = PlanetVal(5.688e+26, 6.0268e7)
  val Uranus  = PlanetVal(8.686e+25, 2.5559e7)
  val Neptune = PlanetVal(1.024e+26, 2.4746e7)
}
```

上記のような実装を行うことで条件を満たしたEnumを作成することができます。

```scala
scala> Planet.Saturn.surfaceGravity
val res0: Double = 10.44978014597121

scala> println(Planet.values.filter(_.radius > 7.0e6))
Planet.ValueSet(Jupiter, Saturn, Uranus, Neptune)
```

ただ、Enumを構築する度にこのような実装を行うのは大変です。

## IxiaSのEnum

まず、Enumという名前のトレイトが定義されています。これはSerializableを継承しており、Enum型の基本的な特性を提供します。

```scala
trait Enum extends Serializable

```

次に、EnumStatusとEnumBitFlagsという2つのトレイトが定義されています。これらはEnumを継承しており、それぞれ短い整数（Short）と長い整数（Long）をコードとして持つことができます。

```scala
trait EnumStatus   extends Enum { val code: Short }
trait EnumBitFlags extends Enum { val code: Long  }
```

EnumStatusとEnumBitFlagsに対応するオブジェクトも定義されています。これらのオブジェクトは、それぞれのEnum型の操作と生成するためのメソッドを提供します。

**EnumStatus**

EnumStatusは以下のように構築します。

まず、EnumStatusを元に独自のEnum型を定義します。

```scala
sealed abstract class Color(val code: Short) extends EnumStatus
```

次に、EnumStatusのオブジェクトを構築し、個々のEnum値を定義します。

```scala
object Color extends EnumStatus.Of[Color] {
  case object Red extends Color(code = 0)
  case object Green extends Color(code = 1)
  case object Blue extends Color(code = 2)
  case object White extends Color(code = 3)
  case object Black extends Color(code = 4)
}
```

`EnumStatus.Of[T]`は`Enum.Of[T]`のエイリアスのようなもので、EnumStatusとしてEnumを構築するためのものです。

`Enum.Of[T]`には個々のEnum値に対して処理を行うための様々なメソッドが提供されています。

- values: Enumが持つ全てのEnum値を取得する
- find: Enumが持つ全てのEnum値から条件に合致するEnum値を取得する
- filter: Enumが持つ全てのEnum値から条件に合致するEnumの一覧を取得する
- indexOf: Enumが持つ全てのEnum値から条件に合致するEnum値のインデックスを取得する
- etc...

EnumStatusを使用した際に型がEnumStatusやColorになっておらず個々のEnum値になっていることがわかります。

```shell
scala> Color.Red
val res0: Color.Red.type = Red
```

また、EnumStatusを使用した際に想定したEnum値以外を受け取った場合にはコンパイルエラーが発生します。

```shell
scala> def test(color: Color.Red.type): String = color.toString
def test(color: Color.Red.type): String

scala> test(Color.Red)
val res1: String = Red

scala> test(Color.Blue)
                             ^
       error: type mismatch;
        found   : Color.Blue.type
        required: Color.Red.type
```

`Enum.Of[T]`で提供されているメソッドを使用することで、Enum値の一覧を取得したり、条件に合致するEnum値を取得することができます。

```shell
scala> Color.values
val res2: List[Color] = List(Red, Green, Blue, White, Black)
```

```shell
scala> Color.find(_.code == 1)
val res2: Option[Color] = Some(Green)
```

**EnumBitFlags**

EnumBitFlagsは名前の通り、ビットフラグを使用したEnumを構築するためのものです。

まずビットとは、コンピュータ上において「0か1が入る箱」を表します。

例えば「4ビット」と言った場合は「0か1が入る箱が4つある」という意味になります。

ビットは コンピュータが処理する最小単位と言われており、コンピュータはこのビットを組み合わせて様々な情報を表現します。

例えば、4ビットを組み合わせて「0000」から「1111」までの16種類の情報を表現することができます。

0000は数字の0、0001は数字の1というように表現できます。

このようにビットを組み合わせて表現する情報のことを「ビット列」と言います。

ビット演算とは、このビット列を組み合わせたり各ビットの移動を行うことで、新たなビット列を生成する演算のことです。

ビット単位の論理演算としては、以下のようなものがあります。

- AND演算（&）: 両方のビットが1のときに1を返します。それ以外の場合は0を返します。 
- OR演算（|）: どちらかのビットが1のときに1を返します。両方のビットが0の場合のみ0を返します。 
- XOR演算（^）: 両方のビットが異なるときに1を返します。両方のビットが同じ場合は0を返します。 
- NOT演算（~）: ビットを反転します。1のビットは0に、0のビットは1になります。

ビットフラグとは簡単にいうと、ビット演算を使用してビット列の各桁が`0`か`1`で判別を行い、`0`をOFF、`1`をONと定義し大量の状態（フラグ）を一つの数字として管理する方法です。

例えば、`0001`と`0010`のビット列を持つ場合、これらをビット演算を使用して組み合わせると`0011`というビット列を生成することができます。

これをビットフラグ(フラグが立っているか)で判別を行うと、`0011`のビット列は、`0001`と`0010`のビット列のフラグが立っていることを表すことができます。

つまり`0011`(数字の3)として値を保持しているが、使用する際には`0001`と`0010`のフラグが立っていることを判別することで、`0001`(数字の1)と`0010`(数字の2)のビット列を持っていることを表現することができます。

Nextbeatでは、EnumBitFlagsを使用してビットフラグを使用したEnumの構築を行い、DBにビット列を組み合わせた結果の数字を格納し、アプリケーションで使用する際にはそれぞれのEnum値を配列として使用しています。

DBには数字の3を格納 => アプリケーションでは数字の1と2として使用 (配列として数字の1と2を持つイメージ)

EnumBitFlagsは以下のように構築します。

```scala
sealed abstract class Color(val code: Long) extends EnumBitFlags
```

`EnumBitFlags.Of[T]`は`Enum.Of[T]`のエイリアスのようなもので、EnumBitFlagsとしてEnumを構築するためのものですが、`Enum.Of[T]`のメソッドに加えて、ビット演算を使用したEnum値の操作を行うためのメソッドが提供されています。

- `apply(bitset: Long): Seq[T]`: 与えられたビットセット（Long型の整数）に対して、そのビットセットが表すフラグを持つEnumのインスタンスをすべて返します。(AND演算)
- `toBitset(bitset: Seq[T]): Long`: 与えられたEnumのインスタンスのリストに対して、それらが表すビットセットをLong型の整数として返します。(OR演算)
- etc...

EnumBitFlagsのオブジェクトを構築し、個々のEnum値を定義します。

```scala
object Color extends EnumBitFlags.Of[Color] {
  case object Red extends Color(code = 0x00000001) // 1L << 0 とか書き方はプロダクトによって変わります
  case object Green extends Color(code = 0x00000002)
  case object Blue extends Color(code = 0x00000004)
  case object White extends Color(code = 0x00000008)
  case object Black extends Color(code = 0x00000010)
}
```

使用方法はEnumStatusと同様です。

```shell
scala> Color.Red
val res0: Color.Red.type = Red

scala> res0.code
val res1: Long = 1
```

`toBitset`を使用してそれぞれのEnum値をビット列として組み合わせることができます。

```shell
scala> Color.toBitset(Seq(Color.Red, Color.Green))
val res2: Long = 3
```

`apply`を使用してEnum値をビット列として組み合わせた値からそれぞれのEnum値に変換することができます。

```shell
scala> Color(res2)
val res3: Seq[Color] = List(Red, Green)
```

IxiaSのEnumBitFlagsを使用することで、1つの値で複数のEnum値を表現することができるようになり構築も簡単に行うことができます。
