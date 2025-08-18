package service.bot

class MinimaxResult(
    val path: Path,
    val evaluation: Evaluation,
    val maxDepthReached: Boolean
)