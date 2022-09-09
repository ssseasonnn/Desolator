package zlc.season.foo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import zlc.season.foo.databinding.FragmentFooBinding

class FooFragment : Fragment() {
    var binding: FragmentFooBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return FragmentFooBinding.inflate(inflater, container, false).also { binding = it }.root
    }

}