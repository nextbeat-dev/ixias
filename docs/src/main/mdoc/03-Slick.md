# Slick

この章では、IxiaSがラップしているSlickの使い方を説明します。

プロジェクトに以下の依存関係を設定する必要があります。

@@@ vars
```scala
libraryDependencies ++= Seq(
  "$org$" %% "ixias-slick" % "$version$",
  "mysql" % "mysql-connector-java" % "8.0.33"
)
```
@@@

以降のコード例では、以下のimportを想定しています。

```scala
import ixias.slick.jdbc.MySQLProfile.api._
import ixias.slick.builder._
```

NextbeatではSlickを使用したDBアクセスを行う際に、IxiaSのEntity Modelを使用しています。
Entity Modelの章をまだ読んでいない場合は先に[こちら](/ixias/01-Entity-Model.html)を読んでください。

本章で使用するモデルはEntity Modelの章で作成したUserモデルを使用します。

## MySQLProfile

IxiaSでSlickを使用する際は、公式が提供しているMySQLProfileを使用せずに、独自に作成したProfileを使用します。

```scala
ixias.slick.jdbc.MySQLProfile.api._
```

ではなぜ公式ではなく独自のProfileを使用するのでしょうか？

まず第一にSlick3.3からjava.time系のデータ型へのサポートが追加されたのですが、その時のデータの扱い方がRDBによって大きく差がある状態になりました。
特にMySQLProfileは、精度維持のためにほとんどの時間データを一旦文字列として取得してから変換するので、slick3.2以下で行っていたようなjava.sql.TimestampとLocalDateTimeのMappedColumnTypeを利用したものが動作しなくなってしまいました。

対応方法はいくつかありますが、公式推奨実装であるProfileを拡張して日付関連のマッピングをoverrideする方法をNextbeatは採用しています。
そのため、独自にProfileを作成して内部でマッピング処理を実装してプロダクト側に提供しています。

第二にIxiaSではEntity ModelやEnum等の独自の型を多様しています。Slickでは独自の型を扱う際にマッピング処理を作成しなければいけません。
プロダクトごとに同じようなマッピング処理を書くのは面倒です。
IxiaSで使用する独自の型に関してはライブラリ側でマッピング処理を実装し提供できるようにするため、独自のProfileを作成しています。

## Table定義

Table定義は以下のようになります。
基本的な実装はSlickのドキュメントに準じています。[参照](https://scala-slick.org/doc/stable/schemas.html)

```scala
class UserTable(tag: Tag) extends Table[User](tag, "user") {
  def id = column[User.Id]("id", O.PrimaryKey, O.AutoInc)
  def updatedAt = column[LocalDateTime]("updated_at", TsCurrent)
  def createdAt = column[LocalDateTime]("created_at", Ts)

  def * = (id.?, updatedAt, createdAt).<> (
    (User.apply _).tupled,
    (User.unapply _).andThen(_.map(_.copy(
      _2 = LocalDateTime.now()
    )))
  )
}
```

`Table`クラスのコンストラクタには、第1引数に`tag`、第2引数にテーブル名を指定します。

NextbeatではSlickを使用したDBアクセスを行う際に、更新処理系は更新を行いたいカラムのみを指定して更新を行うのではなく、モデル全体を使用して更新処理を行います。
そのため、`*`メソッドでのマッピング処理で`updated_at`カラムに対して`LocalDateTime.now()`を設定して現在日時が格納されるように設定する必要があります。

※ `*`メソッドの実装は、`<>`メソッドを使用しています。`<>`メソッドは、`*`メソッドで指定したカラムの値をモデルに変換する関数と、モデルをカラムの値に変換する関数を指定します。

### カラムのデータ型

Slickではカラムの型を定義する際に、`column`メソッドを使用します。その際に第2引数以降はカラムのデータ型を設定することができるのですが、本来は`slick.sql.SqlProfile.ColumnOption.SqlType`型を使用して任意でデータ型を設定します。
IxiaSでは`ixias.slick.jdbc.MySQLProfile.api`に`ixias.slick.jdbc.SlickColumnType`が組み込まれているためこちらを使用することで、より簡単にデータ型を設定することができます。

以下はIxiaSで提供されているデータ型の一覧です。

| 値                       | データ型                                                            |
|-------------------------|-----------------------------------------------------------------|
| Boolean                 | BOOLEAN                                                         |
| Int8                    | TINYINT                                                         |
| Int16                   | SMALLINT                                                        |
| Int32                   | INT                                                             |
| Int64                   | BIGINT                                                          |
| UInt8                   | TINYINT  UNSIGNED                                               |
| UInt16                  | SMALLINT UNSIGNED                                               |
| UInt32                  | INT      UNSIGNED                                               |
| UInt64                  | BIGINT   UNSIGNED                                               |
| AsciiChar8              | VARCHAR(8)   CHARACTER SET ascii                                |
| AsciiChar16             | VARCHAR(16)  CHARACTER SET ascii                                |
| AsciiChar32             | VARCHAR(32)  CHARACTER SET ascii                                |
| AsciiChar64             | VARCHAR(64)  CHARACTER SET ascii                                |
| AsciiChar128            | VARCHAR(128) CHARACTER SET ascii                                |
| AsciiChar255            | VARCHAR(255) CHARACTER SET ascii                                |
| AsciiCharBin8           | VARCHAR(8)   CHARACTER SET ascii COLLATE ascii_bin              |
| AsciiCharBin16          | VARCHAR(16)  CHARACTER SET ascii COLLATE ascii_bin              |
| AsciiCharBin32          | VARCHAR(32)  CHARACTER SET ascii COLLATE ascii_bin              |
| AsciiCharBin64          | VARCHAR(64)  CHARACTER SET ascii COLLATE ascii_bin              |
| AsciiCharBin128         | VARCHAR(128) CHARACTER SET ascii COLLATE ascii_bin              |
| AsciiCharBin255         | VARCHAR(255) CHARACTER SET ascii COLLATE ascii_bin              |
| Utf8Char8               | VARCHAR(8)   CHARACTER SET utf8mb4                              |
| Utf8Char16              | VARCHAR(16)  CHARACTER SET utf8mb4                              |
| Utf8Char32              | VARCHAR(32)  CHARACTER SET utf8mb4                              |
| Utf8Char64              | VARCHAR(64)  CHARACTER SET utf8mb4                              |
| Utf8Char128             | VARCHAR(128) CHARACTER SET utf8mb4                              |
| Utf8Char255             | VARCHAR(255) CHARACTER SET utf8mb4                              |
| Utf8BinChar8            | VARCHAR(8)   CHARACTER SET utf8mb4 COLLATE utf8mb4_bin          |
| Utf8BinChar16           | VARCHAR(16)  CHARACTER SET utf8mb4 COLLATE utf8mb4_bin          |
| Utf8BinChar32           | VARCHAR(32)  CHARACTER SET utf8mb4 COLLATE utf8mb4_bin          |
| Utf8BinChar64           | VARCHAR(64)  CHARACTER SET utf8mb4 COLLATE utf8mb4_bin          |
| Utf8BinChar128          | VARCHAR(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin          |
| Utf8BinChar255          | VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin          |
| DateTime                | DATETIME                                                        |
| Date                    | DATE                                                            |
| Time                    | TIME                                                            |
| Ts                      | TIMESTAMP DEFAULT CURRENT_TIMESTAMP                             |
| TsCurrent               | TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP |
| Text                    | TEXT CHARACTER SET utf8mb4                                      |
| Blob                    | BLOB                                                            |
| Decimal(m: Int, d: Int) | DECIMAL($m, $d)                                                 |

ここで定義されていないデータ型を使用したい場合は、`import slick.sql.SqlProfile.ColumnOption.SqlType`を使用してください。

## Database定義

Database定義は以下のようになります。
NextbeatではデータベースアクセスにHikariCPのコネクションプールを使用しています。

IxiaSにはこのHikariCPのコネクションプールをConfファイルから設定値を読み取り構築するビルダーが提供されています。

ビルダーはConfファイルから設定値を読み取るために、`DataSourceName`というものを使用します。

```scala
val dataSource = DataSourceName("ixias.db.mysql://slave/example")
```

`DataSourceName`にはConfファイルのパスを表す文字列を引数に取ります。
文字列の形式は`{path}://{hostspec}/{database}`という正規表現で構築されており、それぞれDataSourceNameの各プロパティに格納されます。

```scala
case class DataSourceName(
  path:     String,
  hostspec: String,
  database: String
)
```

ここでいう`path`とは単純にConfファイルのパスを表す文字列です。
NextbeatではDBアクセスを行う際に、読み取り用と書き込み用で接続先のDBを変更しています。
NextbeatではAWSのAurora on MySQLを使用しているため、`hostspec`には`master`と`slave`の2つの値が設定されます。
`database`はその名の通りDB名です。

`application.conf`等のconfファイルには以下のように設定します。

```txt
ixias.db.mysql {
  example {
    username                 = "username"
    password                 = "password"
    driver_class_name        = "com.mysql.cj.jdbc.Driver"
    hostspec.master.jdbc_url = "jdbc:mysql://127.0.0.1:3306/example"
    hostspec.slave.jdbc_url  = "jdbc:mysql://127.0.0.1:3306/example"
  }
}
```

`hostspec`には`master`と`slave`の2つの値が設定されており、それぞれの値には`jdbc_url`というキーで接続先のDBのURLを設定して接続先を変更しています。

※ ローカル環境なので接続先は同じですが、本番環境では接続先が異なります。

ではなぜこのようなネストした設定を取得できるのでしょうか？

IxiaSではConfファイルから設定値を読み取る際に、`DataSourceName`の`path`、`database`、`hostspec`の値を組み合わせてそれぞれの組み合わせでConfファイルへのパスを構築しています。
そのためネストした設定値でも読み取ることができるのです。

```scala
def readValue[A](f: Configuration => Option[A])(implicit dsn: DataSourceName): Option[A] =
    Seq(
      dsn.path + "." + dsn.database + "." + CF_SECTION_HOSTSPEC.format(dsn.hostspec),
      dsn.path + "." + dsn.database,
      dsn.path + "." + CF_SECTION_HOSTSPEC.format(dsn.hostspec),
      dsn.path
    ).foldLeft[Option[A]](None) {
      case (prev, path) => prev.orElse {
        config.get[Option[Configuration]](path).flatMap(f(_))
      }
    }
```

Nextbeatではデータベースを複数作成しているプロダクトがありますが、このような設定を取得できるのでConfファイルの設定値を共通化することができます。

```txt
ixias.db.mysql {
  username          = "username"
  password          = "password"
  driver_class_name = "com.mysql.cj.jdbc.Driver"

  database1 {
    hostspec.master.jdbc_url = "jdbc:mysql://127.0.0.1:3306/database1"
    hostspec.slave.jdbc_url  = "jdbc:mysql://127.0.0.1:3306/database1"
  }
  
  database2 {
    hostspec.master.jdbc_url = "jdbc:mysql://127.0.0.1:3307/database2"
    hostspec.slave.jdbc_url  = "jdbc:mysql://127.0.0.1:3307/database2"
  }
}
```

HikariCPを構築するための設定値に関しては公式のREADMEを参照してください。[参照](https://github.com/brettwooldridge/HikariCP/blob/dev/README.md)

IxiaSのHikariConfigBuilderを使用して以下のようにHikariCPのDataSourceを構築します。

```scala
val dataSource = DataSourceName("ixias.db.mysql://slave/example")
val hikariConfigBuilder = HikariConfigBuilder.default(dataSource)
val hikariConfig        = hikariConfigBuilder.build()
hikariConfig.validate() // 未設定値をデフォルト値で補完する

val dataSource = new HikariDataSource(hikariConfig)
```

IxiaSにはHikariDataSourceからSlickのDatabaseを構築するビルダーも提供されているので、先ほど作成したHikariCPのDataSourceを使用して以下のようにSlickのDatabaseを構築します。

```scala
val database: Database = DatabaseBuilder.fromHikariDataSource(dataSource)
```

LambdaなどでHikariCPのコネクションプールを使用したくない場合は、`DatabaseBuilder`に`fromDataSource`というメソッドも提供されているため、任意のドライバーから作成したDataSourceを使用してSlickのDatabaseを構築することもできます。

```scala
val dataSource: DataSource = ???
val database: Database = DatabaseBuilder.fromDataSource(dataSource)
```

## Repository定義

NextbeatではSlickを使用したDBアクセスを行う際に、Repositoryというレイヤーを挟んで処理を行います。
NextbeatではDBアクセスを読み取り用と書き込み用で分けているため、それぞれのDatabaseインスタンスを作成します。

```scala
class UserRepository {
  ...
  val master: Database = DatabaseBuilder.fromHikariDataSource(masterDataSource)
  val slave: Database = DatabaseBuilder.fromHikariDataSource(slaveDataSource)
}
```

Slickを使用したDBアクセスを行う際には、`TableQuery`を使用してテーブルを定義する必要があるため、先ほど作成したUserTableを使用して以下のように構築を行います。

```scala
class UserRepository {
  ...
  val userTable = TableQuery[UserTable]
}
```

Repositoryのメソッドは基本的にはSlickのドキュメントに準じています。[参照](https://scala-slick.org/doc/3.3.3/queries.html)

```scala
class UserRepository {

  def getById(id: User.Id): Future[Ooption[User]] =
    slave.run(userTable.filter(_.id === id).result.headOption)
}
```

Entity Modelの章で「Idを持たないWithNoId型とはDBにデータを格納する前(Auto Incrementによって採番される前)のレコードを表現して、Idを持つEmbeddedId型とはDBにデータを格納した後(Auto Incrementによって採番された後)のレコードを表現している」という説明を行いました。

Entity Modelを使用してレコード作成を行う際には、WithNoId型のモデルを引数に受け取っています。
WithNoId型のモデルを使用している場合、idは常に`None`になっていますので、Slickの追加処理を行う際に`None`が渡されることで任意の値でレコード作成を行わずにAuto Incrementによって採番された値でレコード作成を行うことができます。

```scala
def add(data: User#WithNoId): Future[User.Id] =
  master.run(userTable returning userTable.map(_.id) += data)
```

先ほどの`getById`メソッドの戻り値は`Option[User]`を返すようにしていましたが、戻り値の型をEmbeddedId型に変更する場合は以下のように変換処理を行う必要があります。

```scala
def getById(id: User.Id): Future[Ooption[User#EmbeddedId]] =
  slave.run(userTable.filter(_.id === id).result.headOption).map(_.map(_.toEmbeddedId))
```

IxiaSにはこのような変換処理を暗黙的に行うtraitが用意されています。

`ixias.slick.SlickRepository`を使用することで、EmbeddedId型の戻り値が欲しいメソッドに対してEntity Modelが渡されている場合に暗黙的に型変換を行なってくれます。

```scala
import ixias.slick.SlickRepository
class UserRepository()(implicit val ex: ExecutionContext) extends SlickRepository[User.Id, User] {
  def getById(id: User.Id): Future[Ooption[EntityEmbeddedId]] =
    slave.run[Option[User]](userTable.filter(_.id === id).result.headOption)
}
```

※ SlickRepositoryには`EntityEmbeddedId`と`EntityWithNoId`の2つの型エイリアスが定義されています。

※ SlickRepositoryは内部でExecutionContextを必要としているため暗黙的に受け取れるように設定を追加する必要があります。

## Play Frameworkへの組み込み

以下サンプル実装では、以下のimportを想定しています。

```scala
import javax.inject.{ Inject, Provider, Singleton }
import scala.concurrent.Future
import com.google.inject.name.Names
import com.google.inject.AbstractModule
import play.api.inject.ApplicationLifecycle
import ixias.slick.model.DataSourceName
import ixias.slick.builder._
import ixias.slick.jdbc.MySQLProfile.api.Database
```

NextbeatではScalaを使用したプロダクトにはPlay Frameworkを使用しています。
IxiaSで構築を行なったリポジトリをPlay Frameworkに組み込むためのサンプルをご紹介します。

Play FrameworkにはデフォルトでGoogle guiceのDIが組み込まれています。IxiaSでのDB処理に関してもこのDIに乗っかる形で実装を行います。

まずはSlickのDatabaseをDIするためのモジュールを作成します。

Play Frameworkを使用したアプリケーションではHikariCPのコネクションプールを使用するため、Provider経由でPlay FrameworkのApplicationLifecycleを受け取り、アプリケーションの終了時にコネクションプールを閉じるよう作成を行います。

```scala
@Singleton
class MasterDatabaseProvider @Inject() (
  lifecycle: ApplicationLifecycle
) extends Provider[Database] {

  private val hikariConfigBuilder = HikariConfigBuilder.default(DataSourceName("ixias.db.mysql://master/example"))
  private val hikariConfig        = hikariConfigBuilder.build()
  hikariConfig.validate()

  private val dataSource = new HikariDataSource(hikariConfig)

  lifecycle.addStopHook { () =>
    Future.successful(dataSource.close())
  }

  override def get(): Database = DatabaseBuilder.fromHikariDataSource(dataSource)
}
```

※ 読み取り用のDatabaseも同様に作成します。

Play Frameworkにモジュールを組み込むためにAbstractModuleをMixInしたモジュールを作成します。

Nextbeatでは読み取り用と書き込み用で接続先のDBを変更しているため、それぞれのDatabaseインスタンスを作成します。
Databaseインスタンスはどちらも同じ型になっているため、DIする際に同じ型だと区別することができません。
そのため、`@Named`アノテーションを使用して名前付きのインスタンスとして組み込めるように`Names.named(...)`で任意の名前を付与しています。

```scala
class DatabaseModule extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[Database])
      .annotatedWith(Names.named("master"))
      .toProvider(classOf[MasterDatabaseProvider])
      .asEagerSingleton()
    bind(classOf[Database])
      .annotatedWith(Names.named("slave"))
      .toProvider(classOf[SlaveDatabaseProvider])
      .asEagerSingleton()
  }
}
```

Play Frameworkでは独自のモジュールを組み込むためには、Confファイルに以下のように有効化するための設定を追記します。

```txt
play.modules.enabled += "modules.DatabaseModule"
```

これでDatabaseの設定は完了です。

次はRepositoryの設定を行います。
RepositoryではDIする際に`@Named`アノテーションを使用して名前付きのインスタンスとして組み込めるように`Names.named(...)`で任意の名前を付与しているためその設定と同じ名前で組み込みます。

```scala
@Singleton
class UserRepository @Inject()(
  @Named("master") master: Database,
  @Named("slave") slave:   Database         
) {
  ...
}
```

Repositoryに関してはDatabseのようにモジュールを作成する必要はなく、そのままDIすることができます。

```scala
@Singleton
class Controller @Inject()(
  cc:             MessagesControllerComponents,
  taskRepository: TaskRepository
) extends MessagesAbstractController(cc) {
  ...
}
```
