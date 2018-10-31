package io.hemin.engine.util

import java.util.concurrent.{Callable, CancellationException, ExecutorService, TimeUnit, Future => JavaFuture}
import java.{util => jutil}

import scala.collection.JavaConverters._
import scala.concurrent.duration.{TimeUnit, _}
import scala.concurrent.{Await, ExecutionContext, Future, Promise}
import scala.util.{Failure, Success}
import scala.util.control.NonFatal


class ExecutorServiceWrapper(implicit ec: ExecutionContext) extends ExecutorService {

  def execute(command: Runnable): Unit = {
    ec.execute(() => {
      try command.run() catch {
        case NonFatal(ex) => ec.reportFailure(ex)
      }
    })
  }

  def isTerminated: Boolean = false
  def isShutdown: Boolean = false

  def shutdown(): Unit = {
    throw new UnsupportedOperationException("ExecutorServiceWrapper.shutdown")
  }

  def awaitTermination(timeout: Long, unit: TimeUnit): Boolean =
    throw new UnsupportedOperationException("ExecutorServiceWrapper.awaitTermination")

  def shutdownNow(): java.util.List[Runnable] =
    throw new UnsupportedOperationException("ExecutorServiceWrapper.shutdownNow")

  def submit(task: Runnable): JavaFuture[_] =
    wrapPromiseInJavaFuture(executeWithPromise {
      task.run()
    })

  def submit[T](task: Runnable, result: T): JavaFuture[T] =
    wrapPromiseInJavaFuture(executeWithPromise {
      task.run(); result
    })

  def submit[T](task: Callable[T]): JavaFuture[T] =
    wrapPromiseInJavaFuture(executeWithPromise {
      task.call()
    })

  def invokeAll[T](tasks: jutil.Collection[_ <: Callable[T]], timeout: Long, unit: TimeUnit): jutil.List[JavaFuture[T]] =
    invokeAll(tasks, durationFor(timeout, unit))

  def invokeAll[T](tasks: java.util.Collection[_ <: Callable[T]]): jutil.List[JavaFuture[T]] =
    invokeAll(tasks, Duration.Inf)

  def invokeAny[T](tasks: jutil.Collection[_ <: Callable[T]], timeout: Long, unit: TimeUnit): T =
    invokeAny(tasks, durationFor(timeout, unit))

  def invokeAny[T](tasks: java.util.Collection[_ <: Callable[T]]): T =
    invokeAny(tasks, Duration.Inf)

  private[this] sealed trait TaskState[+T]
  private[this] case object Cancelled extends TaskState[Nothing]
  private[this] case class  Finished[+T](result: T) extends TaskState[T]

  private[this] def executeWithPromise[T](callback: => T): Promise[TaskState[T]] = {
    val promise = Promise[TaskState[T]]()

    ec.execute(() => {
      if (!promise.isCompleted)
        try {
          val result = callback
          promise.tryComplete(Success(Finished(result)))
        }
        catch {
          case NonFatal(ex) =>
            promise.tryComplete(Failure(ex))
            ec.reportFailure(ex)
        }
    })

    promise
  }

  private[this] def wrapPromiseInJavaFuture[T](promise: Promise[TaskState[T]]): JavaFuture[T] = {
    val future = promise.future

    new JavaFuture[T] {
      def isCancelled: Boolean =
        future.isCompleted && future.value.get == Success(Cancelled)

      def get(timeout: Long, unit: TimeUnit): T =
        Await.result(future, durationFor(timeout, unit)) match {
          case Finished(result) =>
            result
          case Cancelled =>
            throw new CancellationException()
        }

      def get(): T =
        Await.result(future, Duration.Inf) match {
          case Finished(result) =>
            result
          case Cancelled =>
            throw new CancellationException()
        }

      def cancel(mayInterruptIfRunning: Boolean): Boolean =
        promise.tryComplete(Success(Cancelled))

      def isDone: Boolean =
        promise.isCompleted
    }
  }

  private[this] def invokeAny[T](tasks: java.util.Collection[_ <: Callable[T]], atMost: Duration): T = {
    if (tasks.size() == 0)
      throw new IllegalArgumentException("tasks is empty")

    val promises = tasks.asScala.map(task => executeWithPromise {
      task.call()
    })

    val firstCompleted = Future.firstCompletedOf(promises.map(_.future))

    Await.result(firstCompleted, atMost) match {
      case Cancelled =>
        throw new CancellationException()
      case Finished(r) => r
    }
  }

  private[this] def invokeAll[T](tasks: java.util.Collection[_ <: Callable[T]], atMost: Duration): java.util.List[java.util.concurrent.Future[T]] = {
    if (tasks.size() == 0)
      throw new IllegalArgumentException("tasks is empty")

    val promises = tasks.asScala.map(task => executeWithPromise {
      task.call()
    })
    val futures = promises.map(_.future)
    val sequence = Future.sequence(futures)

    Await.ready(sequence, atMost)
    promises.map(wrapPromiseInJavaFuture).toList.asJava
  }

  private[this] def durationFor(timeout: Long, unit: TimeUnit): FiniteDuration = unit match {
    case TimeUnit.NANOSECONDS => timeout.nanos
    case TimeUnit.MICROSECONDS => timeout.micros
    case _ => unit.toMillis(timeout).millis
  }
}
