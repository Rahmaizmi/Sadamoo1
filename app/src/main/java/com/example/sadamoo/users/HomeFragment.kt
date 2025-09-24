package com.example.sadamoo.users.fragments
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sadamoo.databinding.FragmentHomeBinding
import com.example.sadamoo.users.adapters.CattleTypeAdapter
import com.example.sadamoo.users.models.CattleType

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCattleTypesRecyclerView()
        loadUserName()
    }

    private fun setupCattleTypesRecyclerView() {
        val cattleTypes = listOf(
            CattleType("Brahman", "drawable/sapi_brahman"),
            CattleType("Limosin", "drawable/sapi_limosin"),
            CattleType("Simental", "drawable/sapi_simental"),
            CattleType("Angus", "drawable/sapi_angus"),
            CattleType("Holstein", "drawable/sapi_holstein"),
            CattleType("Jersey", "drawable/sapi_jersey"),
            CattleType("Hereford", "drawable/sapi_hereford"),
            CattleType("Charolais", "drawable/sapi_charolais"),
            CattleType("Shorthorn", "drawable/sapi_shorthorn"),
            CattleType("Gelbvieh", "drawable/sapi_gelbvieh")
        )

        val adapter = CattleTypeAdapter(cattleTypes)
        binding.rvCattleTypes.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            this.adapter = adapter
        }
    }

    private fun loadUserName() {
        // TODO: Get user name from Firebase Auth or SharedPreferences
        binding.tvWelcome.text = "Selamat Datang John Doe!"
    }
}
