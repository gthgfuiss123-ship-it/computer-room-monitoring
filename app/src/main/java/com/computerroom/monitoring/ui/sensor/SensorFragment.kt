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
import com.computerroom.monitoring.viewmodel.SensorViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SensorFragment : Fragment() {

    private var _binding: FragmentSensorBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SensorViewModel by viewModels()

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
            // Temperature
            binding.tvTempValue.text = String.format("%.1f", data.temperature)
            binding.tvTempUnit.text = "°C"
            binding.progressTemp.progress = data.temperature.toInt().coerceIn(0, 60)

            when {
                data.temperature > 40 -> {
                    binding.tvTempStatus.text = "Quá cao!"
                    binding.tvTempStatus.setTextColor(
                        ContextCompat.getColor(requireContext(), R.color.warning_red)
                    )
                }
                data.temperature < 10 -> {
                    binding.tvTempStatus.text = "Quá thấp!"
                    binding.tvTempStatus.setTextColor(
                        ContextCompat.getColor(requireContext(), R.color.info_blue)
                    )
                }
                else -> {
                    binding.tvTempStatus.text = "Bình thường"
                    binding.tvTempStatus.setTextColor(
                        ContextCompat.getColor(requireContext(), R.color.safe_green)
                    )
                }
            }

            // Humidity
            binding.tvHumidValue.text = String.format("%.0f", data.humidity)
            binding.tvHumidUnit.text = "%"
            binding.progressHumid.progress = data.humidity.toInt().coerceIn(0, 100)

            when {
                data.humidity > 80 -> {
                    binding.tvHumidStatus.text = "Quá cao!"
                    binding.tvHumidStatus.setTextColor(
                        ContextCompat.getColor(requireContext(), R.color.warning_red)
                    )
                }
                data.humidity < 30 -> {
                    binding.tvHumidStatus.text = "Quá thấp!"
                    binding.tvHumidStatus.setTextColor(
                        ContextCompat.getColor(requireContext(), R.color.warning_orange)
                    )
                }
                else -> {
                    binding.tvHumidStatus.text = "Bình thường"
                    binding.tvHumidStatus.setTextColor(
                        ContextCompat.getColor(requireContext(), R.color.safe_green)
                    )
                }
            }

            // Motion
            if (data.motion) {
                binding.tvMotionValue.text = "CÓ"
                binding.tvMotionDetail.text = "Phát hiện chuyển động trong phòng!"
                binding.ivMotionIcon.setColorFilter(
                    ContextCompat.getColor(requireContext(), R.color.warning_red)
                )
            } else {
                binding.tvMotionValue.text = "KHÔNG"
                binding.tvMotionDetail.text = "Không phát hiện chuyển động"
                binding.ivMotionIcon.setColorFilter(
                    ContextCompat.getColor(requireContext(), R.color.safe_green)
                )
            }

            // Last update
            if (data.timestamp > 0) {
                val sdf = SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault())
                binding.tvLastUpdate.text = "Cập nhật lần cuối: ${sdf.format(Date(data.timestamp * 1000L))}"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
