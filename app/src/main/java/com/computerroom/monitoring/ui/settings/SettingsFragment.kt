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
import com.computerroom.monitoring.databinding.FragmentSettingsBinding
import com.computerroom.monitoring.ui.login.LoginActivity
import com.google.android.material.slider.Slider
import com.google.firebase.auth.FirebaseAuth

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

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
    }

    private fun setupUserInfo() {
        val user = FirebaseAuth.getInstance().currentUser
        binding.tvUserEmail.text = user?.email ?: "Chưa đăng nhập"
    }

    private fun setupSliders() {
        binding.sliderHighTemp.addOnChangeListener(Slider.OnChangeListener { _, value, _ ->
            binding.tvHighTempLabel.text = "Nhiệt độ cao: ${value.toInt()}°C"
        })

        binding.sliderLowTemp.addOnChangeListener(Slider.OnChangeListener { _, value, _ ->
            binding.tvLowTempLabel.text = "Nhiệt độ thấp: ${value.toInt()}°C"
        })

        binding.sliderHighHumid.addOnChangeListener(Slider.OnChangeListener { _, value, _ ->
            binding.tvHighHumidLabel.text = "Độ ẩm cao: ${value.toInt()}%"
        })

        binding.sliderLowHumid.addOnChangeListener(Slider.OnChangeListener { _, value, _ ->
            binding.tvLowHumidLabel.text = "Độ ẩm thấp: ${value.toInt()}%"
        })
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
