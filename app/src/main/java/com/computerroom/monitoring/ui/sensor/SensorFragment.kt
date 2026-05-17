package com.computerroom.monitoring.ui.sensor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.computerroom.monitoring.R
import com.computerroom.monitoring.databinding.FragmentSensorBinding
import com.computerroom.monitoring.viewmodel.HomeViewModel

class SensorFragment : Fragment() {

    private var _binding: FragmentSensorBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSensorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
    }

    private fun setupObservers() {
        viewModel.sensorData.observe(viewLifecycleOwner) { data ->
            binding.tvTempCurrent.text = String.format("%.1f°", data.temperature)
            binding.tvHumidCurrent.text = String.format("%.0f%%", data.humidity)

            binding.progressTemp.progress = data.temperature.toInt().coerceIn(0, 60)
            binding.progressHumid.progress = data.humidity.toInt().coerceIn(0, 100)

            binding.tvTempMin.text = String.format("%.0f°", data.temperature - 5)
            binding.tvTempMax.text = String.format("%.0f°", data.temperature + 3)
            binding.tvHumidMin.text = String.format("%.0f%%", data.humidity - 10)
            binding.tvHumidMax.text = String.format("%.0f%%", data.humidity + 5)

            if (data.temperature > 40 || data.temperature < 10) {
                binding.tvTempStatusDetail.text = "Cảnh báo"
                binding.tvTempStatusDetail.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.warning_red)
                )
            } else {
                binding.tvTempStatusDetail.text = "Bình thường"
                binding.tvTempStatusDetail.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.primary)
                )
            }

            if (data.humidity > 80 || data.humidity < 30) {
                binding.tvHumidStatusDetail.text = "Cảnh báo"
                binding.tvHumidStatusDetail.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.warning_red)
                )
            } else {
                binding.tvHumidStatusDetail.text = "Bình thường"
                binding.tvHumidStatusDetail.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.primary)
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
