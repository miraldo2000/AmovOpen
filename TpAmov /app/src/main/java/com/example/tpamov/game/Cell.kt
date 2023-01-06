package com.example.tpamov.game

class Cell (val row: Int, val col: Int, var value:String){

    fun isNumber() : Boolean{
        val flag = value.toIntOrNull()
        return flag != null //É NÚMERO
    }
}