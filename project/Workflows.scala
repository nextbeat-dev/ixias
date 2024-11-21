import scala.concurrent.duration._

import sbtghactions._

object Workflows {

  val settingsSns: WorkflowStep.Run = WorkflowStep.Run(
    commands = List(
      "aws sns create-topic --name testSNS --endpoint-url http://localhost:4566 --region ap-northeast-1",
      "aws sns subscribe --topic-arn arn:aws:sns:ap-northeast-1:000000000000:testSNS --protocol lambda --notification-endpoint arn:aws:lambda:ap-northeast-1:000000000000:function:testFunction --endpoint-url http://localhost:4566 --region ap-northeast-1"
    ),
    env =
      Map("AWS_ACCESS_KEY_ID" -> "dummy", "AWS_SECRET_ACCESS_KEY" -> "dummy", "AWS_DEFAULT_REGION" -> "ap-northeast-1"),
    name = Some("Create SNS topic")
  )

  val settingsS3: WorkflowStep.Run = WorkflowStep.Run(
    commands = List(
      "aws s3api create-bucket --bucket \"dummy\" --create-bucket-configuration \"LocationConstraint=ap-northeast-1\" --endpoint-url \"http://localhost:4566\" --region ap-northeast-1"
    ),
    env =
      Map("AWS_ACCESS_KEY_ID" -> "dummy", "AWS_SECRET_ACCESS_KEY" -> "dummy", "AWS_DEFAULT_REGION" -> "ap-northeast-1"),
    name = Some("Create S3 object")
  )

  val settingsSES: WorkflowStep.Run = WorkflowStep.Run(
    commands = List(
      "aws ses verify-email-identity --email-address \"takahiko.tominaga@nextbeat.net\" --endpoint-url=http://localhost:4566"
    ),
    env =
      Map("AWS_ACCESS_KEY_ID" -> "dummy", "AWS_SECRET_ACCESS_KEY" -> "dummy", "AWS_DEFAULT_REGION" -> "ap-northeast-1"),
    name = Some("Create SES object")
  )

  val waitForLocalStackContainerStart: WorkflowStep.Run = WorkflowStep.Run(
    timeout = Some(1.minute),
    commands = List(
      "set -x",
      "until [ \"$(docker inspect --format='{{.State.Health.Status}}' localstack_ixias)\" = 'healthy' ]; do",
      "  sleep 10s",
      "done"
    ),
    name = Some("Wait for LocalStack to start")
  )

  val waitForMySQLContainerStart: WorkflowStep.Run = WorkflowStep.Run(
    timeout = Some(1.minute),
    commands = List(
      "set -x",
      "until [ \"$(docker inspect --format='{{.State.Health.Status}}' mysql_ixias)\" = 'healthy' ]; do",
      "  sleep 10s",
      "done"
    ),
    name = Some("Wait for MySQL to start")
  )

  val dockerRun: WorkflowStep.Run = WorkflowStep.Run(
    commands = List("docker compose up -d"),
    name     = Some("Start up LocalStack and MySQL on Docker")
  )

  val dockerStop: WorkflowStep.Run = WorkflowStep.Run(
    commands = List("docker compose down"),
    name     = Some("Stop LocalStack and MySQL on Docker")
  )
}
