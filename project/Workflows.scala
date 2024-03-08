import sbtghactions._

object Workflows {
  val dockerRun: WorkflowStep.Run = WorkflowStep.Run(
    commands = List("docker compose up -d"),
    name     = Some("Start up LocalStack on Docker")
  )

  val dockerStop: WorkflowStep.Run = WorkflowStep.Run(
    commands = List("docker compose down"),
    name     = Some("Stop LocalStack on Docker")
  )
}
