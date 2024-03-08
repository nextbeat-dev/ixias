# AWS

この章では、IxiaSが提供するAWS SDKを使用して、AWSのサービスを操作する方法について説明します。

IxiaSが提供するAWS SDKは[aws-sdk-java](https://github.com/aws/aws-sdk-java)をラップしたものです。

現在IxiaSがラップしているAWS SDKは以下の通りです。

| サービス |      パッケージ      |
|:----:|:---------------:|
|  S3  | `ixias-aws-s3`  |
| SNS  | `ixias-aws-sns` |

現在のバージョンは **AWS SDK $awsSDKVersion$** に対応しています。

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
import ixias.slick.jdbc.MySQLProfile.api._
import ixias.slick.builder._
```

## AWS SNS

プロジェクトに以下の依存関係を設定する必要があります。

@@@ vars
```scala
libraryDependencies ++= Seq(
  "$org$" %% "ixias-aws-sns" % "$version$"
)
```
@@@
