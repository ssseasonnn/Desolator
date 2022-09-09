package zlc.season.desolatordemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import zlc.season.desolator.Desolator
import zlc.season.desolator.PluginData
import zlc.season.desolatordemo.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnLoadLocalPlugin.setOnClickListener {
            Desolator.startPlugin(
                PluginData(
                    id = "101",
                    name = "foo",
                    version = "1",
                    entrance = "zlc.season.foo.FooFragment"
                )
            )
        }

        binding.btnLoadNetworkPlugin.setOnClickListener {
            val pluginData = PluginData(
                id = "101",
                name = "foo",
                version = "2",
                entrance = "zlc.season.foo.FooFragment",
                downloadUrl = "http://192.168.0.30:8000/plugin_foo/foo.apk"
            )

            Desolator.downloadPlugin(pluginData)
        }
    }
}