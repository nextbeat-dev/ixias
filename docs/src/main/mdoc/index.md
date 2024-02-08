@@@ index
 * [Entity Model](01-Entity-Model.md)
 * [Enum](02-Enum.md)
 * [Slick](03-Slick.md)
 * [Play Framework](./04-Play-Framework.md)
@@@

# IxiaS

IxiaSとはNextbeat旧CTOの衣笠が開発したOSSであり、社内のScalaプロダクト共通で用いられているScalaライブラリです。

[IxiaSのOSS化に関するインタビュー記事](https://medium.com/nextbeat-engineering/%E9%96%8B%E7%99%BA%E8%80%85%E3%81%AFcto%E8%A1%A3%E7%AC%A0-%E3%83%97%E3%83%AD%E3%83%80%E3%82%AF%E3%83%88%E5%85%B1%E9%80%9A%E3%81%AEscala%E3%81%AE%E3%82%B3%E3%82%A2%E6%8A%80%E8%A1%93-ixias-%E3%82%92oss%E5%8C%96-3eb5c4ed66bf)も公開しているので参考にしてください。

IxiaSはScalaのWebフレームワークである[Play Framework](https://github.com/playframework/playframework)とDBレイヤーである[Slick](https://github.com/slick/slick)、インフラレイヤーであるAWS SDKをラップしたライブラリです。

IxiaSは以下のような特徴を持っています。

- Entity Modelと呼ばれる、DBのテーブルと1対1で対応するデータ型を提供する
- Enum等の汎用的なデータ型を提供する
- Play Frameworkの機能を拡張し、より簡単に開発できるようにする
- SlickをEntity Modelと組み合わせて簡単に構築できる
- AWS SDKを使用したインフラ構築を簡単にする

Slick等のIxiaSがラップしているライブラリを使用する際もプロダクト間で共通したコードを書くことができるので、コード共有及びプロダクト間の人材ローテーションが容易になり開発効率が向上します。

## クイックスタート

IxiaSはMaven Centralには公開されておらず、AWSのS3に公開されているライブラリです。
使用する際はResolverの設定を追加して、S3からダウンロードできるようにする必要があります。

```scala
resolvers ++= Seq(
  "Nextbeat Snapshots" at "https://s3-ap-northeast-1.amazonaws.com/maven.nextbeat.net/snapshots",
  "Nextbeat Releases"  at "https://s3-ap-northeast-1.amazonaws.com/maven.nextbeat.net/releases",
)
```

現在のバージョンは **Scala $scalaVersion$** に対応した **$version$** です。

@@@ vars
```scala
libraryDependencies ++= Seq(

  // まずはこの1つから
  "$org$" %% "ixias" % "$version$",

  // そして、必要に応じてこれらを加える
  "$org$" %% "ixias-slick" % "$version$", // Slickを使用したDBアクセス
  "$org$" %% "ixias-aws"   % "$version$", // AWS SDKのラッパー
)
```
@@@
