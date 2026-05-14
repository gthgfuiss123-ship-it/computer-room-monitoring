package com.computerroom.monitoring.ui.control

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.computerroom.monitoring.databinding.FragmentControlBinding
import com.computerroom.monitoring.viewmodel.ControlViewModel

class ControlFragment : Fragment() {

    private var _binding: FragmentControlBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ControlViewModel by viewModels()
    private var isUpdatingFromServer = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentControlBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        viewModel.deviceStatus.observe(viewLifecycleOwner) { status ->
            isUpdatingFromServer = true
            binding.switchFan.isChecked = status.fan
            binding.switchLight.isChecked = status.light
            binding.switchBuzzer.isChecked = status.buzzer
            isUpdatingFromServer = false

            binding.tvFanStatus.text = if (status.fan) "Đang BẬT" else "Đang TẮT"
            binding.tvLightStatus.text = if (status.light) "Đang BẬT" else "Đang TẮT"
            binding.tvBuzzerStatus.text = if (status.buzzer) "Đang BẬT" else "Đang TẮT"
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupListeners() {
        binding.switchFan.setOnCheckedChangeListener { _, isChecked ->
            if (!isUpdatingFromServer) viewModel.toggleFan(isChecked)
        }

        binding.switchLight.setOnCheckedChangeListener { _, isChecked ->
            if (!isUpdatingFromServer) viewModel.toggleLight(isChecked)
        }

        binding.switchBuzzer.setOnCheckedChangeListener { _, isChecked ->
            if (!isUpdatingFromServer) viewModel.toggleBuzzer(isChecked)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
