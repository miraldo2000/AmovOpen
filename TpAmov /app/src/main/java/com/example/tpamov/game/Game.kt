package com.example.tpamov.game

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.MutableLiveData

class Game {
    var selectedCellLiveData = MutableLiveData<Pair<Int,Int>>()
    var cellsLiveData = MutableLiveData<List<Cell>>()

    private var selectedRow = -1
    private var selectedCol = -1

    private var numbers = listOf(0)
        get() = field
        set(value) {
            field = value// setter
        }
    private var operands = listOf("+","-","*","/")
    private var dificulty = 0 //0->Fácil , 1->Médio, 2->Difícil
    private var time:Long = 10000L

    private var score = 0 //Score do Jogador
    private var contAcertadas = 0 //Conta as acertadas pelo jogador para subir de nível
    private var timeRewarded = 0L //Tempo que vai receber por acertar dependendo de cada nível

    private lateinit var board : Board
    private var bestResult : Double = 0.0
    private var secondBestResult : Double = 0.0
    private var playedResult  : Double = 0.0


    fun initBoard(){
        setGameVariables()
        val cells = List(5*5){
                i->
            if(i%2 != 0)
                Cell(i/5,i % 5, operands.random())
            else if((i==6||i==8||i==16||i==18)) Cell(i / 5, i % 5, "")
            else
                Cell(i/5,i % 5, numbers.random().toString())
        }
        board = Board(5, cells)
        selectedCellLiveData.postValue(Pair(selectedRow,selectedCol))
        cellsLiveData.postValue(board.cells as List<Cell>?)

        board.verificaMelhor()
        secondBestResult = board.getSecondBestResult()
        bestResult = board.getBestResult()
    }

    fun getScore():Int{
        return score
    }
    fun getTime():Int{
        var long = time/1000
       // var tempo = numStr.code
        return long.toInt()
    }
    //ALTERAÇÃO DE SCORE NÍVEL A NÍVEL
    fun upScoreBasedOnLevel(acertouMax : Int){
        if(acertouMax==0) //Se não acertou, não aumenta o score
            return
        if(dificulty==0){
            if(acertouMax==1) //SIGNIFICA QUE O JOGADOR ACERTOU NO MAIOR RESULTADO
                score+=2
            else
                score+=1
        }else if(dificulty==1){
            if(acertouMax==1)
                score+=4
            else
                score+=2
        }else if(dificulty==2){
            if(acertouMax==1)
                score+=6
            else
                score+=4
        }
    }

    fun getResultPlayed() : Double {
        return playedResult
    }

    fun getBestResult() : Double {
        return bestResult
    }
    fun getSecondBestResult() : Double {
        return secondBestResult
    }

    fun acertou(move : List<Pair<Int,Int>>) : Int{
        //Verificar jogada com as top 2 do board || RETORNA 0 SE N ACERTOU, 1 SE ACERTOU NO MAIOR, 2 SE ACERTOU NO SEGUNDO MAIOR
        val resultadoJogada = board.verificaJogada(move)
        playedResult = resultadoJogada
        if(resultadoJogada == bestResult) {
            upScoreBasedOnLevel(1) //ACERTOU NO MELHOR RESULTADO
            contAcertadas+=1
            Log.d(TAG,"ACERTOU ($contAcertadas) vezes")

            if(contAcertadas==3){
                contAcertadas=0
                level_up()
            }
            return 1
        }
        else if(resultadoJogada == secondBestResult){
            upScoreBasedOnLevel(2) //ACERTOU NO SEGUNDO MELHOR RESULTADO
            contAcertadas+=1
            Log.d(TAG,"ACERTOU ($contAcertadas) vezes")

            if(contAcertadas==3){
                contAcertadas=0
                level_up()
            }
            return 2
        }
        else if(resultadoJogada==-1.0)
            return 3
        return 0
    }


    fun level_up(){
        if(dificulty==2)
            return
        dificulty += 1

    }

    fun setGameVariables(){
        numbers = mutableListOf()
        operands = mutableListOf()
        if(dificulty == 0){
            time = 15_000 // 90segundos para jogar
            timeRewarded = 5_000 //No 1 nível apenas recebe 10 secs por acertar
            for(i in 0..5)
                (numbers as MutableList<Int>).add((0..10).random())
            (operands as MutableList<String>).add("+")
        }
        else if(dificulty == 1){
            time = 20_000
            timeRewarded = 5_000 //No 1 nível  recebe 20 secs por acertar
            for(i in 0..10)
                (numbers as MutableList<Int>).add((0..100).random())
            (operands as MutableList<String>).add("+")
            (operands as MutableList<String>).add("-")
        }
        else if(dificulty == 2){
            time = 30_000
            timeRewarded = 50_000
            for(i in 0..15)
                (numbers as MutableList<Int>).add((0..999).random())
            (operands as MutableList<String>).add("+")
            (operands as MutableList<String>).add("-")
            (operands as MutableList<String>).add("*")
            (operands as MutableList<String>).add("/")
        }
    }

    fun updateSelectedCell(row: Int, col:Int){
        selectedRow=row
        selectedCol=col
        selectedCellLiveData.postValue(Pair(row,col))
    }

    fun handleInput(number:Int){
        if(selectedCol == -1 || selectedRow == -1) return

        board.getCell(selectedRow,selectedCol).value=number.toString()
        cellsLiveData.postValue(board.cells)
    }

    fun stopGame(){
        empty_board();
    }

    fun empty_board(){
        val cells = List(5*5){
                i->
            if(i%2 != 0)
                Cell(i/5,i % 5, "")
            else Cell(i / 5, i % 5, "")
        }
        board = Board(5, cells)
        //selectedCellLiveData.postValue(Pair(selectedRow,selectedCol))
        cellsLiveData.postValue(board.cells as List<Cell>?)
    }
}