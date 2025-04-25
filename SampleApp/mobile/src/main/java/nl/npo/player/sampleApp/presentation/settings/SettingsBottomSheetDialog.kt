package nl.npo.player.sampleApp.presentation.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import nl.npo.player.sampleApp.databinding.BottomSheetDialogSettingsBinding
import nl.npo.player.sampleApp.presentation.ext.supportsCasting
import nl.npo.player.sampleApp.shared.presentation.viewmodel.SettingsViewModel

@AndroidEntryPoint
class SettingsBottomSheetDialog : BottomSheetDialogFragment() {
    private lateinit var binding: BottomSheetDialogSettingsBinding

    private val viewModel by viewModels<SettingsViewModel>()

    private val adapter by lazy {
        SettingsAdapter(viewModel::handleSettingChange)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding =
            BottomSheetDialogSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        setupView()
        setupObservers()
    }

    private fun setupView() =
        with(binding) {
            root.adapter = adapter
        }

    private fun setupObservers() {
        viewModel.initSettingsList(requireContext().supportsCasting)
        viewModel.settingsList.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
        }
    }

    companion object {
        fun newInstance(): SettingsBottomSheetDialog =
            SettingsBottomSheetDialog().apply {
                arguments = bundleOf()
            }
    }
}
