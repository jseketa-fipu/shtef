package hr.fipu.shtef.ui.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import hr.fipu.shtef.R;
import hr.fipu.shtef.data.local.entity.MachineEntity;
import hr.fipu.shtef.ui.viewmodel.MachineViewModel;

public class SearchFragment extends Fragment {

    private MachineViewModel viewModel;
    private TextInputEditText etBrand, etModel, etEan;
    private SearchResultsAdapter adapter;
    private TextView tvNoResults, tvResultsLabel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_search, container, false);

        viewModel = new ViewModelProvider(this).get(MachineViewModel.class);

        etBrand = root.findViewById(R.id.et_brand);
        etModel = root.findViewById(R.id.et_model);
        etEan = root.findViewById(R.id.et_ean);
        Button btnSearch = root.findViewById(R.id.btn_search);
        tvNoResults = root.findViewById(R.id.tv_no_results);
        tvResultsLabel = root.findViewById(R.id.tv_results_label);
        
        RecyclerView rvResults = root.findViewById(R.id.rv_search_results);
        rvResults.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SearchResultsAdapter(machineId -> {
            Bundle args = new Bundle();
            args.putString("machineId", machineId);
            Navigation.findNavController(root).navigate(R.id.navigation_machine, args);
        });
        rvResults.setAdapter(adapter);

        btnSearch.setOnClickListener(v -> performSearch(v));

        TextView.OnEditorActionListener searchListener = (v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || 
                (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                performSearch(v);
                return true;
            }
            return false;
        };

        etBrand.setOnEditorActionListener(searchListener);
        etModel.setOnEditorActionListener(searchListener);
        etEan.setOnEditorActionListener(searchListener);

        // Check if we should show all machines immediately
        if (getArguments() != null && getArguments().getBoolean("show_all", false)) {
            showAllMachines();
        }

        return root;
    }

    private void showAllMachines() {
        viewModel.getAllMachines().observe(getViewLifecycleOwner(), this::updateUI);
    }

    private void performSearch(View view) {
        String brand = etBrand.getText().toString().trim();
        String model = etModel.getText().toString().trim();
        String ean = etEan.getText().toString().trim();

        if (brand.equals("*") || model.equals("*") || ean.equals("*")) {
            showAllMachines();
            return;
        }

        if (!TextUtils.isEmpty(ean)) {
            viewModel.getMachineByEan(ean).observe(getViewLifecycleOwner(), machine -> {
                List<MachineEntity> list = new ArrayList<>();
                if (machine != null) list.add(machine);
                updateUI(list);
            });
        } else if (!TextUtils.isEmpty(brand) || !TextUtils.isEmpty(model)) {
            String query = TextUtils.isEmpty(brand) ? model : brand;
            viewModel.searchMachines(query).observe(getViewLifecycleOwner(), this::updateUI);
        } else {
            // If all fields are empty, show all machines instead of showing a toast
            showAllMachines();
        }
    }

    private void updateUI(List<MachineEntity> machines) {
        if (machines == null || machines.isEmpty()) {
            tvNoResults.setVisibility(View.VISIBLE);
            tvResultsLabel.setVisibility(View.GONE);
            adapter.setMachines(new ArrayList<>());
        } else {
            tvNoResults.setVisibility(View.GONE);
            tvResultsLabel.setVisibility(View.VISIBLE);
            adapter.setMachines(machines);
        }
    }

    private static class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.ViewHolder> {
        private List<MachineEntity> machines = new ArrayList<>();
        private final OnItemClickListener listener;

        interface OnItemClickListener {
            void onItemClick(String machineId);
        }

        SearchResultsAdapter(OnItemClickListener listener) {
            this.listener = listener;
        }

        void setMachines(List<MachineEntity> machines) {
            this.machines = machines;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_machine, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            MachineEntity machine = machines.get(position);
            holder.tvModel.setText(machine.model);
            holder.tvBrand.setText(machine.brand);
            holder.tvScore.setText(String.valueOf(machine.repairabilityScore));
            holder.itemView.setOnClickListener(v -> listener.onItemClick(machine.id));
        }

        @Override
        public int getItemCount() {
            return machines.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvModel, tvBrand, tvScore;
            ViewHolder(View itemView) {
                super(itemView);
                tvModel = itemView.findViewById(R.id.tv_machine_model);
                tvBrand = itemView.findViewById(R.id.tv_machine_brand);
                tvScore = itemView.findViewById(R.id.tv_machine_score);
            }
        }
    }
}
