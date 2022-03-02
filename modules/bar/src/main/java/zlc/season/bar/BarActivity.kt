package zlc.season.bar

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import zlc.season.bar.databinding.ActivityBarBinding

class BarActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityBarBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}