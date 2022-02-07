package zlc.season.desolatordemo

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import zlc.season.desolator.*


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        println(R.layout.activity_main)

        val btnHello = findViewById<Button>(R.id.btn_hello)
        btnHello.setOnClickListener {
            Desolator.startPlugin(90001)

//            val fragment = classLoader.loadClass("com.example.plugina.PluginFragment").newInstance() as Fragment
//            supportFragmentManager.beginTransaction()
//                .add(R.id.container, fragment)
//                .commit()
        }
    }
}