package com.example.tpamov.game

import android.content.ContentValues.TAG
import android.util.Log
import org.checkerframework.checker.units.qual.m

class Board(val size: Int, val cells: List<Cell>) {
    fun getCell(row:Int, col:Int) = cells[row*size+col]

    fun verificaMelhor(){
        verificaMelhorColuna()
        verificaMelhorLinha()
        getJogadas()
    }

    var numeros = mutableListOf<Int>()
    var operadores = mutableListOf<String>()

    var bestResultadoCol = 0.0
    var bestResultadoRow = 0.0

    var secondBestResultadoCol = 0.0
    var secondBestResultadoRow = 0.0

    var bestResult = 0.0
    var secondBestResult = 0.0
    var resultJogada = 0.0

    //VAI PREENCHER A VAR resultJogada COM O RESULTADO DA CONTA JOGADA PELO JOGADOR
    fun verificaJogada(move : List<Pair<Int,Int>>) : Double{
        for(m in move){
            if(m.first < 0 || m.first > 4 || m.second < 0 || m.second > 4)
                return -1.0 //RETURN JOGADA INVÁLIDA
        }

        var firstMove = move.get(0)
        var secondMove = move.get(1)
        //VÊ SE O JOGADOR JOGOU UMA LINHA VÁLIDA
        //LINHA SUPERIOR, IDA E VOLTA
        if(firstMove.first==0 && firstMove.second==4){
            if( (secondMove.first==4 && secondMove.second==4) ){
                return calculaJogada(firstMove,secondMove)
            }
        }
        else if(firstMove.first==4 && firstMove.second==4)
            if( (secondMove.first==0 && secondMove.second==4) ){
                return calculaJogada(firstMove,secondMove)
            }
        //LINHA DO MEIO, IDA E VOLTA
        if(firstMove.first==0 && firstMove.second==2){
            if( (secondMove.first==4 && secondMove.second==2) ){
                return calculaJogada(firstMove,secondMove)
            }
        }else if(firstMove.first==4 && firstMove.second==2){
            if( (secondMove.first==0 && secondMove.second==2) ){
                return calculaJogada(firstMove,secondMove)
            }
        }
        //LINHA INFERIOR, IDA E VOLTA
        if(firstMove.first==0 && firstMove.second==0){
            if( (secondMove.first==4 && secondMove.second==0) || (secondMove.first==0 && secondMove.second==0) ){
                return calculaJogada(firstMove,secondMove)
            }
        }
        else if(firstMove.first==4 && firstMove.second==0)
            if(  secondMove.first==0 && secondMove.second==0 ){
                return calculaJogada(firstMove,secondMove)
            }

        //VÊ SE O JOGADOR ADICIONOU UMA COLUNA VÁLIDA

        if(firstMove.first==0 && firstMove.second==4){
            if( (secondMove.first==0 && secondMove.second==0) ){
                return calculaJogada(firstMove,secondMove)
            }
        }
        else if(firstMove.first==0 && firstMove.second==0){
            if( (secondMove.first==0 && secondMove.second==4) ){
                return calculaJogada(firstMove,secondMove)
            }
        }
        if(firstMove.first==2 && firstMove.second==4){
            if( (secondMove.first==2 && secondMove.second==0) ){
                return calculaJogada(firstMove,secondMove)
            }
        }
        else if(firstMove.first==2 && firstMove.second==0){
            if( (secondMove.first==2 && secondMove.second==4) ){
                return calculaJogada(firstMove,secondMove)
            }
        }
        if(firstMove.first==4 && firstMove.second==4){
            if( (secondMove.first==4 && secondMove.second==0) ){
                return calculaJogada(firstMove,secondMove)
            }
        }
        if(firstMove.first==4 && firstMove.second==0){
            if( (secondMove.first==4 && secondMove.second==4) ){
                return calculaJogada(firstMove,secondMove)
            }
        }
        Log.d(TAG,"JOGADA INVÁLIDA")
        return -1.0
    }


    fun calculaJogada(from : Pair<Int,Int> , to : Pair<Int,Int>) : Double{
        val fromX = from.first
        val fromY = from.second
        val toX = to.first
        val toY = to.second

        if(fromX==toX && fromY==toY)
            return -1.0

        //TRATA-SE DE UMA LINHA, PORQUE A COORDENADA X FOI ALTERADA
        if(fromX != toX){
            if(fromY == 0) { //LINHA DE BAIXO
                for (i in 0..4) {
                    if(getCell(4,i).isNumber())
                        numeros.add(getCell(4,i).value.toInt())
                    else
                        operadores.add(getCell(4,i).value)
                }
            }
            else if(fromY==2){ //LINHA DO MEIO
                for (i in 0..4) {
                    if(getCell(2,i).isNumber())
                        numeros.add(getCell(2,i).value.toInt())
                    else
                        operadores.add(getCell(2,i).value)
                }
            }
            else if(fromY==4){ //LINHA DE CIMA
                for (i in 0..4) {
                    if(getCell(0,i).isNumber())
                        numeros.add(getCell(0,i).value.toInt())
                    else
                        operadores.add(getCell(0,i).value)
                }
            }

        }
        //TRATA-SE DE UMA COLUNA, PORQUE A COORDENADA Y FOI ALTERADA
        else if(fromY != toY){
            if(fromX == 0) { //COLUNA DA ESQUERDA
                for (i in 0..4) {
                    if(getCell(i,0).isNumber())
                        numeros.add(getCell(i,0).value.toInt())
                    else
                        operadores.add(getCell(i,0).value)
                }
            }
            else if(fromX==2){ //COLUNA DO MEIO
                for (i in 0..4) {
                    if(getCell(i,2).isNumber())
                        numeros.add(getCell(i,2).value.toInt())
                    else
                        operadores.add(getCell(i,2).value)
                }
            }
            else if(fromX==4){ //COLUNA DA DIREITA
                for (i in 0..4) {
                    if(getCell(i,4).isNumber())
                        numeros.add(getCell(i,4).value.toInt())
                    else
                        operadores.add(getCell(i,4).value)
                }
            }
        }
        resultJogada = verificaResultado()
        return resultJogada
    }

    @JvmName("getResultJogada1")
    fun getResultJogada() : Double{
        return resultJogada
    }

    @JvmName("getBestResult1")
    fun getBestResult():Double{
        if(bestResultadoCol>bestResultadoRow)
            bestResult=bestResultadoCol
        else if(bestResultadoCol<bestResultadoRow)
            bestResult=bestResultadoRow
        else{
            bestResult=bestResultadoCol
        }
        return bestResult
    }
    @JvmName("getSecondBestResult1")
    fun getSecondBestResult():Double{
        if(secondBestResultadoCol>secondBestResultadoRow)
            secondBestResult=secondBestResultadoCol
        else if(secondBestResultadoCol<secondBestResultadoRow)
            secondBestResult=secondBestResultadoRow
        else{
            secondBestResult=secondBestResultadoCol
        }
        return secondBestResult
    }

    fun verificaMelhorColuna(){
        bestResultadoCol=0.0
        secondBestResultadoCol=0.0
        for(x in (0..4).filter{it%2 == 0} ) {
            for (i in 0..4) {
                if(getCell(i,x).isNumber())
                    numeros.add(getCell(i,x).value.toInt())
                else
                    operadores.add(getCell(i,x).value)
            }

            val result = verificaResultado()
            if(result >= bestResultadoCol)
                bestResultadoCol = result
            else if(result >= secondBestResultadoCol)
                secondBestResultadoCol = result
        }
    }

    fun verificaMelhorLinha(){
        bestResultadoRow=0.0
        secondBestResultadoRow=0.0
        for(x in (0..4).filter{it%2 == 0} ) { //TROQUEI ESTAVA 4
            for (i in 0..4) {
                if(getCell(x,i).isNumber())
                    numeros.add(getCell(x,i).value.toInt())
                else
                    operadores.add(getCell(x,i).value)
            }

            val result = verificaResultado()
            if(result >= bestResultadoRow)
                bestResultadoRow = result
            else if(result >= secondBestResultadoRow)
                secondBestResultadoRow = result
        }
    }

    fun getJogadas(){
        Log.d(TAG, bestResultadoCol.toString())
        Log.d(TAG, bestResultadoRow.toString())
    }
    //VERIFICA RESULTADO DA EXPRESSÃO
    fun verificaResultado( ) : Double{
        if(numeros.size==0 || operadores.size == 0)
            return -1.0
        val num1 = numeros.get(0)
        val num2 = numeros.get(1)
        val num3 = numeros.get(2)

        val expression = num1.toString().plus(operadores.get(0)).plus(num2.toString())
            .plus(operadores.get(1)).plus(num3.toString())

        val result = evaluate(expression)
        Log.d(TAG,"RESULT: ($result) || EXPRESSION: ($expression)")
        numeros.clear()
        operadores.clear()

        return result
    }

    fun evaluate(str: String): Double {

        data class Data(val rest: List<Char>, val value: Double)

        return object : Any() {

            fun parse(chars: List<Char>): Double {
                return getExpression(chars.filter { it != ' ' })
                    .also { if (it.rest.isNotEmpty()) throw RuntimeException("Unexpected character: ${it.rest.first()}") }
                    .value
            }

            private fun getExpression(chars: List<Char>): Data {
                var (rest, carry) = getTerm(chars)
                while (true) {
                    when {
                        rest.firstOrNull() == '+' -> rest = getTerm(rest.drop(1)).also { carry += it.value }.rest
                        rest.firstOrNull() == '-' -> rest = getTerm(rest.drop(1)).also { carry -= it.value }.rest
                        else                      -> return Data(rest, carry)
                    }
                }
            }

            fun getTerm(chars: List<Char>): Data {
                var (rest, carry) = getFactor(chars)
                while (true) {
                    when {
                        rest.firstOrNull() == '*' -> rest = getTerm(rest.drop(1)).also { carry *= it.value }.rest
                        rest.firstOrNull() == '/' -> rest = getTerm(rest.drop(1)).also { carry = it.value / carry }.rest
                        else                      -> return Data(rest, carry)
                    }
                }
            }

            fun getFactor(chars: List<Char>): Data {
                return when (val char = chars.firstOrNull()) {
                    '+'              -> getFactor(chars.drop(1)).let { Data(it.rest, +it.value) }
                    '-'              -> getFactor(chars.drop(1)).let { Data(it.rest, -it.value) }
                    '('              -> getParenthesizedExpression(chars.drop(1))
                    in '0'..'9', ',' -> getNumber(chars)
                    else             -> throw RuntimeException("Unexpected character: $char")
                }
            }

            fun getParenthesizedExpression(chars: List<Char>): Data {
                return getExpression(chars)
                    .also { if (it.rest.firstOrNull() != ')') throw RuntimeException("Missing closing parenthesis") }
                    .let { Data(it.rest.drop(1), it.value) }
            }

            fun getNumber(chars: List<Char>): Data {
                val s = chars.takeWhile { it.isDigit() || it == '.' }.joinToString("")
                return Data(chars.drop(s.length), s.toDouble())
            }

        }.parse(str.toList())

    }

}