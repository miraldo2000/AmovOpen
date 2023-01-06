package com.example.tpamov.activities

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.text.InputFilter
import android.text.Spanned
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.example.tpamov.MainActivity
import com.example.tpamov.R
import com.example.tpamov.game.Cell
import com.example.tpamov.view.BoardView
import com.example.tpamov.viewmodel.GameViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_single.*

class GameActivity : AppCompatActivity() , BoardView.OnTouchListener, BoardView.OnDragListener{

    val db = Firebase.firestore
    private var context : Context = el_context

    private lateinit var timer: CountDownTimer //TEMPORIZADOR DA RONDA
    private lateinit var mainIimer: CountDownTimer //TEMPORIZADOR DO TEMPO TOTAL JOGADO

    private lateinit var viewModel: GameViewModel

    private lateinit var textView : TextView
    private lateinit var tempoJogado : TextView

    private var contadorAcertadas = 0
    private var finalScore = 0
    private var totalTime : Long = 0
    private var millisUntilFinished:Long = 0

    private var timeAwarded:Int = 2 //NO 1 NÍVEL O JOGADOR RECEBE 2SEGUNDOS POR JOGADA ACERTADA
    private lateinit var btnPause : Button

    private var timeStoped : Long = 0


    companion object ContextStatic{
        private const val SERVER_MODE = 0
        @SuppressLint("StaticFieldLeak")
        private lateinit var el_context : Context
        fun start(context : Context) : Intent {
            el_context = context
            return Intent(context, GameActivity::class.java).apply {
                putExtra("mode", SERVER_MODE)
            }
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single)

        //CRIA A BOX QUE DÁ DISPLAY AO DIALOG
        val edtBox = EditText(this).apply {
            maxLines = 1
            filters = arrayOf(object : InputFilter {
                override fun filter(
                    source: CharSequence?,
                    start: Int,
                    end: Int,
                    dest: Spanned?,
                    dstart: Int,
                    dend: Int
                ): CharSequence? {
                    source?.run {
                        var ret = ""
                        forEach {
                            if (it.isDigit() || it == '.')
                                ret += it
                        }
                        return ret
                    }
                    return null
                }

            })
        }
        //CRIA O DIALOG PARA CONFIRMAR SE O JOGADOR QUER SAIR DO JOGO
        val dlg = AlertDialog.Builder(this)
            .setTitle(R.string.leave)
            .setMessage(R.string.sure_to_leave)
            .setPositiveButton(R.string.stay) { _: DialogInterface, _: Int ->
                val secsRemaining = (millisUntilFinished / 1000).toInt()
                startTimer(secsRemaining,"")
            }
            .setNegativeButton(R.string.leave) { _: DialogInterface, _: Int ->
                //TEMPORIZADORES NÃO PARAM PORQUE O SCORE NÃO VAI SER MANTIDO, DEVIDO AO JOGADOR DECIDIR SAIR DO JOGO SEM TERMINAR
                viewModel.game.stopGame()
                finish()
            }
            .setCancelable(false)
            .setView(edtBox)
            .create()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                stopTimer()
                //TODO: PARAR MAIN TIMER
                dlg.show()
            }
        })

        //VIEWS
        textView = findViewById(R.id.tvTime)
        val acertouView = findViewById<TextView>(R.id.tvAcertou)
        val tvScore = findViewById<TextView>(R.id.tvScore)
        val respostaCertaView = findViewById<TextView>(R.id.tvAnswer)
        val respostaJogadaView = findViewById<TextView>(R.id.tvJogada)
        val segundaRespostaCertaView = findViewById<TextView>(R.id.tvSecondAnswer)
        tempoJogado = findViewById(R.id.tvTimePlayed)

        var onResume = false

        //BUTONS
        btnPause = findViewById(R.id.btnPause)
        btnPause.isClickable = false
        btnPause.isVisible=false
        btnPause.setOnClickListener {
            if(!onResume) {
                stopTimer()
                //PARAR MAIN TIMER ------------------------------------------------------------
                timeStoped = tempoPassado
                //Toast.makeText(this, "Game Paused", Toast.LENGTH_LONG).show()
                onResume=true
                btnPause.text = getString(R.string.resume)
            }
            else{
                onResume = false
                //RETOMAR MAIN TIMER ----------------------------------------------------------
                tempoPassado = timeStoped
                btnPause.text = getString(R.string.pause)
                val secsRemaining = (millisUntilFinished / 1000).toInt()
                startTimer(secsRemaining,"BREAK")
            }
        }
        findViewById<Button>(R.id.btnPlay).setOnClickListener {
            val movePlayed = boardView.getPlay()
            val acertou = viewModel.game.acertou(movePlayed)

            val resultadoJogado = viewModel.game.getResultPlayed()
            val bestResultado = viewModel.game.getBestResult()
            val secondBestResultado = viewModel.game.getSecondBestResult()
            respostaJogadaView.text = getString(R.string.playResult).plus("\n").plus("$resultadoJogado")
            respostaCertaView.text = getString(R.string.winningResult).plus("\n").plus("$bestResultado")
            segundaRespostaCertaView.text = getString(R.string.secondWinningResult).plus("\n").plus("$secondBestResultado")

            if(acertou==1 || acertou==2){
                contadorAcertadas+=1
                //ACERTOU E SUBIU D NÍVEL
                if(contadorAcertadas==3||contadorAcertadas==6) {
                    acertouView.text = getString(R.string.correctLvlup)
                    acertouView.setBackgroundColor(Color.parseColor("#BA9DBA7B"))

                    btnPause.isClickable = true
                    btnPause.isVisible=true
                    btnPlay.isClickable = false
                    timeAwarded += 4 //RECEBE +4 SEGUNDOS DE TEMPO POR CADA VEZ QUE PASSAR DE NÍVEL (6s .. 10s)

                    stopTimer()
                    startTimer(5,"BREAK")
                }
                else{
                    if(acertou==1)
                        acertouView.text = getString(R.string.correct)
                    else
                        acertouView.text = getString(R.string.second_correct)
                    //else {
                    //acertouView.text = getString(R.string.correct)
                    acertouView.setBackgroundColor(Color.parseColor("#BA9DBA7B"))

                    stopTimer()
                    val secsAdd = (millisUntilFinished / 1000).toInt()
                    startTimer(secsAdd,"",timeAwarded)//ADD 5 SECS
                }

                //}
            }
            else if(acertou==0){
                acertouView.text = getString(R.string.incorrect)
                acertouView.setBackgroundColor(Color.parseColor("#B58C5D5A"))
            }
            else if(acertou==3){
                acertouView.text = getString(R.string.invalid)
                acertouView.setBackgroundColor(Color.parseColor("#D5CC80"))
            }

            tvScore.text = getString(R.string.points).plus("\n\n\n").plus(viewModel.game.getScore().toString())
            viewModel.game.initBoard()
        }

        //LISTENERS DA BoardView
        boardView.registerListener(this)
        boardView.registerDragListener(this)

        //INICIALIZA O ViewModel E FICAR A OBSERVAR , QUANDO EXISTE ALGUMA ALTERAÇÃO, CHAMA updateSelectedCell
        viewModel = ViewModelProvider(this).get(GameViewModel::class.java)
        viewModel.game.selectedCellLiveData.observe(this) { updateSelectedCellUi(it) }
        viewModel.game.cellsLiveData.observe(this) { updateCells(it) }

        //COMEÇA JOGO E TIMERS
        viewModel.game.initBoard()
        startMainTimer()
        startTimer(viewModel.game.getTime())
    }

    var tempoPassado:Long= 0 //TEMPO TOTAL QUE O JOGADOR JOGOU
    var tempoPausado:Long= 0  //TEMPO PASSADO EM PAUSAS
    var tempoOnBack:Long= 0 //TEMPO PASSADO DEPOIS DE CLICAR NO BACK

    //TIMERS------
    var tempoPassadoSecs:Long= 0
    val longTime : Long = 1000000000L
    fun startMainTimer(msg : String = ""){
        var flag = true
        mainIimer = object : CountDownTimer(longTime,1){
            override fun onFinish() {
                /*totalTime = tempoPassado/1000
                tempoPassadoSecs = tempoPassado / 10000
                Log.d(TAG,"MAIN TIMER TERMINOU COM $tempoPassado segundos")
                tempoJogado.text = getString(R.string.times_up)
                checkTime()
                startTimer(1,"REFRESH")*/
                flag = false
            }
            override fun onTick(ticking: Long) {
                tempoPassado = longTime - ticking
                if(msg=="STOP"){
                    timeStoped = tempoPassado
                    stopMainTimer("STOP")
                }
                if(msg=="RESUME"){
                    tempoPassado = timeStoped
                    stopMainTimer("RESUME")
                }
                val scds = (tempoPassado / 1000)
                if (flag) {
                    tempoJogado.text =
                        getString(R.string.time_played).plus("\n").plus(String.format("%02d", scds))
                    //scds = tempoPassado - (5+5+5)
                }
            }
        }.start()
    }
    fun stopMainTimer(msg : String = ""){
        if(msg=="STOP"){
            mainIimer.onFinish()
        }
        else if(msg=="FINISH"){
            totalTime = tempoPassado/1000
            tempoPassadoSecs = tempoPassado / 10000
            Log.d(TAG,"MAIN TIMER TERMINOU COM $tempoPassado segundos")
            tempoJogado.text = getString(R.string.times_up)
            checkTime()
            startTimer(1,"REFRESH")
        }
        else if(msg == "RESUME"){
            //LÓGICA PARA RESUMIR MAIN TIMER
        }
    }
    fun startTimer(seconds : Int , msg : String = "", addedSeconds : Int = 0){
        //timer.cancel()
        timer = object : CountDownTimer(((seconds+addedSeconds)*1000+1000).toLong(), 1){
            override fun onTick(remaining: Long) {
                millisUntilFinished = remaining
                val scds = (remaining / 1000)
                if(msg==("")){
                    textView.text = getString(R.string.time).plus("\n").plus(String.format("%02d", scds))
                    btnPause.isClickable = false
                    btnPause.isVisible=false
                    btnPlay.isClickable=true
                }
                else if(msg==("BREAK")){
                    textView.text = getString(R.string.break_time).plus(String.format("%02d", scds))
                    viewModel.game.empty_board()
                    timeStoped = tempoPassado
                    //PARA MAIN TIMER --------------------------------------------------------------------------------
                }
            }
            //TERMINA O JOGO, FALTA COMPARAR OS TOP5 SCORES E TEMPOS
            override fun onFinish() {
                if(msg == "") {
                    textView.text = getString(R.string.times_up)
                    viewModel.game.empty_board()
                    btnPlay.isClickable = false

                    finalScore = viewModel.game.getScore()

                    checkScore(finalScore)
                    stopMainTimer("STOP")
                    stopMainTimer("FINISH")
                    //mainIimer.onFinish()
                }
                //QUANDO TERMINA A BREAK, COMEÇA O NOVO NÍVEL
                else if(msg == "BREAK"){
                    textView.text = getString(R.string.time)
                    viewModel.game.initBoard()
                    startTimer(viewModel.game.getTime()) //COMEÇA O NOVO NÍVEL

                    //COMEÇA MAIN TIMER --------------------------------------------------------------------------------
                    btnPlay.isClickable = true
                }
                    //AQUI TERMINA DE VEZ
                //ESTE TEMPORIZADOR É PARA DAR TEMPO PARA A BD ATUALIZAR E MOSTRAR OS MELHORES RESULTADOS
                else if(msg == "REFRESH"){
                    val tvBestScores : TextView = (context as MainActivity).findViewById(R.id.tvBestResults)
                    refreshViews(tvBestScores)
                    refreshTime()
                    finish()
                }
            }
        }.start()
    }
    fun stopTimer(){
        timer.cancel()
        //textView.text = "Timer Stoped"
    }
    fun refreshViews(textview : TextView){
        db.collection("users").document("Top 5 Scores").get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val strAux = document.data!!.get("Top 1").toString().plus("\n")
                        .plus(document.data!!.get("Top 2").toString()).plus("\n")
                        .plus(document.data!!.get("Top 3").toString()).plus("\n")
                        .plus(document.data!!.get("Top 4").toString()).plus("\n")
                        .plus(document.data!!.get("Top 5").toString())

                    textview.text = getString(R.string.best_scores).plus("\n").plus(strAux)
                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }


    }
    //TIMERS------

    //DÁ REFRESH À VIEW DOS TOP5 MELHORES TEMPOS
    fun refreshTime(){
        db.collection("users").document("Top 5 Times").get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val strAux = document.data!!.get("Top 1").toString().plus("\n")
                        .plus(document.data!!.get("Top 2").toString()).plus("\n")
                        .plus(document.data!!.get("Top 3").toString()).plus("\n")
                        .plus(document.data!!.get("Top 4").toString()).plus("\n")
                        .plus(document.data!!.get("Top 5").toString())

                    val tvBestTimes : TextView = (context as MainActivity).findViewById(R.id.tvBestTimes)
                    tvBestTimes.text = getString(R.string.best_times).plus("\n").plus(strAux)
                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
    }

    //CHECKS FOR TOP5 SCORES AND TIMES TO SEE IF CURRENT GAME BEAT ANY OF THE SCORES
    fun checkScore(finalScore : Int) : Int{
        lateinit var documentData : DocumentSnapshot
        db.collection("users").document("Top 5 Scores").get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    documentData = document
                    for(i in 0 .. 4) {
                        val aux = i+1
                        val top = document.data!!.getValue("Top $aux")
                        if(finalScore >= top.toString().toInt()){
                            updateScore(aux , documentData, finalScore)
                            break
                        }
                    }
                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                } else {

                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }



        return -1
    }
    fun checkTime(){
        lateinit var documentData : DocumentSnapshot
        db.collection("users").document("Top 5 Times").get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    documentData = document
                    for(i in 0 .. 4) {
                        val aux = i+1
                        val top = document.data!!.getValue("Top $aux")
                        if(tempoPassado >= top.toString().toDouble()){
                            updateTime(aux , documentData)
                            break
                        }
                    }
                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                } else {

                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
    }
    fun updateTime(posicao : Int, documentData : DocumentSnapshot){

        val top1: Int = documentData.get("Top 1").toString().toInt()
        val top2: Int = documentData.get("Top 2").toString().toInt()
        val top3: Int = documentData.get("Top 3").toString().toInt()
        val top4: Int = documentData.get("Top 4").toString().toInt()
        if (posicao == 1) {
            //if(finalScore!=top1)
                db.collection("users").document("Top 5 Times")
                    .update(mapOf(
                        "Top 1" to tempoPassado,
                        "Top 2" to top1,
                        "Top 3" to top2,
                        "Top 4" to top3,
                        "Top 5" to top4 ,
                    ))
        }
        else if(posicao == 2){
            //if(finalScore!=top2)
                db.collection("users").document("Top 5 Times")
                    .update(mapOf(
                        "Top 2" to tempoPassado,
                        "Top 3" to top2,
                        "Top 4" to top3,
                        "Top 5" to top4
                    ))
        }
        else if(posicao == 3){
            //if(finalScore!=top3)
                db.collection("users").document("Top 5 Times")
                    .update(mapOf(
                        "Top 3" to tempoPassado,
                        "Top 4" to top3,
                        "Top 5" to top4
                    ))
        }
        else if(posicao == 4){
            //if(finalScore!=top4)
                db.collection("users").document("Top 5 Times")
                    .update(mapOf(
                        "Top 4" to tempoPassado,
                        "Top 5" to top4
                    ))
        }
        else if(posicao == 5){
            db.collection("users").document("Top 5 Times")
                .update(mapOf(
                    "Top 5" to finalScore
                ))
        }

    }
    fun updateScore(posicao : Int, documentData : DocumentSnapshot, finalScore: Int){

        val top1: Int = documentData.get("Top 1").toString().toInt()
        val top2: Int = documentData.get("Top 2").toString().toInt()
        val top3: Int = documentData.get("Top 3").toString().toInt()
        val top4: Int = documentData.get("Top 4").toString().toInt()
        if (posicao == 1) {
            if(finalScore!=top1)
                db.collection("users").document("Top 5 Scores")
                    .update(mapOf(
                        "Top 1" to finalScore,
                        "Top 2" to top1,
                        "Top 3" to top2,
                        "Top 4" to top3,
                        "Top 5" to top4 ,
                    ))
        }
        else if(posicao == 2){
            if(finalScore!=top2)
                db.collection("users").document("Top 5 Scores")
                    .update(mapOf(
                        "Top 2" to finalScore,
                        "Top 3" to top2,
                        "Top 4" to top3,
                        "Top 5" to top4
                    ))
        }
        else if(posicao == 3){
            if(finalScore!=top3)
                db.collection("users").document("Top 5 Scores")
                    .update(mapOf(
                        "Top 3" to finalScore,
                        "Top 4" to top3,
                        "Top 5" to top4
                    ))
        }
        else if(posicao == 4){
            if(finalScore!=top4)
                db.collection("users").document("Top 5 Scores")
                    .update(mapOf(
                        "Top 4" to finalScore,
                        "Top 5" to top4
                    ))
        }
        else if(posicao == 5){
            db.collection("users").document("Top 5 Scores")
                .update(mapOf(
                    "Top 5" to finalScore
                ))
        }

    }

    //AUX
    override fun onStart() {
        super.onStart()
        timer.start()
    }
    override fun onStop() {
        super.onStop()
        timer.cancel()
    }
    private fun updateSelectedCellUi(cell: Pair<Int,Int>?) = cell?.let{
        boardView.updateSelectedCellUi(cell.first, cell.second)
    }
    private fun updateCells(cells: List<Cell>?) = cells?.let{
        boardView.updateCells(cells)
    }
    override fun onCellTouched(row:Int, col:Int){
        viewModel.game.updateSelectedCell(row,col)
    }
    override fun onCellDragged(row:Int, col:Int){
        viewModel.game.updateSelectedCell(row,col)
    }

}