package com.example.flashcarapp


import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.roadrivals.GameTask

class GameView(var c: Context, private var gameTask: GameTask) : View(c) {

    private var myPaint: Paint = Paint()
    private var speed = 1
    private var time = 0
    private var score = 0
    private var mycar = 1 // Initialize mycar in the middle lane
    private val otherMonstcar = ArrayList<HashMap<String, Any>>()
    private var isRunning = false

    private var viewWidth = 0
    private var viewHeight = 0


    private lateinit var myCar: Drawable
    private lateinit var otherCar: Drawable
    private lateinit var roadDrawable: Drawable

    init {
        myPaint = Paint()
        loadDrawables()
    }

    private fun loadDrawables() {
        myCar = context.getDrawable(R.drawable.mycar)!!
        otherCar = context.getDrawable(R.drawable.othercar)!!

    }

    private fun updateScoreAndSpeed() {
        score++
        speed = 1 + score / 8
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (canvas == null) return

        viewWidth = width
        viewHeight = height


        myPaint.color = Color.argb(255, 182, 115, 82)
        canvas.drawRect(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat(), myPaint)


        val laneWidth = viewWidth / 3
        myPaint.color = Color.WHITE
        myPaint.strokeWidth = 5f
        canvas.drawLine(laneWidth.toFloat(), 0f, laneWidth.toFloat(), viewHeight.toFloat(), myPaint)
        canvas.drawLine((laneWidth * 2).toFloat(), 0f, (laneWidth * 2).toFloat(), viewHeight.toFloat(), myPaint)

        // Draw  line
        val centerX = laneWidth.toFloat() + laneWidth / 2
        myPaint.color = Color.YELLOW
        for (i in 0 until viewHeight step 50) {
            canvas.drawLine(centerX, i.toFloat(), centerX, (i + 30).toFloat(), myPaint)
        }

        // Update time and Other cars
        if (isRunning) {
            time += 10 + speed
            if (time % 700 < 10 + speed) {
                val map = HashMap<String, Any>()
                map["lane"] = (0..2).random()
                map["startTime"] = time
                otherMonstcar.add(map)
            }
        }

        // Draw the myCar
        val carWidth = laneWidth / 2 // adjust according to your design
        val carHeight = carWidth  * 1 // adjust according to your design
        val manX = mycar * laneWidth + (laneWidth - carWidth ) / 2
        val manY = viewHeight - carHeight
        myCar.setBounds(manX, manY, manX + carWidth , manY + carHeight)
        myCar.draw(canvas)

        // Draw other other cars and handle collisions
        val indicesToRemove = mutableListOf<Int>()
        for (i in otherMonstcar.indices) {
            val carX = otherMonstcar[i]["lane"] as Int * laneWidth + (laneWidth - carWidth ) / 2
            var carY = time - (otherMonstcar[i]["startTime"] as Int)
            carY *= speed // Speed up the monstercar movement
            if (carY < -carHeight) { // Remove monsterscar that have passed beyond the top of the screen
                indicesToRemove.add(i)
                updateScoreAndSpeed()
            } else {
                val otherCarWidth = laneWidth / 2 // adjust according to your design
                val otherCarHeight = otherCarWidth * 1 // adjust according to your design
                otherCar.setBounds(carX, carY, carX + otherCarWidth, carY + otherCarHeight)
                otherCar.draw(canvas)
                if (isRunning && otherMonstcar[i]["lane"] as Int == mycar && carY > manY - otherCarHeight && carY < manY + carHeight) {
                    // Game over if a collision occurs
                    gameTask.closeGame(score)
                } else if (isRunning && carY >= manY + carHeight && carY < manY + 2 * carHeight) {
                    // Score increases when the car successfully dodges a monstercar
                    score += 1
                }
            }
        }

// Remove monsterscar outside the loop
        indicesToRemove.forEach { index ->
            otherMonstcar.removeAt(index)
        }

        // Draw score and speed
        myPaint.color = Color.WHITE
        myPaint.textSize = 40f
        canvas.drawText("Score : $score", 80f, 80f, myPaint)
        canvas.drawText("Speed : $speed", 380f, 80f, myPaint)

        // Redraw the view
        if (isRunning) {
            invalidate()
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            when (it.action) {
                MotionEvent.ACTION_DOWN -> {
                    val laneWidth = viewWidth / 3
                    val x1 = it.x
                    mycar = when {
                        x1 < laneWidth -> 0
                        x1 < laneWidth * 2 -> 1
                        else -> 2
                    }
                }
            }
        }
        return true
    }

    fun startAnimation() {
        isRunning = true
        invalidate()
    }

    fun stopAnimation() {
        isRunning = false
    }
}
