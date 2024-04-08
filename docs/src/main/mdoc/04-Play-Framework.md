# Play Framework

この章では、IxiaSがラップしているPlayFrameworkの使い方を説明します。

プロジェクトに以下の依存関係を設定する必要があります。

@@@ vars
```scala
libraryDependencies ++= Seq(
  "$org$" %% "ixias-play-core" % "$version$"
)
```
@@@

## JsonHelper

`ixias-play-core` には、Jsonのシリアライズ/デシリアライズを行うためのヘルパークラスが含まれています。

JsonHelperは、Play FrameworkのリクエストデータをJSONとしてバインドするためのヘルパートレイトです。このトレイトは、リクエストデータを特定の型Tにバインドするための`bindFromRequest[T]`メソッドを提供します。また、JsonHelperトレイトはJsonHelperオブジェクトにミックスインされており、このオブジェクトを通じてメソッドを利用することができます。

```scala
trait JsonHelper {

  /** To bind request data to a `T` component.
    */
  def bindFromRequest[T](implicit req: Request[AnyContent], reads: Reads[T]): Either[Result, T]
}
```

以下に、JsonHelperオブジェクトの使用方法を示します。

まず、bindFromRequest[T]メソッドを使用してリクエストデータをバインドします。このメソッドは、リクエストデータを指定した型Tにバインドしようと試み、成功した場合はRight[T]を、失敗した場合はLeft[Result]を返します。

このメソッドを使用する際には、T型のデータをJSONから変換するためのReads[T]インスタンスが必要です。このReadsは、Play FrameworkのJSONライブラリ、Play Jsonを使用して生成します。

```scala
def myAction = Action { implicit request =>
  JsonHelper.bindFromRequest[MyData] match {
    case Right(myData) =>
      // バインドに成功した場合の処理
      Ok("Got data: " + myData)
    case Left(badRequest) =>
      // バインドに失敗した場合の処理
      badRequest
  }
}
```

ここで、MyDataはリクエストデータを表す任意の型で、この型に対するReadsインスタンスが必要です。Readsは、JSONデータをScalaのオブジェクトに変換するための型クラスです。このReadsインスタンスは通常、Json.reads[MyData]メソッドを使用して自動的に生成されます。

```scala
case class MyData(name: String, age: Int)

object MyData {
  implicit val reads: Reads[MyData] = Json.reads[MyData]
}
```

以上がJsonHelperオブジェクトの基本的な使用方法です。このオブジェクトを使用することで、Play Frameworkのリクエストデータを簡単にJSONとしてバインドすることができます。

## FormHelper

FormHelperは、Play Frameworkのリクエストデータを特定の型Tにバインドするためのヘルパートレイトです。このトレイトは、リクエストデータを特定の型TにバインドするためのbindFromRequest[T]メソッドを提供します。また、FormHelperトレイトはFormHelperオブジェクトにミックスインされており、このオブジェクトを通じてメソッドを利用することができます。

```scala
trait FormHelper {

  /** To bind request data to a `T` component.
    */
  def bindFromRequest[T](mapping: Mapping[T])(implicit
    req:      Request[_],
    provider: MessagesProvider
  ): Either[Result, T]
}
```

以下に、FormHelperオブジェクトの使用方法を示します。

まず、bindFromRequest[T]メソッドを使用してリクエストデータをバインドします。このメソッドは、リクエストデータを指定した型Tにバインドしようと試み、成功した場合はRight[T]を、失敗した場合はLeft[Result]を返します。

このメソッドを使用する際には、T型のデータをフォームから変換するためのMapping[T]インスタンスが必要です。このMappingは、Play Frameworkのフォームライブラリを使用して生成します。

```scala
import play.api.data._
import play.api.data.Forms._

case class MyData(name: String, age: Int)

val myDataMapping: Mapping[MyData] = mapping(
  "name" -> text,
  "age"  -> number
)(MyData.apply)(MyData.unapply)

def myAction = Action { implicit request =>
  FormHelper.bindFromRequest(myDataMapping) match {
    case Right(myData) =>
      // バインドに成功した場合の処理
      Ok("Got data: " + myData)
    case Left(badRequest) =>
      // バインドに失敗した場合の処理
      badRequest
  }
}
```

この例では、まずMyData型のMappingインスタンスをmappingメソッドで生成しています。このMappingインスタンスは、フォームデータをMyData型のオブジェクトに変換する方法を定義しています。そして、bindFromRequest[MyData]メソッドを使用して、リクエストデータをMyData型にバインドしています。

以上がFormHelperオブジェクトの基本的な使用方法です。このオブジェクトを使用することで、Play Frameworkのリクエストデータを簡単に特定の型にバインドすることができます。

## Binder

Bindersは、特定の型のデータをリクエストパラメータからバインドするためのヘルパートレイトです。このトレイトは、QueryStringBindableやPathBindableなどのインスタンスを提供します。これらのインスタンスは、リクエストパラメータを特定の型にバインドするためのメソッドを提供します。

以下に、Bindersの使用方法を示します。

まず、PathBindableとQueryStringBindableのインスタンスを定義します。これらのインスタンスは、リクエストパラメータを特定の型にバインドするためのメソッドを提供します。

```scala
import ixias.play.api.mvc.Binders._

trait Binders extends ixias.play.api.mvc.Binders {

  val Hoge = pacage.hoge.Hoge

  implicit val pathBind: PathBindable[Box[Hoge.Id]] = pathBindableBoxId[Hoge.Id]
  implicit val queryBind: QueryStringBindable[Box[Hoge.Id]] = queryStringBindableBoxId[Hoge.Id]
}
```

※ Play Frameworkのroutesファイルはテキストファイルを処理してルーティングの構築を行なっています。そのためバインドを行う型情報は全てパッケージのフルパスを記載するか、変数として定義しておきルーティングの変換処理でも参照できるようにしておかなければいけません。

作成したBindersをPlay Frameworkのルーティングで使用できるようにbuild.sbtに設定します。

```sbt
RoutesKeys.routesImport := Seq(
  "Binders._"
)
```

次に、routesファイルでこれらのバインダーを使用します。以下の例では、:idパスパラメータをBox[Location.Id]型にバインドしています。

```scala
GET /hoge/:id controllers.HogeController.getById(id: Box[Hoge.Id])
```

最後に、コントローラーでバインドされたパラメータを使用します。以下の例では、getByIdメソッドのidパラメータがBox[Hoge.Id]型になっています。

```scala
def getById(id: Hoge.Id) = Action async {
   // ...
}
```

パスパラメーターではなくクエリパラメーターとしてバインドする場合は`BoxCsv`を使用してroutesファイルを以下のように記載します。

```scala
GET /hoge controllers.HogeController.getById(id: BoxCsv[Hoge.Id])
```

以上がBindersの基本的な使用方法です。このトレイトを使用することで、Play Frameworkのリクエストパラメータを簡単に特定の型にバインドすることができます。

## DeviceDetection

DeviceDetectionBuilderは、ユーザーエージェントに基づいてデバイス情報を検出するためのカスタムアクションを提供します。これにより、リクエストが行われたデバイスのOS、デバイスタイプ、ユーザーエージェント、モバイルデバイスかどうかといった情報を取得することができます

以下に、DeviceDetectionBuilderの使用方法を示します。

まず、DeviceDetectionアクションをコントローラーのアクションメソッドで使用します。このアクションは、リクエストのユーザーエージェントを解析し、デバイス情報をリクエスト属性として追加します。

```scala
def myAction = DeviceDetectionBuilder(play.api.mvc.parse.default) { request =>
  // デバイス情報を取得
  val os        = request.attrs(DeviceDetectionAttrKey.OS)
  val device    = request.attrs(DeviceDetectionAttrKey.Device)
  val userAgent = request.attrs(DeviceDetectionAttrKey.UserAgent)
  val isMobile  = request.attrs(DeviceDetectionAttrKey.IsMobile)

  // デバイス情報を使用した処理
  Ok(s"OS: $os, Device: $device, UserAgent: $userAgent, IsMobile: $isMobile")
}
```

この例では、DeviceDetectionアクションを使用してリクエストを処理しています。このアクションは、リクエストのユーザーエージェントを解析し、デバイス情報をリクエスト属性として追加します。その後、これらの属性を取得して使用します。

以上がDeviceDetectionBuilderの基本的な使用方法です。このビルダーを使用することで、Play Frameworkのリクエストからデバイス情報を簡単に取得することができます。

## RequestHeaderAttrHelper

RequestHeaderAttrHelperは、Play Frameworkのリクエストヘッダーから特定の属性を取得するためのヘルパーオブジェクトです。このヘルパーは、リクエストヘッダーに格納された特定のキーに対応する値を取得するためのメソッドを提供します。 

以下に、RequestHeaderAttrHelperの使用方法を示します。

まず、RequestHeaderAttrHelperのgetメソッドを使用してリクエストヘッダーから特定の属性を取得します。このメソッドは、指定したキーに対応する値を取得しようと試み、成功した場合はRight[T]を、失敗した場合はLeft[Result]を返します。

```scala
import ixias.play.api.mvc.RequestHeaderAttrHelper
import play.api.mvc.{ RequestHeader, Result }
import play.api.libs.typedmap.TypedKey

// TypedKeyの定義
val MyKey: TypedKey[String] = TypedKey[String]("myKey")

def myAction() = Action { implicit request =>
  RequestHeaderAttrHelper.get(MyKey) match {
    case Right(value) =>
      // 属性の取得に成功した場合の処理
      Ok("Got value: " + value)
    case Left(result) =>
      // 属性の取得に失敗した場合の処理
      result
  }
}
```

この例では、まずMyKeyというTypedKeyを定義しています。このキーは、リクエストヘッダーに格納された属性を識別するためのものです。そして、getメソッドを使用して、リクエストヘッダーからMyKeyに対応する値を取得しています。

以上がRequestHeaderAttrHelperの基本的な使用方法です。このヘルパーを使用することで、Play Frameworkのリクエストヘッダーから特定の属性を簡単に取得することができます。

## BaseExtensionMethods

BaseExtensionMethodsは、Play FrameworkのBaseControllerHelpersを拡張するためのトレイトです。このトレイトは、リクエストヘッダーから特定の属性を取得したり、フォームデータを特定の型にバインドしたり、デバイス情報を取得したりするためのヘルパーオブジェクトやメソッドを提供します。

以下に、BaseExtensionMethodsの使用方法を示します。

まず、BaseExtensionMethodsをミックスインしたコントローラーを定義します。このコントローラーは、BaseExtensionMethodsが提供するヘルパーオブジェクトやメソッドを使用することができます。

```scala
import ixias.play.api.mvc.BaseExtensionMethods
import play.api.mvc._

class MyController @Inject() (implicit cc: MessagesControllerComponents) extends AbstractController(cc) with BaseExtensionMethods {
  // ...
}
```

BaseExtensionMethodsを使用すると先ほどまで紹介した機能をエイリアス名で使用できるようになります。

| 機能                      | エイリアス名          |
|-------------------------|-----------------|
| JsonHelper              | JsonHelper      |
| FormHelper              | FormHelper      |
| DeviceDetectionBuilder  | DeviceDetection |
| RequestHeaderAttrHelper | AttrHelper      |
