// File: app/src/main/java/com/wpc/servicesync/adapters/MealSelectionAdapter.kt
package com.wpc.servicesync.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.wpc.servicesync.R
import com.wpc.servicesync.models.MealType

class MealSelectionAdapter(
    private val mealTypes: List<MealType>,
    private var selectedMealType: MealType,
    private val onMealSelected: (MealType) -> Unit
) : RecyclerView.Adapter<MealSelectionAdapter.MealViewHolder>() {

    class MealViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mealCard: CardView = itemView.findViewById(R.id.mealCard)
        val mealIcon: TextView = itemView.findViewById(R.id.mealIcon)
        val mealTitle: TextView = itemView.findViewById(R.id.mealTitle)
        val mealTimeRange: TextView = itemView.findViewById(R.id.mealTimeRange)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_meal_selection, parent, false)
        return MealViewHolder(view)
    }

    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        val mealType = mealTypes[position]
        val context = holder.itemView.context

        // Set meal icon
        holder.mealIcon.text = getMealIcon(mealType)

        // Set meal title
        holder.mealTitle.text = mealType.displayName

        // Set meal time range
        holder.mealTimeRange.text = getMealTimeRange(mealType)

        // Update selection state
        val isSelected = mealType == selectedMealType
        if (isSelected) {
            holder.mealCard.setCardBackgroundColor(
                ContextCompat.getColor(context, R.color.primary_color)
            )
            holder.mealTitle.setTextColor(
                ContextCompat.getColor(context, R.color.white)
            )
            holder.mealTimeRange.setTextColor(
                ContextCompat.getColor(context, R.color.white)
            )
        } else {
            holder.mealCard.setCardBackgroundColor(
                ContextCompat.getColor(context, R.color.white)
            )
            holder.mealTitle.setTextColor(
                ContextCompat.getColor(context, R.color.text_primary)
            )
            holder.mealTimeRange.setTextColor(
                ContextCompat.getColor(context, R.color.text_secondary)
            )
        }

        // Set click listener
        holder.mealCard.setOnClickListener {
            selectedMealType = mealType
            onMealSelected(mealType)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int = mealTypes.size

    private fun getMealIcon(mealType: MealType): String {
        return mealType.icon
    }

    private fun getMealTimeRange(mealType: MealType): String {
        return mealType.timeRange
    }

    fun updateSelection(newSelectedMealType: MealType) {
        selectedMealType = newSelectedMealType
        notifyDataSetChanged()
    }
}