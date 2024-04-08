# AWS

この章では、IxiaSが提供するAWS SDKを使用して、AWSのサービスを操作する方法について説明します。

IxiaSが提供するAWS SDKは[aws-sdk-java](https://github.com/aws/aws-sdk-java)をラップしたものです。

現在IxiaSがラップしているAWS SDKは以下の通りです。

| サービス |      パッケージ      |
|:----:|:---------------:|
|  S3  | `ixias-aws-s3`  |
| SNS  | `ixias-aws-sns` |
| SES  | `ixias-aws-ses` |

現在のバージョンは **AWS SDK $awsSDKVersion$** に対応しています。

IxiaSが提供するAWS SDKでは設定をConfig経由で設定します。

共通で設定できる値は以下の通りです。

| キー                | 型      | 必須 | 備考                        |
|-------------------|--------|----|---------------------------|
| access_key_id     | String | ❌  | AWSクレデンシャル情報              |
| secret_access_key | String | ❌  | AWSクレデンシャル情報              |
| region            | String | ✅  | リージョン情報                   |
| endpoint          | String | ❌  | 送信先エンドポイント (主にLocalStack) |

AWS SDKではデフォルトでIAMロールの権限を使用して操作を行いますが、クレデンシャル情報を設定することでIAMロールを使用せずに操作を行うことができます。
IAMユーザーを使用して操作を行う場合は、`access_key_id`と`secret_access_key`を両方設定する必要があります。

※ IAMユーザーの鍵情報を使用して操作を行うことは非推奨です。なるべき実行サービスのIAMロールに権限を付与して操作を行うことを推奨します。

AWS SKDはConfigからの設定取得を`DataSourceName`というクラスを使用して行います。

`DataSourceName`は以下のように設定します。

```scala
val dsn = DataSourceName("{path}://{resource}/{name}")
```

`DataSourceName`へ渡す引数の文字列はConfigから読み取る値のパスを指定します。

現状は2種類の正規表現に合致する文字列を指定する必要があります。

- `"""^([.\w]+)://(\w+?)$""".r`
- `"""^([.\w]+)://(\w+?)/(\w+)$$""".r`

それぞれの正規表現に合致する文字列は以下の通りです。

- `{path}`は設定ファイルのパスを指定します。
- `{resource}`は設定ファイルのリソース名を指定します。
- `{name}`は設定ファイル内の設定名を指定します。

例えば、以下のS3用の設定ファイルがある場合

```txt
aws.s3 {
  access_key_id     = "dummy"
  secret_access_key = "dummy"
  region            = "ap-northeast-1"
  endpoint          = "http://localhost:4566"

  dummy_bucket {
    bucket_name = "dummy"
  }
}
```

`DataSourceName`は以下のように設定します。

```scala
val dsn = DataSourceName("aws.s3://dummy_bucket")
```

`DataSourceName`の`path`は`.`繋ぎで設定することができ、最初の値で設定を共有することができます。
例えば`aws`のプレフィクスを付けて設定を行なっている場合、以下の設定は全てのAWS SDKで共通の設定として認識されます。

```txt
aws {
  access_key_id     = "dummy"
  secret_access_key = "dummy"
  region            = "ap-northeast-1"
  endpoint          = "http://localhost:4566"
}
```

サービスごとに個別の設定を行う場合は、最初の例と同じように`aws.{サービス名}`のようにサービス名をキーとして設定します。
例として、S3の設定を行った場合以下の設定は`aws.s3`内でのみ使用される設定となります。

```txt
aws.s3 {
  access_key_id     = "dummy"
  secret_access_key = "dummy"
  region            = "ap-northeast-1"
  endpoint          = "http://localhost:4566"
}
```

## AWS S3

プロジェクトに以下の依存関係を設定する必要があります。

@@@ vars
```scala
libraryDependencies ++= Seq(
  "$org$" %% "ixias-aws-s3" % "$version$"
)
```
@@@

以降のコード例では、以下のimportを想定しています。

```scala
import ixias.aws.s3._
```

S3のConfigで設定できる値は以下の通りです。

| キー                        | 型       | 必須 | 備考                               |
|---------------------------|---------|----|----------------------------------|
| bucket_name               | String  | ✅  | 操作を行うバケット名                       |
| path_style_access_enabled | Boolean | ❌  | すべてのリクエストにパス形式のアクセスを使うようにするための設定 |

S3への操作を行うためには、`AmazonS3Client`を使用します。

```scala
val s3Client = AmazonS3Client("aws.s3://bucket") // or AmazonS3Client(DataSourceName("aws.s3://bucket"))
```

この`AmazonS3Client`を使用してS3の操作を行います。
現在のバージョンでは以下の操作をサポートしています。

| 操作            | 関数                     | 備考 |
|---------------|------------------------|----|
| オブジェクトの取得     | `load`                 |    |
| オブジェクトのアップロード | `upload`               |    |
| オブジェクトの削除     | `remove`               |    |
| オブジェクトの一括削除   | `bulkRemove`           |    |
| 署名付きURLの取得    | `generatePreSignedUrl` |    |

サポートされている操作以外の操作を行う場合は、`AmazonS3Client`の`action`メンバを使用して操作を行います。

```scala
val result = s3Client.action { client =>
  // ここに操作を記述
}
```

actionメンバに渡される関数の引数は`AmazonS3`クラスのインスタンスです。
そのため、公式AWS SDKの`AmazonS3`クラスでサポートされている操作を行うことができます。

サポートされている操作の詳細については、[公式AWS SDKのAmazonS3クラスのドキュメント](https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/s3/AmazonS3.html)を参照してください。

## AWS SNS

プロジェクトに以下の依存関係を設定する必要があります。

@@@ vars
```scala
libraryDependencies ++= Seq(
  "$org$" %% "ixias-aws-sns" % "$version$"
)
```
@@@

SNSのConfigで設定できる値は以下の通りです。

| キー    | 型       | 必須 | 備考                   |
|-------|---------|----|----------------------|
| topic | String  | ✅  | SNSトピック名             |
| skip  | Boolean | ❌  | SNSプッシュ処理をスキップするかの設定 |

SNSへの操作を行うためには、`AmazonSNSClient`を使用します。

```scala
val snsClient = AmazonSNSClient("aws://sns/test_topic") // or AmazonS3Client(DataSourceName("aws://sns/test_topic"))
```

この`AmazonSNSClient`を使用してSNSの操作を行います。
現在のバージョンでは以下の操作をサポートしています。

| 操作   | 関数        | 備考 |
|------|-----------|----|
| 送信処理 | `publish` |    |

`publish`関数ではサブスクリプションフィルターを使用することもできます。

```scala
val client = AmazonSNSClient("aws.sns://topic")
val messageAttributeValue = new MessageAttributeValue()
messageAttributeValue.setDataType("Number")
messageAttributeValue.setStringValue("1")

client.publish("Test", Map("filterType" -> messageAttributeValue))
```

## AWS SES

プロジェクトに以下の依存関係を設定する必要があります。

@@@ vars
```scala
libraryDependencies ++= Seq(
  "$org$" %% "ixias-aws-ses" % "$version$"
)
```
@@@

SESへの操作を行うためには、`AmazonSESClient`を使用します。

```scala
val sesClient = AmazonSESClient("aws.ses://dummy") // or AmazonSESClient(DataSourceName("aws.ses://dummy"))
```

この`AmazonSESClient`を使用してSESの操作を行います。
現在のバージョンでは以下の操作をサポートしています。

| 操作                          | 関数                   | 備考                                |
|-----------------------------|----------------------|-----------------------------------|
| 電子メール・メッセージ送信               | `sendEmail`          |                                   |
| Eメール・テンプレートを使ってEメール・メッセージ送信 | `sendTemplatedEmail` |                                   |
| バウンスメッセージ送信                 | `sendBounce`         | この操作は、メールを受信してから24時間後までしか使用できません。 |

`sendEmail`関数では、以下のようにメールを送信することができます。

```scala
val body =
  new Body().withText(new Content().withCharset("UTF-8").withData("This email was sent through Amazon SES"))
val subject = new Content().withCharset("UTF-8").withData("Amazon SES test (AWS SDK for Java)")
val message = new Message().withBody(body).withSubject(subject)
val request = new SendEmailRequest()
  .withDestination(new Destination().withToAddresses("takahiko.tominaga@nextbeat.net"))
  .withMessage(message)
  .withSource("takahiko.tominaga@nextbeat.net")

sesClient.sendEmail(request)
```

サポートされている操作以外の操作を行う場合は、`AmazonSESClient`の`action`メンバを使用して操作を行います。

```scala
val result = sesClient.action { client =>
  // ここに操作を記述
}
```

actionメンバに渡される関数の引数は`AmazonSimpleEmailService`クラスのインスタンスです。
そのため、公式AWS SDKの`AmazonSimpleEmailService`クラスでサポートされている操作を行うことができます。

サポートされている操作の詳細については、[公式AWS SDKのAmazonSimpleEmailServiceクラスのドキュメント](https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/simpleemail/AmazonSimpleEmailService.html)を参照してください。
