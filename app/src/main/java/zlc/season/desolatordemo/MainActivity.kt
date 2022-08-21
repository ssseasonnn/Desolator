package zlc.season.desolatordemo

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import zlc.season.desolator.Desolator
import zlc.season.desolator.PluginData


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Desolator.installInternalPlugin()

        setContentView(R.layout.activity_main)
        println(R.layout.activity_main)

        val btnHello = findViewById<Button>(R.id.btn_hello)
        btnHello.setOnClickListener {
//            Desolator.startPlugin(90001)
            Desolator.startPlugin(PluginData(1, "foo-debug", 1, "zlc.season.foo.FooFragment"))

//            val fragment = classLoader.loadClass("com.example.plugina.PluginFragment").newInstance() as Fragment
//            supportFragmentManager.beginTransaction()
//                .add(R.id.container, fragment)
//                .commit()
        }
    }
}