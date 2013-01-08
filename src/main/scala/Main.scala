import collection.mutable

  import org.zeromq.ZMQ

object Helper {
  def time[A](a: => A) = {
    val now = System.nanoTime
    val result = a
    val micros = (System.nanoTime - now) / 1000
    println("%f seconds".format(micros/1000000.0))
    result
  }
}

object Cloud {

  import Helper._


  val tasks = List(0.2, 1.5, 1.8, 0.3, 0.8, 4.0, 0.1, 0.15, 0.25, 0.36, 0.5, 0.1, 0.61,0.761,0.712,0.83,0.82,0.176,0.56,0.71, 0.761,0.712,0.83,0.82,0.176,0.56,0.71, 0.15, 0.28, 0.85, 0.72, 0.72, 0.98, 1.2).sorted.reverse
  val deadline = 8.0


  var best = tasks.map(_.ceil).sum
  var bestSolution = (0 to (tasks.length)).toList
  var visited = 0

  var bestPossible = tasks.sum.ceil

  def cost(solution: List[Int]) =  (solution, tasks).zipped.groupBy(_._1).toList.map((p) => p._2.map(_._2).sum.ceil).sum

  def isLegal(solution: List[Int]):Boolean = (solution, tasks).zipped.groupBy(_._1).toList.map((p) => p._2.map(_._2).sum).forall(_ <= deadline)

  def publishBest(cost: Double, solution: List[Int]) {
    best = cost
    bestSolution = solution
    println("new best found: %d %f [%s]".format(visited, cost, solution.mkString(", ")))
  }

  val queue:mutable.MutableList[List[Int]] = mutable.MutableList()

  def branch(solution: List[Int], level: Int, newBest: (Double) => Unit):Unit = {
    visited += 1
    val result = cost(solution)
    if (isLegal(solution) && result < best && best > bestPossible) {
      if(solution.length == tasks.length) {

        if (result < best) {
          publishBest(result, solution)
          newBest(result)
        }
      } else {
//        queue ++= ((0 to (solution.max + 1)).toList).map(n => solution ::: List(n))

        if (level > 0) {
           // Minimalizuje liczbę wykorzystanych maszyn przy minimalnym koszcie
          (0 to (solution.max + 1)).reverse.foreach(n => branch(solution ::: List(n), level - 1, newBest))
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

}

object Main {

  def main(args: Array[String]) {

    val context = ZMQ.context(1)


    import Helper._

    val publisher = context.socket(ZMQ.PUSH)
    publisher.bind("tcp://*:5556")

    val reciever = context.socket(ZMQ.PULL)
    reciever.bind("tcp://*:5557")


    val bestThread = new Thread(){
     override def run() {

       val bestSub = context.socket(ZMQ.PULL)
       bestSub.bind("tcp://*:5558")
       val bestPub = context.socket(ZMQ.PUB)
       bestPub.bind("tcp://*:5559")

       while(true) {
         val msg = bestSub.recv(0)
         println("New best: " + new String(msg, 0, msg.length))
         bestPub.send(msg, 0)
       }

     }
    }
    bestThread.start()


    println("Waiting for " + args(0) + " workers")

    (0 until args(0).toInt).foreach((i) => {
        reciever.recv(0)
        println(i.toString + " is up")
    })

    time {
      Cloud.branch(List(0), 6, (nb) => null)
      println(Cloud.queue.length)
      Cloud.queue.foreach( (task) => {

//        println("Publishing " + task.mkString(","))
        publisher.send(task.mkString(",").getBytes, 0)
//        println("Task++")
//        time {
//          branch(task, tasks.length)
//        }
      })

      Cloud.queue.foreach( (task) => {

        val rmsg = reciever.recv(0)
        val msg = new String(rmsg, 0, rmsg.length - 1)
//        println("Task finished")
      })
    }
    (0 until args(0).toInt).foreach( (_) => publisher.send("".getBytes, 0))


    println("best found: %f\n%s".format(Cloud.best, (Cloud.tasks, Cloud.bestSolution).zipped.groupBy(_._2).mkString("\n")))
    println("\nvisited: %d".format(Cloud.visited))

  }

}


object Worker {
  def main(args: Array[String]) {

    val context = ZMQ.context(1)


    import Helper._

    val publisher = context.socket(ZMQ.PUSH)
    publisher.connect("tcp://localhost:5557")


    val bestPublisher = context.socket(ZMQ.PUSH)
    bestPublisher.connect("tcp://localhost:5558")


    val reciever = context.socket(ZMQ.PULL)
    reciever.connect("tcp://localhost:5556")


    val bestThread = new Thread(){
      override def run() {

        val bestSub = context.socket(ZMQ.SUB)
        bestSub.connect("tcp://localhost:5559")
        bestSub.subscribe("".getBytes)

        println("Waiting for new best")
        while(true) {
          val rmsg = bestSub.recv(0)
          val msg = new String(rmsg, 0, rmsg.length)
          println("Recieved new best: " + msg )
          Cloud.best = msg.toDouble
        }

      }
    }
    bestThread.start()


    publisher.send("-1".getBytes, 0)


    println("Worker ready")
    while(true) {
      val rmsg = reciever.recv(0)

      if(rmsg.length == 0) {
        println("Done all, exiting")
        return
      }
      val msg = new String(rmsg, 0, rmsg.length )

      Cloud.branch(msg.split(",").toList.map(_.toInt), Cloud.tasks.length, (best) => {
        bestPublisher.send(best.toString.getBytes, 0)
      })
      publisher.send(Cloud.best.toString.getBytes, 0)
      println("Task done")
    }


  }

}



object Sequential {

  import Helper._
  def main(args: Array[String]) {
    time {
      Cloud.branch(List(0), 6, (nb) => null)
      Cloud.queue.foreach( (task) => Cloud.branch(task, Cloud.tasks.length, (_) => null))
    }
    println("best found: %f\n%s".format(Cloud.best, (Cloud.tasks, Cloud.bestSolution).zipped.groupBy(_._2).mkString("\n")))
    println("\nvisited: %d".format(Cloud.visited))

  }

}