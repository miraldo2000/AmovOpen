package com.example.tpamov.view

import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.DragEvent
import android.view.DragEvent.ACTION_DRAG_STARTED
import android.view.MotionEvent
import android.view.MotionEvent.INVALID_POINTER_ID
import android.view.View
import androidx.core.view.MotionEventCompat
import com.example.tpamov.game.Cell
import kotlin.math.min


class BoardView(context: Context?, attrs: AttributeSet?) : View(context,attrs)  {
    private val paint = Paint()

    private var selectedRow = 0
    private var selectedCol = 0

    private var fromRow = -1
    private var fromCol = -1

    private var listener: OnTouchListener? = null
    private var dragListener: OnDragListener?=null


    private var cells: List<Cell>? = null

    private var size = 5
    private var cellSizePixels = 10f
    private var sqrtSize = 1

    private lateinit var play : List<Pair<Int,Int>>

    private val thickLinePaint = Paint().apply{
        style = Paint.Style.STROKE
        color = Color.WHITE
        strokeWidth = 2f
    }

    private val textPaint = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
        color = Color.WHITE
        textSize = 47F
    }
    private val selectedCellPaint = Paint().apply{
        style = Paint.Style.FILL_AND_STROKE
        color = Color.parseColor("#8BCCC485")
    }

    //DESENHA O QUADRO A APRESENTAR
    override fun onDraw(canvas: Canvas) {
        cellSizePixels = (width / size).toFloat()
        fillCells(canvas)
        drawLines(canvas)
        drawText(canvas)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val sizePixels = min(widthMeasureSpec,heightMeasureSpec)
        setMeasuredDimension(sizePixels,sizePixels)
    }

    private fun drawLines(canvas : Canvas){
        canvas.drawRect(0F,0F,width.toFloat(),height.toFloat(),thickLinePaint)

        for(i in 0..4){
            val paintToUse = when (i%sqrtSize){
                0->thickLinePaint
                else ->thickLinePaint//thinLinePaint
            }
            canvas.drawLine(i*cellSizePixels,0F, (i)*cellSizePixels, cellSizePixels*5,paintToUse)
            canvas.drawLine(
                0F,
                i*cellSizePixels,
                cellSizePixels*5,
                i*cellSizePixels,
                paintToUse
            )
        }
    }
    //QUANDO CLICA PINTA DE VERDE
    private fun fillCell(canvas:Canvas,r:Int,c:Int,paint:Paint){
        //canvas.drawRect(c*cellSizePixels,r*cellSizePixels,(c+1)*cellSizePixels/sqrtSize,(r+1)*cellSizePixels/sqrtSize,paint)
        //canvas.drawRect(c*cellSizePixels,r*cellSizePixels,(c+3)*cellSizePixels/sqrtSize,(r+1)*cellSizePixels,paint)
        canvas.drawRect(c*cellSizePixels,r*cellSizePixels,(c+1)*cellSizePixels,(r+1)*cellSizePixels,paint)
    }
    private fun fillCells(canvas: Canvas){
        if(selectedRow==-1 || selectedCol==-1) return
        cells?.forEach{
            val r = it.row
            val c = it.col
            if(r==selectedRow && c==selectedCol){
                fillCell(canvas,r,c,selectedCellPaint)
            }
        }
    }
    fun updateSelectedCellUi(row:Int, col:Int){
        selectedRow = row
        selectedCol = col
        invalidate()
    }

    private fun handleTouchEvent(x:Float, y:Float){
        //selectedRow = (y/cellSizePixels).toInt()
        //selectedCol = (x/cellSizePixels).toInt()
        val possibleSelectedRow = (y/cellSizePixels).toInt()
        val possibleSelectedCol = (x/cellSizePixels).toInt()
        listener?.onCellTouched(possibleSelectedRow, possibleSelectedCol)
        //listener?.onCellTouched(possibleSelectedRow,possibleSelectedCol)
    }
    private fun handleDragEvent(x:Float, y:Float){
        //selectedRow = (y/cellSizePixels).toInt()
        //selectedCol = (x/cellSizePixels).toInt()
        val possibleSelectedRow = (y/cellSizePixels).toInt()
        val possibleSelectedCol = (x/cellSizePixels).toInt()
        dragListener?.onCellDragged(possibleSelectedRow, possibleSelectedCol)
        //listener?.onCellTouched(possibleSelectedRow,possibleSelectedCol)
    }

    fun registerDragListener(lister: OnDragListener){
        this.dragListener = lister
    }


    fun registerListener(lister: OnTouchListener){
        this.listener = lister
    }

    interface OnDragListener{
        fun onCellDragged(row: Int,col:Int)
    }

    interface OnTouchListener{
        fun onCellTouched(row: Int,col:Int)
    }

    fun updateCells(cells: List<Cell>){
        this.cells = cells
        invalidate()
    }

    private fun drawText(canvas:Canvas){
        cells?.forEach{
            val row = it.row
            val col = it.col
            val valueString = it.value

            val textBounds = Rect()
            textPaint.getTextBounds(valueString,0,valueString.length, textBounds)
            val textWidth = textPaint.measureText(valueString)
            val textHeight = textBounds.height()

            canvas.drawText(valueString,(col*cellSizePixels)+cellSizePixels/2-textWidth/2,
                    (row*cellSizePixels)+cellSizePixels/2-textHeight/2,textPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false
        //RETURN Gesture Detector(event)
        when(event.action){
            //AQUI?
            MotionEvent.ACTION_DOWN -> {
                fromCol = ((event.x / cellSizePixels).toInt())
                fromRow = 4-((event.y / cellSizePixels).toInt())
                Log.d(TAG,"from ($fromCol,$fromRow)")
                handleTouchEvent(event.x,event.y)
            }


            MotionEvent.ACTION_UP -> {
                val col = ((event.x / cellSizePixels).toInt())
                val row = 4-((event.y / cellSizePixels).toInt())
                Log.d(TAG,"from ($fromCol,$fromRow) to ($col,$row)")

                val from : Pair<Int,Int> = Pair(fromCol,fromRow)
                val to : Pair<Int,Int> = Pair(col,row)
                play = mutableListOf(from,to)
            }
        }
        return true
    }


    override fun onDragEvent(event: DragEvent?): Boolean {
        event ?: return false
        when(event.action){
            ACTION_DRAG_STARTED -> {
                fromCol = ((event.x / cellSizePixels).toInt())
                fromRow = 4-((event.y / cellSizePixels).toInt())
                Log.d(TAG,"at ($fromCol,$fromRow)")
                handleDragEvent(event.x,event.y)
            }
            DragEvent.ACTION_DRAG_ENTERED -> {
                fromCol = ((event.x / cellSizePixels).toInt())
                fromRow = 4-((event.y / cellSizePixels).toInt())
                Log.d(TAG,"at ($fromCol,$fromRow)")
                handleDragEvent(event.x,event.y)
            }
            DragEvent.ACTION_DRAG_EXITED -> {
                fromCol = ((event.x / cellSizePixels).toInt())
                fromRow = 4-((event.y / cellSizePixels).toInt())
                Log.d(TAG,"at ($fromCol,$fromRow)")
                handleDragEvent(event.x,event.y)
            }
            DragEvent.ACTION_DRAG_LOCATION -> {
                fromCol = ((event.x / cellSizePixels).toInt())
                fromRow = 4-((event.y / cellSizePixels).toInt())
                Log.d(TAG,"at ($fromCol,$fromRow)")
                handleDragEvent(event.x,event.y)
            }
        }
        return true
    }


    fun getPlay():List<Pair<Int,Int>>{
        return play
    }
}