package com.computerroom.monitoring.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.computerroom.monitoring.R
import com.computerroom.monitoring.data.model.ThresholdSettings
import com.computerroom.monitoring.data.repository.FirebaseRepository
import com.computerroom.monitoring.databinding.FragmentSettingsBinding
import com.computerroom.monitoring.ui.login.LoginActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val repository = FirebaseRepository.getInstance()
    private var hasUnsavedChanges = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUserInfo()
        setupSliders()
        setupFrequencySpinner()
        setupTempUnitButtons()
        setupLogout()
        setupSaveButton()
        loadThresholds()
    }

    private fun setupUserInfo() {
        val user = FirebaseAuth.getInstance().currentUser
        binding.tvUserEmail.text = user?.email ?: "Chưa đăng nhập"
    }

    private fun setupSliders() {
        binding.sliderHighTemp.addOnChangeListener { _, value, fromUser ->
            binding.tvHighTempLabel.text = "Nhiệt độ cao: ${value.toInt()}°C"
            if (fromUser) {
                hasUnsavedChanges = true
                updateSaveButtonState()
            }
        }

        binding.sliderLowTemp.addOnChangeListener { _, value, fromUser ->
            binding.tvLowTempLabel.text = "Nhiệt độ thấp: ${value.toInt()}°C"
            if (fromUser) {
                hasUnsavedChanges = true
                updateSaveButtonState()
            }
        }

        binding.sliderHighHumid.addOnChangeListener { _, value, fromUser ->
            binding.tvHighHumidLabel.text = "Độ ẩm cao: ${value.toInt()}%"
            if (fromUser) {
                hasUnsavedChanges = true
                updateSaveButtonState()
            }
        }

        binding.sliderLowHumid.addOnChangeListener { _, value, fromUser ->
            binding.tvLowHumidLabel.text = "Độ ẩm thấp: ${value.toInt()}%"
            if (fromUser) {
                hasUnsavedChanges = true
                updateSaveButtonState()
            }
        }
    }

    private fun updateSaveButtonState() {
        if (hasUnsavedChanges) {
            binding.btnSaveThresholds.isEnabled = true
            binding.btnSaveThresholds.alpha = 1.0f
            binding.btnSaveThresholds.text = "Lưu ngưỡng cảnh báo"
        } else {
            binding.btnSaveThresholds.isEnabled = false
            binding.btnSaveThresholds.alpha = 0.6f
            binding.btnSaveThresholds.text = "Đã lưu"
        }
    }

    private fun setupSaveButton() {
        binding.btnSaveThresholds.setOnClickListener {
            val settings = ThresholdSettings(
                highTemp = binding.sliderHighTemp.value,
                lowTemp = binding.sliderLowTemp.value,
                highHumid = binding.sliderHighHumid.value,
                lowHumid = binding.sliderLowHumid.value
            )

            binding.btnSaveThresholds.isEnabled = false
            binding.btnSaveThresholds.text = "Đang lưu..."

            repository.saveThresholds(settings) { success, errorMsg ->
                activity?.runOnUiThread {
                    if (success) {
                        hasUnsavedChanges = false
                        updateSaveButtonState()
                        Snackbar.make(
                            binding.root,
                            "Đã lưu ngưỡng cảnh báo thành công!",
                            Snackbar.LENGTH_SHORT
                        ).setBackgroundTint(requireContext().getColor(R.color.primary))
                            .setTextColor(requireContext().getColor(R.color.white))
                            .show()
                    } else {
                        binding.btnSaveThresholds.isEnabled = true
                        binding.btnSaveThresholds.text = "Lưu ngưỡng cảnh báo"
                        Snackbar.make(
                            binding.root,
                            "Lỗi: ${errorMsg ?: "Không thể lưu"}",
                            Snackbar.LENGTH_LONG
                        ).setBackgroundTint(requireContext().getColor(R.color.warning_red))
                            .setTextColor(requireContext().getColor(R.color.white))
                            .show()
                    }
                }
            }
        }

        hasUnsavedChanges = false
        updateSaveButtonState()
    }

    private fun loadThresholds() {
        repository.loadThresholds()
        repository.thresholdSettings.observe(viewLifecycleOwner) { settings ->
            binding.sliderHighTemp.value = settings.highTemp.coerceIn(20f, 60f)
            binding.sliderLowTemp.value = settings.lowTemp.coerceIn(-10f, 30f)
            binding.sliderHighHumid.value = settings.highHumid.coerceIn(50f, 100f)
            binding.sliderLowHumid.value = settings.lowHumid.coerceIn(0f, 50f)

            binding.tvHighTempLabel.text = "Nhiệt độ cao: ${settings.highTemp.toInt()}°C"
            binding.tvLowTempLabel.text = "Nhiệt độ thấp: ${settings.lowTemp.toInt()}°C"
            binding.tvHighHumidLabel.text = "Độ ẩm cao: ${settings.highHumid.toInt()}%"
            binding.tvLowHumidLabel.text = "Độ ẩm thấp: ${settings.lowHumid.toInt()}%"

            hasUnsavedChanges = false
            updateSaveButtonState()
        }
    }

    private fun setupFrequencySpinner() {
        val frequencies = arrayOf("Mỗi 2 giây", "Mỗi 5 giây", "Mỗi 10 giây", "Mỗi 30 giây")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, frequencies)
        binding.spinnerFrequency.adapter = adapter
        binding.spinnerFrequency.setSelection(1)
    }

    private fun setupTempUnitButtons() {
        binding.btnCelsius.setOnClickListener {
            binding.btnCelsius.setBackgroundColor(requireContext().getColor(R.color.primary))
            binding.btnCelsius.setTextColor(requireContext().getColor(R.color.white))
            binding.btnFahrenheit.setBackgroundColor(requireContext().getColor(R.color.border_color))
            binding.btnFahrenheit.setTextColor(requireContext().getColor(R.color.text_secondary))
        }

        binding.btnFahrenheit.setOnClickListener {
            binding.btnFahrenheit.setBackgroundColor(requireContext().getColor(R.color.primary))
            binding.btnFahrenheit.setTextColor(requireContext().getColor(R.color.white))
            binding.btnCelsius.setBackgroundColor(requireContext().getColor(R.color.border_color))
            binding.btnCelsius.setTextColor(requireContext().getColor(R.color.text_secondary))
        }
    }

    private fun setupLogout() {
        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }

        binding.btnChangePassword.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser
            user?.email?.let { email ->
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(requireContext(), "Đã gửi email đổi mật khẩu", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "Lỗi: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
