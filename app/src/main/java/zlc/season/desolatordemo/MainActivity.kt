package zlc.season.desolatordemo

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import zlc.season.desolator.Desolator
import zlc.season.desolator.PluginData


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnPluginFoo = findViewById<Button>(R.id.btn_plugin_foo)
        val btnPluginBar = findViewById<Button>(R.id.btn_plugin_bar)

        btnPluginFoo.setOnClickListener {
            Desolator.startPlugin(
                PluginData(
                    "101574",
                    "foo",
                    "1",
                    "zlc.season.foo.FooFragment"
                )
            )
        }

        btnPluginBar.setOnClickListener {
            Desolator.startPlugin(
                PluginData(
                    "97299",
                    "bar",
                    "1",
                    "zlc.season.bar.BarFragment"
                )
            )
        }
    }
}