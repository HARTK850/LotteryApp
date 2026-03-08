package com.example.lotteryapp

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.lotteryapp.databinding.ActivityMainBinding
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Random

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val names = mutableListOf<String>()
    private val drawnWinners = mutableSetOf<String>() // למנוע חזרות

    private val pickFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                readNamesFromFile(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnPickFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "text/plain"
            }
            pickFileLauncher.launch(intent)
        }

        binding.btnDraw.setOnClickListener {
            loadNamesFromEditText()
            if (names.isEmpty()) {
                Toast.makeText(this, "אין שמות להגרלה!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            drawWinner()
        }

        binding.btnNextWinner.setOnClickListener {
            drawWinner()
        }
    }

    private fun loadNamesFromEditText() {
        val input = binding.editNames.text.toString().trim()
        if (input.isNotEmpty()) {
            names.addAll(input.split(",").map { it.trim() }.filter { it.isNotEmpty() && !names.contains(it) })
            binding.editNames.text.clear()
        }
    }

    private fun readNamesFromFile(uri: Uri) {
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            val fileName = cursor.getString(nameIndex)
            Toast.makeText(this, "קובץ נבחר: $fileName", Toast.LENGTH_SHORT).show()
        }

        contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                names.clear()
                reader.forEachLine { line ->
                    val trimmed = line.trim()
                    if (trimmed.isNotEmpty()) names.add(trimmed)
                }
            }
        }
    }

    private fun drawWinner() {
        val remainingNames = names.filter { !drawnWinners.contains(it) }
        if (remainingNames.isEmpty()) {
            Toast.makeText(this, "אין יותר שמות!", Toast.LENGTH_SHORT).show()
            return
        }

        val winner = remainingNames[Random().nextInt(remainingNames.size)]
        drawnWinners.add(winner)
        binding.tvWinner.text = "הזוכה: $winner"

        // אנימציה מטורפת: סיבוב + הגדלה
        val scaleX = ObjectAnimator.ofFloat(binding.tvWinner, View.SCALE_X, 0.5f, 1.5f, 1f)
        val scaleY = ObjectAnimator.ofFloat(binding.tvWinner, View.SCALE_Y, 0.5f, 1.5f, 1f)
        val rotate = ObjectAnimator.ofFloat(binding.tvWinner, View.ROTATION, 0f, 360f)
        AnimatorSet().apply {
            playTogether(scaleX, scaleY, rotate)
            duration = 1000
            start()
        }

        // אנימציה לכפתורים
        animateButton(binding.btnDraw)
        binding.btnNextWinner.visibility = View.VISIBLE
        animateButton(binding.btnNextWinner)
    }

    private fun animateButton(button: View) {
        val scaleX = ObjectAnimator.ofFloat(button, View.SCALE_X, 1f, 1.2f, 1f)
        val scaleY = ObjectAnimator.ofFloat(button, View.SCALE_Y, 1f, 1.2f, 1f)
        AnimatorSet().apply {
            playTogether(scaleX, scaleY)
            duration = 300
            start()
        }
    }
}
