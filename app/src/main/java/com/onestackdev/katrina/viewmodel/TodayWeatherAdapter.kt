package com.onestackdev.katrina.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.onestackdev.katrina.databinding.ItemListWeatherTodayBinding
import com.onestackdev.katrina.model.Hour
import com.onestackdev.katrina.screens.activities.MainActivity
import com.onestackdev.katrina.utils.returnImageWeather
import com.onestackdev.katrina.utils.tempFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

class TodayWeatherAdapter @Inject constructor() :
    RecyclerView.Adapter<TodayWeatherAdapter.ViewHolder>() {

    private lateinit var binding: ItemListWeatherTodayBinding
    private lateinit var context: Context

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TodayWeatherAdapter.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        binding = ItemListWeatherTodayBinding.inflate(inflater, parent, false)
        context = parent.context
        return ViewHolder()
    }

    override fun onBindViewHolder(holder: TodayWeatherAdapter.ViewHolder, position: Int) {
        holder.set(differ.currentList[position])
        holder.setIsRecyclable(false)
    }

    override fun getItemCount(): Int = differ.currentList.size

    override fun getItemViewType(position: Int): Int = position

    inner class ViewHolder : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun set(item: Hour) {
            binding.apply {

                binding.tvTemp.text = tempFormat(item.temp_c.toString())

                val calendar = Calendar.getInstance(Locale.ROOT)
                calendar.timeInMillis = item.time_epoch.toLong() * 1000
                val formatDate = DateFormat.format("HH:mm", calendar).toString()

                binding.tvTime.text = formatDate

                Glide.with(context).load(
                    returnImageWeather(item.condition.code, item.is_day, MainActivity.activity)
                ).into(binding.imageWeatherStatus)
            }
        }
    }

    private val differCallback = object : DiffUtil.ItemCallback<Hour>() {
        override fun areItemsTheSame(
            oldItem: Hour,
            newItem: Hour
        ): Boolean {
            return oldItem.time == newItem.time
        }

        override fun areContentsTheSame(
            oldItem: Hour,
            newItem: Hour
        ): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

}