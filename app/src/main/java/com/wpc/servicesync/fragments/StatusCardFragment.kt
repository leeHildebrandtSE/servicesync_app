package com.wpc.servicesync.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.wpc.servicesync.R

class StatusCardFragment : Fragment() {

    private var _binding: View? = null
    private val binding get() = _binding!!

    private lateinit var statusCard: CardView
    private lateinit var statusIcon: TextView
    private lateinit var statusTitle: TextView
    private lateinit var statusMessage: TextView
    private lateinit var statusTime: TextView

    private var cardType: StatusType = StatusType.INFO
    private var title: String = ""
    private var message: String = ""
    private var time: String = ""
    private var icon: String = "ℹ️"

    enum class StatusType {
        SUCCESS,
        WARNING,
        ERROR,
        INFO,
        PROGRESS
    }

    companion object {
        private const val ARG_TYPE = "status_type"
        private const val ARG_TITLE = "status_title"
        private const val ARG_MESSAGE = "status_message"
        private const val ARG_TIME = "status_time"
        private const val ARG_ICON = "status_icon"

        fun newInstance(
            type: StatusType,
            title: String,
            message: String,
            time: String = "",
            icon: String = ""
        ): StatusCardFragment {
            val fragment = StatusCardFragment()
            val args = Bundle().apply {
                putString(ARG_TYPE, type.name)
                putString(ARG_TITLE, title)
                putString(ARG_MESSAGE, message)
                putString(ARG_TIME, time)
                putString(ARG_ICON, icon)
            }
            fragment.arguments = args
            return fragment
        }

        // Factory methods for common status types
        fun createSuccessCard(title: String, message: String, time: String = ""): StatusCardFragment {
            return newInstance(StatusType.SUCCESS, title, message, time, "✅")
        }

        fun createWarningCard(title: String, message: String, time: String = ""): StatusCardFragment {
            return newInstance(StatusType.WARNING, title, message, time, "⚠️")
        }

        fun createErrorCard(title: String, message: String, time: String = ""): StatusCardFragment {
            return newInstance(StatusType.ERROR, title, message, time, "❌")
        }

        fun createInfoCard(title: String, message: String, time: String = ""): StatusCardFragment {
            return newInstance(StatusType.INFO, title, message, time, "ℹ️")
        }

        fun createProgressCard(title: String, message: String, time: String = ""): StatusCardFragment {
            return newInstance(StatusType.PROGRESS, title, message, time, "🔄")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let { args ->
            cardType = StatusType.valueOf(
                args.getString(ARG_TYPE, StatusType.INFO.name)
            )
            title = args.getString(ARG_TITLE, "")
            message = args.getString(ARG_MESSAGE, "")
            time = args.getString(ARG_TIME, "")
            icon = args.getString(ARG_ICON, "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = inflater.inflate(R.layout.fragment_status_card, container, false)
        return binding
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        updateCardAppearance()
        updateContent()
    }

    private fun initViews() {
        statusCard = binding.findViewById(R.id.statusCard)
        statusIcon = binding.findViewById(R.id.statusIcon)
        statusTitle = binding.findViewById(R.id.statusTitle)
        statusMessage = binding.findViewById(R.id.statusMessage)
        statusTime = binding.findViewById(R.id.statusTime)
    }

    private fun updateCardAppearance() {
        val context = requireContext()

        when (cardType) {
            StatusType.SUCCESS -> {
                statusCard.setCardBackgroundColor(
                    ContextCompat.getColor(context, R.color.success_light)
                )
                statusTitle.setTextColor(
                    ContextCompat.getColor(context, R.color.success_dark)
                )
                statusMessage.setTextColor(
                    ContextCompat.getColor(context, R.color.success_dark)
                )
            }
            StatusType.WARNING -> {
                statusCard.setCardBackgroundColor(
                    ContextCompat.getColor(context, R.color.warning_light)
                )
                statusTitle.setTextColor(
                    ContextCompat.getColor(context, R.color.warning_dark)
                )
                statusMessage.setTextColor(
                    ContextCompat.getColor(context, R.color.warning_dark)
                )
            }
            StatusType.ERROR -> {
                statusCard.setCardBackgroundColor(
                    ContextCompat.getColor(context, R.color.error_light)
                )
                statusTitle.setTextColor(
                    ContextCompat.getColor(context, R.color.error_dark)
                )
                statusMessage.setTextColor(
                    ContextCompat.getColor(context, R.color.error_dark)
                )
            }
            StatusType.INFO -> {
                statusCard.setCardBackgroundColor(
                    ContextCompat.getColor(context, R.color.info_light)
                )
                statusTitle.setTextColor(
                    ContextCompat.getColor(context, R.color.info_dark)
                )
                statusMessage.setTextColor(
                    ContextCompat.getColor(context, R.color.info_dark)
                )
            }
            StatusType.PROGRESS -> {
                statusCard.setCardBackgroundColor(
                    ContextCompat.getColor(context, R.color.progress_light)
                )
                statusTitle.setTextColor(
                    ContextCompat.getColor(context, R.color.progress_dark)
                )
                statusMessage.setTextColor(
                    ContextCompat.getColor(context, R.color.progress_dark)
                )
            }
        }
    }

    private fun updateContent() {
        // Set icon with fallback
        val displayIcon = if (icon.isNotEmpty()) {
            icon
        } else {
            getDefaultIcon()
        }
        statusIcon.text = displayIcon

        // Set title
        statusTitle.text = title

        // Set message
        statusMessage.text = message

        // Set time (hide if empty)
        if (time.isNotEmpty()) {
            statusTime.text = time
            statusTime.visibility = View.VISIBLE
        } else {
            statusTime.visibility = View.GONE
        }
    }

    private fun getDefaultIcon(): String {
        return when (cardType) {
            StatusType.SUCCESS -> "✅"
            StatusType.WARNING -> "⚠️"
            StatusType.ERROR -> "❌"
            StatusType.INFO -> "ℹ️"
            StatusType.PROGRESS -> "🔄"
        }
    }

    // Public methods to update content dynamically
    fun updateStatus(
        newTitle: String? = null,
        newMessage: String? = null,
        newTime: String? = null,
        newIcon: String? = null
    ) {
        newTitle?.let { title = it }
        newMessage?.let { message = it }
        newTime?.let { time = it }
        newIcon?.let { icon = it }

        if (_binding != null) {
            updateContent()
        }
    }

    fun updateType(newType: StatusType) {
        cardType = newType
        if (_binding != null) {
            updateCardAppearance()
            updateContent()
        }
    }

    fun setOnClickListener(listener: View.OnClickListener) {
        statusCard.setOnClickListener(listener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}