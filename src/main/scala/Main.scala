import util.Random

object Main {
  def time[A](a: => A) = {
   val now = System.nanoTime
   val result = a
   val micros = (System.nanoTime - now) / 1000
   println("%f seconds".format(micros/1000000.0))
   result
 }


  val tasks = List(0.2, 1.5, 1.8, 0.3, 0.8, 4.0, 0.1)//, 0.15, 0.25, 0.36, 0.73)
  val deadline = 4.5

  var best = tasks.map(_.ceil).sum.toDouble
  var bestSolution = (0 to (tasks.length)).toList

  def cost(solution: List[Int]) =  (solution, tasks).zipped.groupBy(_._1).toList.map((p) => p._2.map(_._2).sum.ceil).sum

  def isLegal(solution: List[Int]):Boolean = (solution, tasks).zipped.groupBy(_._1).toList.map((p) => p._2.map(_._2).sum).forall(_ <= deadline)

  def branch(solution: List[Int]):Unit = {
    val result = cost(solution)
    if (isLegal(solution) && result < best) {
      if(solution.length == tasks.length) {

        if (result < best) {
          best = result
          bestSolution = solution
          println("new best found: %f [%s]".format(result, solution.mkString(", ")))
        }
      } else {
        ((0 to (tasks.length)).toList).foreach(n => branch(solution ::: List(n)))
      }
      //println("solution: %s".format(solution.mkString(", ")))
    } else {
     // println("making cut at %f (best: %f) %s".format(cost(solution), best, solution.length))
    }

  }


  def main(args: Array[String]) {

    time(branch(List()))
    println("best found: %f [%s]".format(best, bestSolution.mkString(", ")))

  }

}
