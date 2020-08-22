package pl.pancordev.poolinterpreter.imageprocessing.balls

import timber.log.Timber
import java.util.*
import kotlin.collections.HashMap

class BallTrackerImpl {

    private val maxLostFrames = 3000
    private val trackedBalls: HashMap<Int, Ball> = HashMap()
    private val disappearedBallsCounter: HashMap<Int, Int> = HashMap()

    private var nextId = 0

    private fun registerBall(ball: Ball) {
        Timber.d("registerBall.nextId: $nextId")
        trackedBalls[nextId] = ball
        disappearedBallsCounter[nextId] = 0
        nextId++
    }

    fun update(untrackedBalls: MutableList<Ball>): HashMap<Int, Ball> {
        Timber.d("-----------------Update started-----------------")
        if (untrackedBalls.isEmpty()) {
            Timber.d("Provided untracked ball list is empty")
            for(disappearedBall in disappearedBallsCounter) {
                disappearedBall.setValue(disappearedBall.value + 1)
            }
            return trackedBalls
        }
        if (trackedBalls.isEmpty()) {
            Timber.d("Tracked balls are empty")
            for (untrackedBall in untrackedBalls) {
                registerBall(untrackedBall)
            }
            return trackedBalls
        }

        val trackedBallIndexes = mutableListOf<Int>()
        for (trackedBall in trackedBalls) {
            trackedBallIndexes.add(trackedBall.key)
        }

        val untrackedBallsWithDistance: MutableList<UntrackedBallWithDistances> = mutableListOf()
        for (untrackedBall in untrackedBalls) {
            val distancesToBall = LinkedList<BallWithDistance>()
            for ((id, trackedBall) in trackedBalls) {
                val distance = Math.sqrt(Math.pow(untrackedBall.center.x - trackedBall.center.x, 2.0) +
                        Math.pow(untrackedBall.center.y - trackedBall.center.y, 2.0))
                distancesToBall.add(BallWithDistance(id, trackedBall, distance))
            }
            distancesToBall.sortBy { it.distance }
            untrackedBallsWithDistance.add(UntrackedBallWithDistances(untrackedBall, distancesToBall))
        }
        untrackedBallsWithDistance.sortBy { it.distancesToBall[0].distance }


        for (untrackedBallWithDistance in untrackedBallsWithDistance) {
            if (untrackedBallWithDistance.distancesToBall.isNotEmpty()) {
                val trackedBallId = untrackedBallWithDistance.distancesToBall[0].id
                trackedBalls[trackedBallId] = untrackedBallWithDistance.untrackedBall
                val disappeared = disappearedBallsCounter.getValue(trackedBallId)
                disappearedBallsCounter[trackedBallId] = disappeared + 1
                untrackedBalls.remove(untrackedBallWithDistance.untrackedBall)
                trackedBallIndexes.remove(trackedBallId)

                for (nestedUntrackedBallsWithDistance in untrackedBallsWithDistance) {
                    val iterator = nestedUntrackedBallsWithDistance.distancesToBall.iterator()
                    while (iterator.hasNext()) {
                        val distanceToBall = iterator.next()
                        if (distanceToBall.id == trackedBallId) {
                            iterator.remove()
                        }
                    }
                }
            }
        }

        for (newBall in untrackedBalls) {
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

        return trackedBalls
    }

    private data class UntrackedBallWithDistances(val untrackedBall: Ball, val distancesToBall: LinkedList<BallWithDistance>)

    private data class BallWithDistance(val id: Int, val trackedBall: Ball, val distance: Double)
}