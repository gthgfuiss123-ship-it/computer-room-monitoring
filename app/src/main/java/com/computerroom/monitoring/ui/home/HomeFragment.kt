package com.computerroom.monitoring.ui.home

import android.app.AlertDialog
import android.graphics.Color
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.computerroom.monitoring.R
import com.computerroom.monitoring.data.model.ThresholdSettings
import com.computerroom.monitoring.databinding.FragmentHomeBinding
import com.computerroom.monitoring.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by activityViewModels()
    private var alertDialog: AlertDialog? = null
    private var currentRingtone: Ringtone? = null
    private var currentThresholds = ThresholdSettings()

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
        viewModel.thresholdSettings.observe(viewLifecycleOwner) { settings ->
            currentThresholds = settings
        }

        viewModel.sensorData.observe(viewLifecycleOwner) { data ->
            binding.tvTemperatureValue.text = String.format("%.1f°", data.temperature)
            binding.tvHumidityValue.text = String.format("%.0f%%", data.humidity)

            binding.progressTempGauge.progress = data.temperature.toInt().coerceIn(0, 50)
            binding.progressHumidGauge.progress = data.humidity.toInt().coerceIn(0, 100)

            val isTempWarning = data.temperature > currentThresholds.highTemp || data.temperature < currentThresholds.lowTemp
            val isHumidWarning = data.humidity > currentThresholds.highHumid || data.humidity < currentThresholds.lowHumid

            if (isTempWarning) {
                binding.tvTempStatus.text = "CANH BAO!"
                binding.tvTempStatus.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.warning_red)
                )
                binding.cardTempGauge.strokeColor = ContextCompat.getColor(requireContext(), R.color.warning_red)
                binding.cardTempGauge.strokeWidth = 4
            } else {
                binding.tvTempStatus.text = "Binh thuong"
                binding.tvTempStatus.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.primary)
                )
                binding.cardTempGauge.strokeColor = ContextCompat.getColor(requireContext(), R.color.border_color)
                binding.cardTempGauge.strokeWidth = 2
            }

            if (isHumidWarning) {
                binding.tvHumidStatus.text = "CANH BAO!"
                binding.tvHumidStatus.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.warning_red)
                )
                binding.cardHumidGauge.strokeColor = ContextCompat.getColor(requireContext(), R.color.warning_red)
                binding.cardHumidGauge.strokeWidth = 4
            } else {
                binding.tvHumidStatus.text = "Binh thuong"
                binding.tvHumidStatus.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.primary)
                )
                binding.cardHumidGauge.strokeColor = ContextCompat.getColor(requireContext(), R.color.border_color)
                binding.cardHumidGauge.strokeWidth = 2
            }

            binding.tvThresholdTemp.text = "Nguong: <${currentThresholds.lowTemp.toInt()}°C hoac >${currentThresholds.highTemp.toInt()}°C"
            binding.tvThresholdHumid.text = "Nguong: <${currentThresholds.lowHumid.toInt()}% hoac >${currentThresholds.highHumid.toInt()}%"

            if (data.timestamp > 0) {
                val sdf = SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault())
                binding.tvLastUpdate.text = "Cap nhat lan cuoi: ${sdf.format(Date(data.timestamp * 1000L))}"
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

        viewModel.criticalAlert.observe(viewLifecycleOwner) { alertMessage ->
            if (!alertMessage.isNullOrEmpty() && alertDialog?.isShowing != true) {
                showCriticalAlertDialog(alertMessage)
                triggerAlertFeedback()
                viewModel.clearCriticalAlert()
            }
        }
    }

    private fun showCriticalAlertDialog(message: String) {
        alertDialog?.dismiss()

        val titleView = TextView(requireContext()).apply {
            text = "CANH BAO NGHIEM TRONG!"
            setTextColor(Color.WHITE)
            textSize = 20f
            setPadding(48, 32, 48, 16)
            setBackgroundColor(ContextCompat.getColor(context, R.color.warning_red))
            setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_warning, 0, 0, 0)
            compoundDrawablePadding = 16
            compoundDrawableTintList = android.content.res.ColorStateList.valueOf(Color.WHITE)
        }

        alertDialog = AlertDialog.Builder(requireContext(), R.style.CriticalAlertDialog)
            .setCustomTitle(titleView)
            .setMessage("$message\n\nVui long kiem tra he thong ngay lap tuc!")
            .setPositiveButton("DA HIEU") { dialog, _ ->
                currentRingtone?.stop()
                dialog.dismiss()
            }
            .setCancelable(false)
            .create()

        alertDialog?.show()

        alertDialog?.getButton(AlertDialog.BUTTON_POSITIVE)?.let { button ->
            button.setTextColor(ContextCompat.getColor(requireContext(), R.color.warning_red))
            button.textSize = 16f
        }
    }

    private fun triggerAlertFeedback() {
        try {
            val vibrator = requireContext().getSystemService(android.content.Context.VIBRATOR_SERVICE) as? Vibrator
            vibrator?.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } catch (_: Exception) { }

        try {
            currentRingtone?.stop()
            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            currentRingtone = RingtoneManager.getRingtone(requireContext(), notification)
            currentRingtone?.play()
        } catch (_: Exception) { }
    }

    override fun onDestroyView() {
        currentRingtone?.stop()
        currentRingtone = null
        alertDialog?.dismiss()
        alertDialog = null
        super.onDestroyView()
        _binding = null
    }
}
