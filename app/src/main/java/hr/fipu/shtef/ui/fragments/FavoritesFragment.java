package hr.fipu.shtef.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import hr.fipu.shtef.R;
import hr.fipu.shtef.data.local.entity.MachineEntity;
import hr.fipu.shtef.ui.viewmodel.MachineViewModel;

public class FavoritesFragment extends Fragment {

    private MachineViewModel viewModel;
    private FavoritesAdapter adapter;
    private TextView tvEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_favorites, container, false);

        viewModel = new ViewModelProvider(this).get(MachineViewModel.class);
        tvEmpty = root.findViewById(R.id.tv_empty_favorites);
        RecyclerView recyclerView = root.findViewById(R.id.rv_favorites);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new FavoritesAdapter(machineId -> {
            Bundle args = new Bundle();
            args.putString("machineId", machineId);
            Navigation.findNavController(root).navigate(R.id.navigation_machine, args);
        });
        recyclerView.setAdapter(adapter);

        viewModel.getFavoriteMachines().observe(getViewLifecycleOwner(), machines -> {
            if (machines == null || machines.isEmpty()) {
                tvEmpty.setVisibility(View.VISIBLE);
                adapter.setMachines(new ArrayList<>());
            } else {
                tvEmpty.setVisibility(View.GONE);
                adapter.setMachines(machines);
            }
        });

        return root;
    }

    private static class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder> {
        private List<MachineEntity> machines = new ArrayList<>();
        private final OnItemClickListener listener;

        interface OnItemClickListener {
            void onItemClick(String machineId);
        }

        FavoritesAdapter(OnItemClickListener listener) {
            this.listener = listener;
        }

        void setMachines(List<MachineEntity> machines) {
            this.machines = machines;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_machine, parent, false);
            return new FavoriteViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
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

        static class FavoriteViewHolder extends RecyclerView.ViewHolder {
            TextView tvModel, tvBrand, tvScore;

            FavoriteViewHolder(View itemView) {
                super(itemView);
                tvModel = itemView.findViewById(R.id.tv_machine_model);
                tvBrand = itemView.findViewById(R.id.tv_machine_brand);
                tvScore = itemView.findViewById(R.id.tv_machine_score);
            }
        }
    }
}
