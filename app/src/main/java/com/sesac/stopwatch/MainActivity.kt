package com.sesac.stopwatch

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.sesac.stopwatch.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

const val TIMER_UPDATE_INTERVAL = 50L

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private var isWorking = false // 스탑 워치 시작 유무
    private var timeRunInMillis = 0L // 총 작동 시간(시작 이후의 시간, pause 시간은 포함X)
    private var recordInMillis = 0L // '구간 기록' 눌렀을 때 총 작동 시간
    private var timeRun = 0L // 'pause'전 총 작동 시간
    private var lapTime = 0L // 'start'후 총 작동 시간

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater).also {
            setContentView(it.root)
        }
        // 스탑워치 변수들 초기화
        initialValues()

        binding.btnStart.setOnClickListener {
            startTimer()
            binding.btnStart.visibility = View.INVISIBLE
            binding.btnPause.visibility = View.VISIBLE
            binding.btnReset.visibility = View.INVISIBLE
            binding.btnTerm.visibility = View.VISIBLE
        }

        binding.btnPause.setOnClickListener {
            pauseTimer()
            binding.btnStart.visibility = View.VISIBLE
            binding.btnPause.visibility = View.INVISIBLE
            binding.btnReset.visibility = View.VISIBLE
            binding.btnTerm.visibility = View.INVISIBLE
        }

        binding.btnReset.setOnClickListener {
            binding.btnReset.visibility = View.INVISIBLE
            binding.btnTerm.visibility = View.VISIBLE
            binding.diff.visibility = View.INVISIBLE

            initialValues()
            binding.layoutTerm.removeAllViews()
            binding.layoutTerm.invalidate()
        }

        binding.btnTerm.setOnClickListener {
            val labTimeTV = TextView(this).apply {
                textSize = 20f
            }
            recordInMillis = timeRunInMillis
            binding.diff.visibility = View.VISIBLE
            with(labTimeTV) {
                text = binding.tvTime.text
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also {
                    it.setMargins(0, 15, 0, 15)
                }
                binding.layoutTerm.addView(labTimeTV, 0)
            }
        }
    }

    /**
     * Initial values
     * 스탑워치 작동에 필요한 변수들 초기화 메서드
     * 제일 처음 앱을 켰을 때, rest 버튼을 눌렀을 때 작동
     */
    private fun initialValues() {
        pauseTimer()
        timeRunInMillis = 0L
        recordInMillis = 0L
        lapTime = 0L
        timeRun = 0L
        binding.tvTime.text = getFormattedStopWatchTime(timeRunInMillis)
    }

    /**
     * Pause timer
     * 스탑워치를 멈추는 메서드
     * 중치 버튼을 눌렀을 때 작동
     */
    private fun pauseTimer() {
        isWorking = false
    }

    /**
     * Start timer
     * 스탑워치를 작동하는 메서드
     */
    private fun startTimer() {
        // 스탑워치 작동
        isWorking = true
        // 현재 시간
        val timeStarted = System.currentTimeMillis()
        CoroutineScope(Dispatchers.Main).launch {
            while (isWorking) {
                // 현재 시간과 start 버튼을 눌렀을 때의 시간의 차이 (start버튼을 누르고 나서 얼마만큼 지났는지)
                lapTime = System.currentTimeMillis() - timeStarted
                // 총 작동 시간 = 'pause'전 총 작동 시간 + 'start'후 총 작동 시간 ('pause'를 누르기 전엔 timeRun은 0L이다.)
                timeRunInMillis = (timeRun + lapTime)
                // 0.05초 단위로 갱신
                delay(TIMER_UPDATE_INTERVAL)
                // 총 작동 시간 실시간 갱신
                binding.tvTime.text = getFormattedStopWatchTime(timeRunInMillis)
                // 구간 기록이 있을 시, 구간 기록과 총 작동 시간과 차이 실시간 갱신
                if (recordInMillis != 0L) binding.diff.text = getFormattedStopWatchTime(timeRunInMillis - recordInMillis)

            }
            // 'pause'후 총 작동 시간 갱신
            timeRun += lapTime
        }
    }

    /**
     * getFormattedStopWatchTime
     * 밀리세컨드 시간 단위 변경 메서드
     *
     * @param ms, 밀리세컨드를 입력 받으면
     * @return 시,분,초,밀리초 단위로 바꿔서 String으로 반환 해준다.
     */
    private fun getFormattedStopWatchTime(ms: Long): String {
        var milliseconds = ms
        val hours = TimeUnit.MILLISECONDS.toHours(milliseconds) // '시' 단위
        milliseconds -= TimeUnit.HOURS.toMillis(hours)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds) // '분' 단위
        milliseconds -= TimeUnit.MINUTES.toMillis(minutes)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) // '초' 단위
        milliseconds -= TimeUnit.SECONDS.toMillis(seconds)

        // 'milli' 단위로 볼 때
        milliseconds /= 10
        return "${if (hours < 10) "0" else ""}$hours:" + // 한자리 수 일땐 앞에 '0'이 필요함
                "${if (minutes < 10) "0" else ""}$minutes:" +
                "${if (seconds < 10) "0" else ""}$seconds:" +
                "${if (milliseconds < 10) "0" else ""}$milliseconds"
    }
}

