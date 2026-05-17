package com.computerroom.monitoring.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.computerroom.monitoring.R
import com.computerroom.monitoring.databinding.FragmentHomeBinding
import com.computerroom.monitoring.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
    }

    private fun setupObservers() {
        viewModel.sensorData.observe(viewLifecycleOwner) { data ->
            binding.tvTemperatureValue.text = String.format("%.1f°", data.temperature)
            binding.tvHumidityValue.text = String.format("%.0f%%", data.humidity)

            binding.progressTempGauge.progress = data.temperature.toInt().coerceIn(0, 50)
            binding.progressHumidGauge.progress = data.humidity.toInt().coerceIn(0, 100)

            when {
                data.temperature > 40 || data.temperature < 10 -> {
                    binding.tvTempStatus.text = "Cảnh báo"
                    binding.tvTempStatus.setTextColor(
                        ContextCompat.getColor(requireContext(), R.color.warning_red)
                    )
                }
                else -> {
                    binding.tvTempStatus.text = "Bình thường"
                    binding.tvTempStatus.setTextColor(
                        ContextCompat.getColor(requireContext(), R.color.primary)
                    )
                }
            }

            when {
                data.humidity > 80 || data.humidity < 30 -> {
                    binding.tvHumidStatus.text = "Cảnh báo"
                    binding.tvHumidStatus.setTextColor(
                        ContextCompat.getColor(requireContext(), R.color.warning_red)
                    )
                }
                else -> {
                    binding.tvHumidStatus.text = "Bình thường"
                    binding.tvHumidStatus.setTextColor(
                        ContextCompat.getColor(requireContext(), R.color.primary)
                    )
                }
            }

            if (data.timestamp > 0) {
                val sdf = SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault())
                binding.tvLastUpdate.text = "Cập nhật lần cuối: ${sdf.format(Date(data.timestamp * 1000L))}"
            }
        }

        viewModel.warningMessage.observe(viewLifecycleOwner) { warning ->
            if (warning.isNullOrEmpty()) {
                binding.cardWarning.visibility = View.GONE
            } else {
                binding.cardWarning.visibility = View.VISIBLE
                binding.tvWarningMessage.text = warning
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
