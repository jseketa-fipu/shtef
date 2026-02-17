package hr.fipu.shtef.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import hr.fipu.shtef.R;
import hr.fipu.shtef.ui.activities.ScanActivity;
import hr.fipu.shtef.ui.viewmodel.MachineViewModel;

public class HomeFragment extends Fragment {

    private MachineViewModel viewModel;

    private final ActivityResultLauncher<Intent> scanLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    String scanResult = result.getData().getStringExtra("SCAN_RESULT");
                    if (scanResult != null) {
                        handleScanResult(scanResult);
                    }
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        viewModel = new ViewModelProvider(this).get(MachineViewModel.class);

        root.findViewById(R.id.btn_scan_qr).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ScanActivity.class);
            scanLauncher.launch(intent);
        });

        root.findViewById(R.id.btn_manual_search).setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.navigation_search);
        });

        // Language selectors
        root.findViewById(R.id.tv_lang_en).setOnClickListener(v -> setLocale("en"));
        root.findViewById(R.id.tv_lang_hr).setOnClickListener(v -> setLocale("hr"));

        return root;
    }

    private void setLocale(String languageCode) {
        LocaleListCompat appLocales = LocaleListCompat.forLanguageTags(languageCode);
        AppCompatDelegate.setApplicationLocales(appLocales);
    }

    private void handleScanResult(String scanResult) {
        // 1. Try to find by ID first
        viewModel.getMachineById(scanResult).observe(getViewLifecycleOwner(), machine -> {
            if (machine != null) {
                navigateToMachine(machine.id);
            } else {
                // 2. If not found by ID, try to find by EAN
                viewModel.getMachineByEan(scanResult).observe(getViewLifecycleOwner(), machineByEan -> {
                    if (machineByEan != null) {
                        navigateToMachine(machineByEan.id);
                    } else {
                        Toast.makeText(getContext(), getString(R.string.machine_not_found_code, scanResult), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void navigateToMachine(String machineId) {
        Bundle args = new Bundle();
        args.putString("machineId", machineId);
        Navigation.findNavController(requireView()).navigate(R.id.navigation_machine, args);
    }
}
