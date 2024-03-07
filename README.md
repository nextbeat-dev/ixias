# IxiaS

IxiaSとはNextbeat旧CTOの衣笠が開発したOSSであり、社内のScalaプロダクト共通で用いられているScalaライブラリです。

本リポジトリは[ixias-net/ixias](https://github.com/ixias-net/ixias)からのフォークプロジェクトです。

大元のプロジェクトから大きく変更が加えられており互換性はありません。

> [!CAUTION]
> バージョン `v2.1.1`で使用しているAWS SDKのバージョンが大幅に更新されました
> 主にS3の使用方法に注意が必要です。
> 以前のバージョンではオブジェクトのアップロードやダウンロードを行う際にバケット指定でオブジェクトも指定可能でした。
> ```scala
> client.getObject(new GetObjectRequest("bucket/key", "key")))
> ```
> しかし、バージョン `v2.1.1`以降ではこれがエラーとなり、バケット名に`/`を使用してオブジェクトを指定することができなくなりました。
> ```scala
> client.getObject(new GetObjectRequest("bucket", "key/key")))
> ```
> そのため`v2.1.1`以前を使用しているユーザーで`v2.1.1`以上にバージョンを更新する際は注意してください。

## ドキュメント

[こちら](https://nextbeat-dev.github.io/ixias/index.html)を参照してください
