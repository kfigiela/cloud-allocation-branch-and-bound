import collection.mutable


object Main {
  def time[A](a: => A) = {
   val now = System.nanoTime
   val result = a
   val micros = (System.nanoTime - now) / 1000
   println("%f seconds".format(micros/1000000.0))
   result
 }


  val tasks = List(0.2, 1.5, 1.8, 0.3, 0.8, 4.0, 0.1, 0.15, 0.25, 0.36, 0.5, 0.1, 0.61,0.761,0.712,0.83,0.82,0.176,0.56,0.71, 0.761,0.712,0.83,0.82,0.176,0.56,0.71).sorted.reverse
  val deadline = 4.5

  var best = tasks.map(_.ceil).sum
  var bestSolution = (0 to (tasks.length)).toList
  var visited = 0

  def cost(solution: List[Int]) =  (solution, tasks).zipped.groupBy(_._1).toList.map((p) => p._2.map(_._2).sum.ceil).sum

  def isLegal(solution: List[Int]):Boolean = (solution, tasks).zipped.groupBy(_._1).toList.map((p) => p._2.map(_._2).sum).forall(_ <= deadline)

  def publishBest(cost: Double, solution: List[Int]) {
    best = cost
    bestSolution = solution
    println("new best found: %d %f [%s]".format(visited, cost, solution.mkString(", ")))
  }

  val queue:mutable.MutableList[List[Int]] = mutable.MutableList()

  def branch(solution: List[Int], level: Int):Unit = {
    visited += 1
    val result = cost(solution)
    if (isLegal(solution) && result < best) {
      if(solution.length == tasks.length) {

        if (result < best) {
          publishBest(result, solution)
        }
      } else {
//        queue ++= ((0 to (solution.max + 1)).toList).map(n => solution ::: List(n))

        if (level > 0) {
           // Minimalizuje liczbę wykorzystanych maszyn przy minimalnym koszcie
          (0 to (solution.max + 1)).reverse.foreach(n => branch(solution ::: List(n), level - 1))
        } else {
          queue ++= (0 to (solution.max + 1)).reverse.map(n => solution ::: List(n))
          println("queued task")
        }
        // Maksymalizuje liczbę wykorzystanych maszyn przy minimalnym koszcie minimalizując czas wykonania
//        (0 to (solution.max + 1)).foreach(n => branch(solution ::: List(n)))
      }
      //println("solution: %s".format(solution.mkString(", ")))
    } else {
//     println("[%d] making cut at %f (best: %f) %s".format(visited, cost(solution), best, solution.length))
    }

  }


  def main(args: Array[String]) {


    time {
      branch(List(0), 6)
      println(queue.length)
      queue.par.foreach( (task) => {
//        println("Task++")
        time {
          branch(task, tasks.length)
        }
      })
    }



    println("best found: %f\n%s".format(best, (tasks, bestSolution).zipped.groupBy(_._2).mkString("\n")))
    println("\nvisited: %d".format(visited))
  }

}
