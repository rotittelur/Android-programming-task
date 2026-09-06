package com.cbs.fuelest;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.cbs.fuelest.databinding.ItemFuelBinding;
import java.util.List;

public class FuelAdapter extends RecyclerView.Adapter<FuelAdapter.ViewHolder> {
    private final List<FuelItem> items;
    private final FuelViewModel viewModel;

    public FuelAdapter(List<FuelItem> items, FuelViewModel viewModel) {
        this.items = items;
        this.viewModel = viewModel;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFuelBinding binding = ItemFuelBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.binding.setFuel(items.get(position));
        holder.binding.setViewModel(viewModel);
        holder.binding.executePendingBindings();
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemFuelBinding binding;
        ViewHolder(ItemFuelBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}