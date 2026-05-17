package com.computerroom.monitoring.ui.sensor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.computerroom.monitoring.R
import com.computerroom.monitoring.data.model.ThresholdSettings
import com.computerroom.monitoring.databinding.FragmentSensorBinding
import com.computerroom.monitoring.viewmodel.HomeViewModel

class SensorFragment : Fragment() {

    private var _binding: FragmentSensorBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by activityViewModels()
    private var currentThresholds = ThresholdSettings()

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
        viewModel.thresholdSettings.observe(viewLifecycleOwner) { settings ->
            currentThresholds = settings
        }

        viewModel.tempMin.observe(viewLifecycleOwner) { min ->
            binding.tvTempMin.text = String.format("%.0f°", min)
        }
        viewModel.tempMax.observe(viewLifecycleOwner) { max ->
            binding.tvTempMax.text = String.format("%.0f°", max)
        }
        viewModel.humidMin.observe(viewLifecycleOwner) { min ->
            binding.tvHumidMin.text = String.format("%.0f%%", min)
        }
        viewModel.humidMax.observe(viewLifecycleOwner) { max ->
            binding.tvHumidMax.text = String.format("%.0f%%", max)
        }

        viewModel.sensorData.observe(viewLifecycleOwner) { data ->
            binding.tvTempCurrent.text = String.format("%.1f°", data.temperature)
            binding.tvHumidCurrent.text = String.format("%.0f%%", data.humidity)

            binding.progressTemp.progress = data.temperature.toInt().coerceIn(0, 60)
            binding.progressHumid.progress = data.humidity.toInt().coerceIn(0, 100)

            if (data.temperature > currentThresholds.highTemp || data.temperature < currentThresholds.lowTemp) {
                binding.tvTempStatusDetail.text = "Canh bao"
                binding.tvTempStatusDetail.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.warning_red)
                )
            } else {
                binding.tvTempStatusDetail.text = "Binh thuong"
                binding.tvTempStatusDetail.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.primary)
                )
            }

            if (data.humidity > currentThresholds.highHumid || data.humidity < currentThresholds.lowHumid) {
                binding.tvHumidStatusDetail.text = "Canh bao"
                binding.tvHumidStatusDetail.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.warning_red)
                )
            } else {
                binding.tvHumidStatusDetail.text = "Binh thuong"
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
