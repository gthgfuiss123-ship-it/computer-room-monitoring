package com.computerroom.monitoring.ui.sensor

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.computerroom.monitoring.R
import com.computerroom.monitoring.data.model.SensorData
import com.computerroom.monitoring.data.model.ThresholdSettings
import com.computerroom.monitoring.databinding.FragmentSensorBinding
import com.computerroom.monitoring.viewmodel.HomeViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
        setupCharts()
        setupObservers()
    }

    private fun setupCharts() {
        val primaryColor = ContextCompat.getColor(requireContext(), R.color.primary)
        val accentColor = ContextCompat.getColor(requireContext(), R.color.accent)

        configureChart(binding.chartTemp, primaryColor, 0f, 60f)
        configureChart(binding.chartHumid, accentColor, 0f, 100f)
    }

    private fun configureChart(chart: LineChart, lineColor: Int, yMin: Float, yMax: Float) {
        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.setTouchEnabled(true)
        chart.setScaleEnabled(false)
        chart.setPinchZoom(false)
        chart.setDrawGridBackground(false)
        chart.setBackgroundColor(Color.TRANSPARENT)
        chart.animateX(500)
        chart.setNoDataText("Dang cho du lieu...")
        chart.setNoDataTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))

        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(true)
        xAxis.gridColor = ContextCompat.getColor(requireContext(), R.color.border_color)
        xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.text_secondary)
        xAxis.textSize = 10f
        xAxis.granularity = 1f
        xAxis.setLabelCount(5, false)
        xAxis.valueFormatter = object : ValueFormatter() {
            private val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            override fun getFormattedValue(value: Float): String {
                return sdf.format(Date(value.toLong()))
            }
        }

        val leftAxis = chart.axisLeft
        leftAxis.axisMinimum = yMin
        leftAxis.axisMaximum = yMax
        leftAxis.setDrawGridLines(true)
        leftAxis.gridColor = ContextCompat.getColor(requireContext(), R.color.border_color)
        leftAxis.textColor = ContextCompat.getColor(requireContext(), R.color.text_secondary)
        leftAxis.textSize = 10f

        chart.axisRight.isEnabled = false

        chart.setExtraOffsets(8f, 8f, 8f, 8f)
    }

    private fun updateChart(chart: LineChart, history: List<SensorData>, lineColor: Int, isTemperature: Boolean) {
        if (history.isEmpty()) return

        val entries = history.map { data ->
            val ts = if (data.timestamp > 0L) data.timestamp.toFloat() else System.currentTimeMillis().toFloat()
            val value = if (isTemperature) data.temperature else data.humidity
            Entry(ts, value)
        }.sortedBy { it.x }

        val dataSet = LineDataSet(entries, "").apply {
            color = lineColor
            lineWidth = 2.5f
            setDrawCircles(true)
            circleRadius = 3f
            setCircleColor(lineColor)
            setDrawCircleHole(true)
            circleHoleRadius = 1.5f
            circleHoleColor = Color.WHITE
            setDrawValues(false)
            setDrawFilled(true)
            fillColor = lineColor
            fillAlpha = 30
            mode = LineDataSet.Mode.CUBIC_BEZIER
            cubicIntensity = 0.2f
            setDrawHighlightIndicators(true)
            highLightColor = lineColor
            highlightLineWidth = 1f
        }

        chart.data = LineData(dataSet)
        chart.invalidate()
    }

    private fun setupObservers() {
        viewModel.thresholdSettings.observe(viewLifecycleOwner) { settings ->
            currentThresholds = settings
            if (_binding != null) {
                binding.tvThreshHighTemp.text = "${settings.highTemp.toInt()}\u00b0C"
                binding.tvThreshLowTemp.text = "${settings.lowTemp.toInt()}\u00b0C"
                binding.tvThreshHighHumid.text = "${settings.highHumid.toInt()}%"
                binding.tvThreshLowHumid.text = "${settings.lowHumid.toInt()}%"
            }
        }

        viewModel.tempMin.observe(viewLifecycleOwner) { min ->
            binding.tvTempMin.text = String.format("%.0f\u00b0", min)
        }
        viewModel.tempMax.observe(viewLifecycleOwner) { max ->
            binding.tvTempMax.text = String.format("%.0f\u00b0", max)
        }
        viewModel.humidMin.observe(viewLifecycleOwner) { min ->
            binding.tvHumidMin.text = String.format("%.0f%%", min)
        }
        viewModel.humidMax.observe(viewLifecycleOwner) { max ->
            binding.tvHumidMax.text = String.format("%.0f%%", max)
        }

        viewModel.sensorData.observe(viewLifecycleOwner) { data ->
            binding.tvTempCurrent.text = String.format("%.1f\u00b0", data.temperature)
            binding.tvHumidCurrent.text = String.format("%.0f%%", data.humidity)

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

        viewModel.sensorHistory.observe(viewLifecycleOwner) { history ->
            val primaryColor = ContextCompat.getColor(requireContext(), R.color.primary)
            val accentColor = ContextCompat.getColor(requireContext(), R.color.accent)
            updateChart(binding.chartTemp, history, primaryColor, true)
            updateChart(binding.chartHumid, history, accentColor, false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
