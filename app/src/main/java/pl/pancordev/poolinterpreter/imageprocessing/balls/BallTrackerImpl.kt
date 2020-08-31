package pl.pancordev.poolinterpreter.imageprocessing.balls

import timber.log.Timber
import java.util.*
import kotlin.collections.HashMap

class BallTrackerImpl {

    private val maxLostFrames = 3000
    private val trackedBalls: HashMap<Int, Ball> = HashMap()
    private val disappearedBallsCounter: HashMap<Int, Int> = HashMap()

    private var nextId = 0

    fun update(circles: List<Circle>): List<Ball> {
        val untrackedCircles = mutableListOf(*circles.toTypedArray())
        Timber.d("-----------------Update started-----------------")
        if (untrackedCircles.isEmpty()) {
            Timber.d("Provided untracked ball list is empty")
            for(disappearedBall in disappearedBallsCounter) {
                disappearedBall.setValue(disappearedBall.value + 1)
            }
            return trackedBalls.values.toList()
        }
        if (trackedBalls.isEmpty()) {
            Timber.d("Tracked balls are empty")
            for (untrackedBall in untrackedCircles) {
                registerBall(untrackedBall)
            }
            return trackedBalls.values.toList()
        }

        val trackedBallIndexes = mutableListOf<Int>()
        for (trackedBall in trackedBalls) {
            trackedBallIndexes.add(trackedBall.key)
        }

        val untrackedBallsWithDistance: MutableList<UntrackedBallWithDistances> = mutableListOf()
        for (untrackedCircle in untrackedCircles) {
            val distancesToBall = LinkedList<BallWithDistance>()
            for ((id, trackedBall) in trackedBalls) {
                val distance = Math.sqrt(Math.pow(untrackedCircle.center.x - trackedBall.circle.center.x, 2.0) +
                        Math.pow(untrackedCircle.center.y - trackedBall.circle.center.y, 2.0))
                distancesToBall.add(BallWithDistance(id, trackedBall, distance))
            }
            distancesToBall.sortBy { it.distance }
            untrackedBallsWithDistance.add(UntrackedBallWithDistances(untrackedCircle, distancesToBall))
        }
        untrackedBallsWithDistance.sortBy { it.distancesToCircle[0].distance }


        for (untrackedBallWithDistance in untrackedBallsWithDistance) {
            if (untrackedBallWithDistance.distancesToCircle.isNotEmpty()) {
                val trackedBallId = untrackedBallWithDistance.distancesToCircle[0].id
                trackedBalls[trackedBallId]!!.circle = untrackedBallWithDistance.untrackedCircle
                val disappeared = disappearedBallsCounter.getValue(trackedBallId)
                disappearedBallsCounter[trackedBallId] = disappeared + 1
                untrackedCircles.remove(untrackedBallWithDistance.untrackedCircle)
                trackedBallIndexes.remove(trackedBallId)

                for (nestedUntrackedBallsWithDistance in untrackedBallsWithDistance) {
                    val iterator = nestedUntrackedBallsWithDistance.distancesToCircle.iterator()
                    while (iterator.hasNext()) {
                        val distanceToBall = iterator.next()
                        if (distanceToBall.id == trackedBallId) {
                            iterator.remove()
                        }
                    }
                }
            }
        }

        for (newBall in untrackedCircles) {
            registerBall(newBall)
        }

        val disappearedCountIterator = disappearedBallsCounter.iterator()
        while (disappearedCountIterator.hasNext()) {
            val disappearedCount = disappearedCountIterator.next()
            if (disappearedCount.value > maxLostFrames) {
                disappearedCountIterator.remove()
                trackedBalls.remove(disappearedCount.key)
            }
        }

        return trackedBalls.values.toList()
    }

    private fun registerBall(circle: Circle) {
        Timber.d("registerBall.nextId: $nextId")
        trackedBalls[nextId] = Ball(nextId, circle)
        disappearedBallsCounter[nextId] = 0
        nextId++
    }

    private data class UntrackedBallWithDistances(val untrackedCircle: Circle, val distancesToCircle: LinkedList<BallWithDistance>)

    private data class BallWithDistance(val id: Int, val trackedBall: Ball, val distance: Double)
}