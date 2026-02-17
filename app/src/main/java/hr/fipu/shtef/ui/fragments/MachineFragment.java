package hr.fipu.shtef.ui.fragments;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import hr.fipu.shtef.R;
import hr.fipu.shtef.data.local.entity.PartEntity;
import hr.fipu.shtef.domain.model.ServiceCenter;
import hr.fipu.shtef.ui.viewmodel.MachineViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MachineFragment extends Fragment {

    private MachineViewModel viewModel;
    private String machineId;
    private PartsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_machine, container, false);

        if (getArguments() != null) {
            machineId = getArguments().getString("machineId");
        }

        viewModel = new ViewModelProvider(this).get(MachineViewModel.class);

        Toolbar toolbar = root.findViewById(R.id.toolbar);
        CollapsingToolbarLayout collapsingToolbarLayout = root.findViewById(R.id.toolbar_layout);
        TextView tvName = root.findViewById(R.id.tv_machine_name);
        TextView tvBrand = root.findViewById(R.id.tv_machine_brand);
        Chip chipScore = root.findViewById(R.id.chip_repair_score);
        TextView tvDescription = root.findViewById(R.id.tv_description);
        RecyclerView rvParts = root.findViewById(R.id.rv_parts);
        FloatingActionButton fabFavorite = root.findViewById(R.id.fab_favorite);
        ImageView ivHeader = root.findViewById(R.id.iv_machine_header);

        // Set up toolbar back button
        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
        toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        rvParts.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PartsAdapter(new PartsAdapter.OnPartActionListener() {
            @Override
            public void onSupplierClick(String supplierName) {
                Bundle args = new Bundle();
                args.putString("supplierName", supplierName);
                Navigation.findNavController(root).navigate(R.id.navigation_map, args);
            }

            @Override
            public void onPartLongClick(PartEntity part) {
                // Fetch service centers to find the address
                viewModel.getServiceCenters(new Callback<List<ServiceCenter>>() {
                    @Override
                    public void onResponse(Call<List<ServiceCenter>> call, Response<List<ServiceCenter>> response) {
                        String address = "N/A";
                        if (response.isSuccessful() && response.body() != null) {
                            for (ServiceCenter center : response.body()) {
                                if (center.getName().equalsIgnoreCase(part.supplier)) {
                                    address = center.getAddress();
                                    break;
                                }
                            }
                        }
                        sharePartInfo(part, address);
                    }

                    @Override
                    public void onFailure(Call<List<ServiceCenter>> call, Throwable t) {
                        sharePartInfo(part, "N/A");
                    }
                });
            }
        });
        rvParts.setAdapter(adapter);

        if (machineId != null) {
            // Observe Machine Data
            viewModel.getMachineById(machineId).observe(getViewLifecycleOwner(), machine -> {
                if (machine != null) {
                    collapsingToolbarLayout.setTitle(machine.model);
                    tvName.setText(machine.model);
                    tvBrand.setText(machine.brand);
                    chipScore.setText(getString(R.string.repairability_index, String.valueOf(machine.repairabilityScore)));
                    tvDescription.setText(machine.description);

                    // Load machine image with Glide
                    if (machine.imageUrl != null && !machine.imageUrl.isEmpty()) {
                        Glide.with(this)
                                .load(machine.imageUrl)
                                .centerCrop()
                                .placeholder(R.drawable.ic_launcher_background)
                                .into(ivHeader);
                    }

                    // Update FAB icon and background color
                    if (machine.isFavorite) {
                        fabFavorite.setImageResource(android.R.drawable.btn_star_big_on);
                        fabFavorite.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.secondary)));
                    } else {
                        fabFavorite.setImageResource(android.R.drawable.btn_star_big_off);
                        fabFavorite.setBackgroundTintList(ColorStateList.valueOf(Color.LTGRAY));
                    }

                    fabFavorite.setOnClickListener(v -> viewModel.toggleFavorite(machine));
                } else {
                    collapsingToolbarLayout.setTitle(getString(R.string.machine_not_found));
                }
            });

            // Observe Parts Data
            viewModel.getPartsForMachine(machineId).observe(getViewLifecycleOwner(), parts -> {
                if (parts != null) {
                    adapter.setParts(parts);
                }
            });
        }

        return root;
    }

    private void sharePartInfo(PartEntity part, String address) {
        String priceText = String.format(Locale.GERMANY, "%.2f €", part.price);
        String shareText = getString(R.string.share_part_message, 
                                    part.name, 
                                    priceText, 
                                    part.supplier, 
                                    address);
        
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, getString(R.string.app_name)));
    }

    private static class PartsAdapter extends RecyclerView.Adapter<PartsAdapter.PartViewHolder> {
        private List<PartEntity> parts = new ArrayList<>();
        private final OnPartActionListener listener;

        interface OnPartActionListener {
            void onSupplierClick(String supplierName);
            void onPartLongClick(PartEntity part);
        }

        PartsAdapter(OnPartActionListener listener) {
            this.listener = listener;
        }

        void setParts(List<PartEntity> parts) {
            this.parts = parts;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public PartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_part, parent, false);
            return new PartViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PartViewHolder holder, int position) {
            PartEntity part = parts.get(position);
            holder.tvName.setText(part.name);
            holder.tvCategory.setText(part.category);
            holder.tvPrice.setText(String.format(Locale.GERMANY, "%.2f €", part.price));
            holder.tvSupplier.setText(part.supplier);
            
            // Short click for navigation
            holder.itemView.setOnClickListener(v -> listener.onSupplierClick(part.supplier));
            
            // Long click for Implicit Intent (Share)
            holder.itemView.setOnLongClickListener(v -> {
                listener.onPartLongClick(part);
                return true;
            });

            // Basic animation for items
            holder.itemView.setAlpha(0f);
            holder.itemView.animate().alpha(1f).setDuration(500).setStartDelay(position * 50L).start();
        }

        @Override
        public int getItemCount() {
            return parts.size();
        }

        static class PartViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvCategory, tvPrice, tvSupplier;
            PartViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tv_part_name);
                tvCategory = itemView.findViewById(R.id.tv_part_category);
                tvPrice = itemView.findViewById(R.id.tv_part_price);
                tvSupplier = itemView.findViewById(R.id.tv_part_supplier);
            }
        }
    }
}
