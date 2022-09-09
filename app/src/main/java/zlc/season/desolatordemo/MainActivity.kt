package zlc.season.desolatordemo

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import zlc.season.desolator.Desolator
import zlc.season.desolator.PluginData
import zlc.season.desolatordemo.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        binding.btnPluginFoo.setOnClickListener {
            Desolator.startPlugin(
                PluginData(
                    "101574",
                    "foo",
                    "1",
                    "zlc.season.foo.FooFragment"
                )
            )
        }

        binding.btnPluginBar.setOnClickListener {
            Desolator.startPlugin(
                PluginData(
                    "97299",
                    "bar",
                    "1",
                    "zlc.season.bar.BarFragment"
                )
            )
        }


        binding.btnPluginFromNetwork.setOnClickListener {
            val pluginData = PluginData(
                "101574",
                "foo",
                "2",
                "zlc.season.foo.FooFragment",
                downloadUrl = "http://192.168.0.10:8000/plugin_foo/foo.apk"
            )

            Desolator.downloadPlugin(pluginData)
//            Desolator.installPlugin(
//                pluginData,
//                onSuccess = {
//                    Desolator.startPlugin(pluginData)
//                }
//            )
        }
    }
}