package com.hotmail.shaundalco.healthhub.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import androidx.core.view.ViewCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hotmail.shaundalco.healthhub.R

class AddBottomSheet : BottomSheetDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialog)

        val view = LayoutInflater.from(context).inflate(R.layout.sheet_add, null, false)
        dialog.setContentView(view)

        // Force half-expanded on show
        dialog.setOnShowListener {
            val bottomSheet =
                dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                    ?: return@setOnShowListener

            val behavior = BottomSheetBehavior.from(bottomSheet)
            behavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
            behavior.isFitToContents = false
            behavior.halfExpandedRatio = 0.55f // ~half screen
            behavior.skipCollapsed = false

        }

        // Hook up actions
        view.findViewById<Button>(R.id.btnLogExercise).setOnClickListener { dismiss() /* TODO */ }
        view.findViewById<Button>(R.id.btnLogWater).setOnClickListener { dismiss() /* TODO */ }
        view.findViewById<Button>(R.id.btnLogWeight).setOnClickListener { dismiss() /* TODO */ }

        // The tiles are Buttons too (or you can use FrameLayout)
        view.findViewById<View>(R.id.tileLogFood).setOnClickListener { dismiss() /* TODO */ }
        view.findViewById<View>(R.id.tileBarcode).setOnClickListener { dismiss() /* TODO */ }

        return dialog
    }
}
