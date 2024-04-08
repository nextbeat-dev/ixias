# Image Resource

この章では、IxiaSが提供する画像管理を使用して、AWS S3への画像操作とその画像をアプリケーションで管理する方法について説明します。

プロジェクトに以下の依存関係を設定する必要があります。

@@@ vars
```scala
libraryDependencies ++= Seq(
  "$org$" %% "ixias-aws-s3-lib" % "$version$"
)
```
@@@

以降のコード例では、以下のimportを想定しています。

```scala
import ixias.aws.s3.model._
import ixias.aws.s3.AmazonS3Repository
```

AmazonS3Repositoryは、AWS S3とのインタラクションを管理するためのScalaトレイトです。このトレイトは、ファイルのアップロード、ダウンロード、削除など、AWS S3との主要な操作を抽象化します。
以下に、AmazonS3Repositoryの主要なメソッドとその使用方法を示します。 

AmazonS3Repositoryのインスタンスを作成する必要があります。これは通常、アプリケーションの初期化時に行います。

```scala
val s3Repo = new AmazonS3Repository {
  def master: Database = ???
  def slave:  Database = ???
}
```

ここで、masterとslaveはデータベース接続を表します。これらはアプリケーションの設定から適切に取得する必要があります。

## ファイルの取得

AmazonS3Repositoryの`get(id: File.Id)`メソッドを使用して、指定したIDのファイルを取得します。以下にその使用例を示します。

```scala
val fileId = File.Id(1L)
val fileOptFuture = s3Repo.get(fileId)
```

このコードは、指定したIDのファイルを非同期に取得します。結果は`Future[Option[EntityEmbeddedId]]`型で返され、ファイルが見つかった場合はそのファイルを含むOption、見つからなかった場合はNoneを含むFutureを返します。

### ファイルの一覧取得

AmazonS3Repositoryの`filter`メソッドを使用して、ファイルの一覧を取得します。以下にその使用例を示します。

```scala
val ids = Seq(File.Id(1L), File.Id(2L), File.Id(3L))
val filesFuture = s3Repo.filter(ids)
```

このコードは、指定したIDのファイルを非同期に取得します。結果は`Future[Seq[EntityEmbeddedId]]`型で返され、ファイルが見つかった場合はそのファイルを含むSeq、見つからなかった場合は空のSeqを含むFutureを返します。

### 署名付きURLを取得

AmazonS3Repositoryの`getWithPresigned`メソッドを使用して、指定したIDのファイルにアクセスするための署名付きURLを取得します。以下にその使用例を示します。

```scala
val fileId = File.Id(1L)
val urlFuture = s3Repo.getWithPresigned(fileId)
```

このコードは、指定したIDのファイルにアクセスするための署名付きURLを非同期に取得します。結果は`Future[Option[String]]`型で返され、URLが見つかった場合はそのURLを含むOption、見つからなかった場合はNoneを含むFutureを返します。

## ファイルの作成

AmazonS3Repositoryの`add(file: File#WithNoId, content: java.io.File)`メソッドを使用して、新しいファイルを作成します。以下にその使用例を示します。

```scala
val file = new File(...).toWithNoId
val content = new java.io.File("path/to/your/file")
val fileIdFuture = s3Repo.add(file, content)
```

このコードは、指定したファイル情報と内容で新しいファイルを非同期に作成します。結果は`Future[File.Id]`型で返され、作成したファイルのIDを含むFutureを返します。

### データベース内にのみファイル情報を作成

AWS S3上にファイルをアップロードせず、データベース内にのみファイル情報を作成したい場合は、`addViaPresignedUrl`メソッドを使用します。

このメソッドはデータベース内にのみファイル情報を作成し、AWS S3上にファイルをアップロードするための署名付きURLを返します。
AWS S3上へはこのメソッドを呼び出した後、指定されたURL経由でファイルをアップロードします。

```scala
val file = new File(...).toWithNoId
val presignedUrlFuture: Future[(File.Id, String)] = s3Repo.addViaPresignedUrl(file)
```

## ファイルの更新

AmazonS3Repositoryの`update(file: EntityEmbeddedId, content: java.io.File)`メソッドを使用して、既存のファイルを更新します。以下にその使用例を示します。

```scala
val file = new File(...).toEmbeddedId
val content = new java.io.File("path/to/your/file")
val updatedFileOptFuture = s3Repo.update(file, content)
```

このコードは、指定したファイル情報と内容で既存のファイルを非同期に更新します。結果は`Future[Option[EntityEmbeddedId]]`型で返され、更新したファイルを含むOption、見つからなかった場合はNoneを含むFutureを返します。

### データベース内にのみファイル情報を更新

AWS S3上のファイルを更新せず、データベース内にのみファイル情報を更新したい場合は、`updateViaPresignedUrl`メソッドを使用します。

このメソッドはデータベース内にのみファイル情報を更新し、AWS S3上にファイルをアップロードするための署名付きURLを返します。
AWS S3上へはこのメソッドを呼び出した後、指定されたURL経由でファイルをアップロードします。

```scala
val file = new File(...).toEmbeddedId
val presignedUrlFuture: Future[(Option[EntityEmbeddedId], String)] = s3Repo.updateViaPresignedUrl(file)
```

## ファイルの削除

AmazonS3Repositoryの`erase(id: File.Id)`メソッドを使用して、既存のファイルを削除します。以下にその使用例を示します。

```scala
val fileId = File.Id(1L)
val erasedFileOptFuture = s3Repo.erase(fileId)
```

このコードは、指定したIDのファイルを非同期に削除します。結果は`Future[Option[EntityEmbeddedId]]`型で返され、削除したファイルを含むOption、見つからなかった場合はNoneを含むFutureを返します。

`erase`メソッドはデータベース内のファイル情報を削除しつつ、AWS S3上のファイルも削除します。

データベース内のファイル情報のみを削除したい場合は、`remove`メソッドを使用します。

```scala
val fileId = File.Id(1L)
val removedFileOptFuture = s3Repo.remove(fileId)
```

### 一括削除

AmazonS3Repositoryの`bulkRemove`メソッド、`bulkErase`メソッドを使用して、複数のファイルを一括で削除します。以下にその使用例を示します。

`bulkRemove`はデータベース内のファイル情報のみを削除し、AWS S3上のファイルは削除しません。

```scala
val fileIds = Seq(File.Id(1L), File.Id(2L), File.Id(3L))
val removedFilesOptFuture = s3Repo.bulkRemove(fileIds)
```

`bulkErase`はデータベース内のファイル情報とAWS S3上のファイルを削除します。

```scala
val fileIds = Seq(File.Id(1L), File.Id(2L), File.Id(3L))
val erasedFilesOptFuture = s3Repo.bulkErase(fileIds)
```
