package nl.npo.player.sample_app.presentation.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import nl.npo.player.sample_app.databinding.FragmentSettingsBottomSheetDialogListDialogBinding

class SettingsBottomSheetDialog : BottomSheetDialogFragment() {

    private var _binding: FragmentSettingsBottomSheetDialogListDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            FragmentSettingsBottomSheetDialogListDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): SettingsBottomSheetDialog =
            SettingsBottomSheetDialog().apply {
                arguments = bundleOf()
            }
    }
}