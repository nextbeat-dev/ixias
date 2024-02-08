# Entity Model

以降のコード例では、以下のimportを想定しています。

```scala
import ixias.model._
```

まず、IxiaSのEntity ModelはIdを持ちます。Idはコンパニオンオブジェクト内に定義を行い、Idの型と値を定義する必要があります。
Idは同一性を持つ識別子の役割を持っています。

```scala
object User {
  val  Id = the[Identity[Id]]
  type Id = Long @@ User
}
```

Idの型は`shapeless.tag.@@`を用いて定義します。
`@@`は`shapeless.tag.@@`の型エイリアスです。

```scala
type @@[+T, U] = shapeless.tag.@@[T, U]
```

こちらはTagged Typeと呼ばれる手法によって、Long型の値を扱うIdという異なる名前の型を持たせています。IdはLong型またはString型を選択することができます。

EntityModelクラスには、id、updatedAt、createdAtの3つの値を定義します。これらはEntityModelトレイトに抽象な値として定義されており、命名と型を定められた通りに定義する必要があります。

```scala
case class User(
  id:        Option[User.Id],
  updatedAt: LocalDateTime,
  createdAt: LocalDateTime
) extends EntityModel[User.Id]
```

idの値は、Option[Id]型を持ちます。Option型にすることでIdがあるかないかを判別できます。

インスタンスの生成は、以下のように行います。

```scala
val user: User#WithNoId = User(
  id        = None,
  updatedAt = LocalDateTime.now(),
  createdAt = LocalDateTime.now()
).toWithNoId
```

## WithNoIdとEmbeddedIdの型の定義

EntityModelトレイト内には先ほど`User#WithNoId`として登場したWithNoId型と共に、WithNoId型と対になる関係を持つEmbeddedId型が定義されています。2つの型は以下のような実装になります。

```scala
type WithNoId = Entity.WithNoId [Id, this.type]
type EmbeddedId = Entity.EmbeddedId[Id, this.type]
```

これらは文脈を持つ型のような役割を持ち、WithNoIdはidを持たないEntity、EmbeddedIdはidを持つEntityという意味を持った型になります。

ここで呼び出しているEntity.WithNoIdとEntity.EmbeddedIdの型は以下のような実装になります。

```scala
type WithNoId [K <: @@[_, _], M <: EntityModel[K]] =
  Entity[K, M, IdStatus.Empty]
type EmbeddedId[K <: @@[_, _], M <: EntityModel[K]] =
  Entity[K, M, IdStatus.Exists]
```

いずれも、1つ目の型パラメータKにはコンパニオンオブジェクト内で定義したIdを、2つ目の型パラメータMにはEntityModelクラスを受け取ります。

呼び出し元であるEntityModelトレイト内では、コンパニオンオブジェクト内で定義したIdとEntityModelトレイトを継承しているthis.typeを与えることで型パラメータの制約を満たしています。

そしてEntity.WithNoIdとEntity.EmbeddedIdでは、受け取った型であるKとMを用いてEntityクラスを呼び出しています。

ここで注目すべきは、Entityクラスが受け取る3つ目の型パラメータです。WithNoIdとEmbeddedIdの型の定義において3つ目の型パラメータに与えているIdStatusオブジェクトは以下のような実装になります。

```scala
trait IdStatus
object IdStatus {
  trait Empty extends IdStatus
  trait Exists extends IdStatus
}
```

このIdStatusオブジェクト内に定義されているEmptyとExistsを用いて、WithNoIdとEmbeddedIdの判別を行います。

**WithNoIdとEmbeddedIdの値の生成**

次に、WithNoIdとEmbeddedIdの型を持つ値を生成するtoWithNoIdメソッドとtoEmbeddedIdメソッドについて説明します。2つのメソッドは以下のような実装になります。

```scala
def toWithNoId: WithNoId = Entity.WithNoId(this)
def toEmbeddedId: EmbeddedId = Entity.EmbeddedId(this)
```

それぞれ、自身のインスタンスであるthisを引数としてEntityクラス内のオブジェクトを呼び出しています。

toWithNoIdメソッドを例に挙げると、toWithNoIdメソッドが呼び出しているEntity.WithNoIdオブジェクトの実装は以下のようになっています。

```scala
object WithNoId {
  /** Create a entity object with no id. */
  def apply[K <: @@[_, _], M <: EntityModel[K]](data: M): WithNoId[K, M] =
    data.id match {
      case None => new Entity(data)
      case Some(_) =>
        throw new IllegalArgumentException(“The entity’s ...”)
    }
}
```

与えられたEntityModelクラスのidをmatch式で判別することで、エラーハンドリングがされるような実装となっています。new User(…)内のidの値にOption型のNoneを与えているのはそのためです。

toWithNoIdメソッドに対してtoEmbeddedIdメソッドは、EmbeddedId型の値を生成します。しかし、IxiaSではEntity.EmbeddedId()を用いるユースケースはあまり多くありません。

理由としては、ixais.slickパッケージを組み合わせて永続ストレージとの連携を行う際、永続ストレージへのデータ取得リクエストによる返り値の型がEmbeddedIdにマッピングできるようになっているからです。

## Entityクラスの実装

Entityクラスにおいて`WithNoId`型と`EmbeddedId`型の違いを見ていきます。

まずはUserモデルの`WithNoId`型を生成して見ましょう。

```shell
scala> val user: User#WithNoId = User(
  id        = None,
  updatedAt = LocalDateTime.now(),
  createdAt = LocalDateTime.now()
).toWithNoId

// val user: ixias.model.Entity[Long with shapeless.tag.Tagged[User],User,ixias.model.IdStatus.Empty] = Entity(User(None,2024-01-23T16:32:02.347987,2024-01-23T16:32:02.347994))
```

WithNoId型であるため、idの値がNoneになっていることが分かります。

この状態でidにアクセスを行なって見ましょう

```shell
scala> user.id
// error: Cannot prove that ixias.model.IdStatus.Empty =:= ixias.model.IdStatus.Exists.
```

idの値が存在しないため、コンパイルエラーが発生します。
なぜコンパイルエラーとなるのか？それはEntityクラス内の`id`実装を見てみると分かります。

```scala
/** get id value when id is exists */
def id(implicit ev: S =:= IdStatus.Exists): K = v.id.get
```

Entityクラス内に定義された`id`には`=:=`を用いた型制約が暗黙的に与えられており、型パラメータSにIdStatus.Existsが与えられているEmbeddedIdでないとidメソッドが提供されない仕様になっているためです。

そのため、型が`WithNoId`であるuserはidメソッドを呼ぶとエラーが起きます。

その他のプロパティに関しては`{model}.v.{property}`のようにv(value)に続けて入力することで、EntityModelクラス内の値を取得できます。(EmbeddedId型の場合も同様です。)

試しに`v`経由ではなく直説アクセスを行うとエラーが発生します。

```shell
scala> user.updatedAt
            ^
       error: value updatedAt is not a member of ixias.model.Entity[Long with shapeless.tag.Tagged[User],User,ixias.model.IdStatus.Empty]
```

`v`を経由することでアクセスすることができます。

```shell
scala> user.v.updatedAt
val res3: java.time.LocalDateTime = 2024-01-23T16:32:02.347987
```

次に、Userモデルの`EmbeddedId`型を生成して見ましょう。

まずEntityクラスの識別子となるIdは以下のように生成します。

```shell
scala> val userId = User.Id(1)
val userId: User.Id = 1
```

次に、`EmbeddedId`型を生成します。

```shell
scala> val user: User#EmbeddedId = User(
     |   id = Some(userId),
     |   updatedAt = LocalDateTime.now(),
     |   createdAt = LocalDateTime.now()
     | ).toEmbeddedId
val user: ixias.model.Entity[Long with shapeless.tag.Tagged[User],User,ixias.model.IdStatus.Exists] = Entity(User(Some(1),2024-01-23T16:46:03.249446,2024-01-23T16:46:03.249462))
```

idの値がSome(userId)になっていることが分かります。

EmbeddedId型の場合はエラーが起きずにidを取得することができます。

```shell
scala> user.id
val res0: Long with shapeless.tag.Tagged[User] = 1
```

EmbeddedIdはidを持っていることを保証する型であるため、Option型で返す必要がありません。

この制約によりコンパイル時にエラーとなり、障害を減らすことができるようです。

最初の方に記載した文脈を持つ型の説明に戻ります。

> これらは文脈を持つ型のような役割を持ち、WithNoIdはidを持たないEntity、EmbeddedIdはidを持つEntityという意味を持った型になります。

IxiaSを使用してモデルを定義する際、EntityModelトレイトを継承したクラスを定義することで、Idを持たないWithNoId型とIdを持つEmbeddedId型の2つの型を取り扱います。
NextbeatではIxiaSのEntityを使用したモデルは、DBのテーブルと1対1で対応するデータ型として使用しています。
また、NextbeatではDBのテーブルには識別子としてAuto Incrementの値を持つ`id`カラムを設定しています。

ここでいうIdを持たないWithNoId型とはDBにデータを格納する前(Auto Incrementによって採番される前)のレコードを表現して、Idを持つEmbeddedId型とはDBにデータを格納した後(Auto Incrementによって採番された後)のレコードを表現しています。

## EntityModelの更新

Entityにはmapメソッドが用意されており、Functionを適用した新たなEntityを返すことができます。実装は以下のようになっています。

```scala
/** Builds a new `Entity` by applying a function to values. */
@inline def map[M2 <: EntityModel[K]](f: M => M2): Entity[K, M2, S] = new Entity(f(v))
```

Entityの更新を行いたい場合は、mapメソッドを用いて以下のように実装します。

```shell
scala> val updatedUser: User#EmbeddedId = user.map(_.copy(id = Some(User.Id(2))))
val updatedUser: ixias.model.Entity[Long with shapeless.tag.Tagged[User],User,ixias.model.IdStatus.Exists] = Entity(User(Some(2),2024-01-23T16:46:03.249446,2024-01-23T16:46:03.249462))
```

## 引用元

[自社OSS「IxiaS」の紹介 ~ ixais.modelパッケージのサンプルコード ~](https://medium.com/nextbeat-engineering/%E8%87%AA%E7%A4%BEoss-ixias-%E3%81%AE%E7%B4%B9%E4%BB%8B-ixais-model%E3%83%91%E3%83%83%E3%82%B1%E3%83%BC%E3%82%B8%E3%81%AE%E3%82%B5%E3%83%B3%E3%83%97%E3%83%AB%E3%82%B3%E3%83%BC%E3%83%89-d6e0e5d8e8aa)
