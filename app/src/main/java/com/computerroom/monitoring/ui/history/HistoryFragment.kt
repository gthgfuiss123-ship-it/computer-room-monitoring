package com.computerroom.monitoring.ui.history

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.computerroom.monitoring.databinding.FragmentHistoryBinding
import com.computerroom.monitoring.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HistoryViewModel by viewModels()
    private lateinit var adapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupDatePicker()
        setupObservers()
    }

    private fun setupRecyclerView() {
        adapter = HistoryAdapter()
        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHistory.adapter = adapter
    }

    private fun setupDatePicker() {
        binding.tvDatePicker.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    calendar.set(year, month, day)
                    binding.tvDatePicker.text = sdf.format(calendar.time)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupObservers() {
        viewModel.historyList.observe(viewLifecycleOwner) { records ->
            if (records.isNullOrEmpty()) {
                binding.rvHistory.visibility = View.GONE
                binding.layoutEmpty.visibility = View.VISIBLE
            } else {
                binding.rvHistory.visibility = View.VISIBLE
                binding.layoutEmpty.visibility = View.GONE
                adapter.submitList(records)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
