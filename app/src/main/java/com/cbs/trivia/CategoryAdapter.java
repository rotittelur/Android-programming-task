package com.cbs.trivia;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.cbs.trivia.databinding.ItemCategoryCardBinding;
import com.cbs.trivia.model.Category;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter rendering selectable SideQuest domain category cards.
 */
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    public interface OnCategoryClickListener {
        void onCategorySelected(Category category);
    }

    private final ArrayList<Category> categories = new ArrayList<>();
    private final OnCategoryClickListener listener;
    private String selectedCategoryId;

    public CategoryAdapter(List<Category> initialCategories, String selectedCategoryId, OnCategoryClickListener listener) {
        if (initialCategories != null) {
            this.categories.addAll(initialCategories);
        }
        this.selectedCategoryId = selectedCategoryId;
        this.listener = listener;
    }

    public void submitCategories(List<Category> updatedCategories, String activeCategoryId) {
        categories.clear();
        if (updatedCategories != null) {
            categories.addAll(updatedCategories);
        }
        this.selectedCategoryId = activeCategoryId;
        notifyDataSetChanged();
    }

    public void setSelectedCategoryId(String newSelectedId) {
        if (newSelectedId == null || newSelectedId.equals(selectedCategoryId)) {
            return;
        }
        int previousIndex = findIndexById(selectedCategoryId);
        int nextIndex = findIndexById(newSelectedId);
        selectedCategoryId = newSelectedId;

        if (previousIndex >= 0) {
            notifyItemChanged(previousIndex);
        }
        if (nextIndex >= 0) {
            notifyItemChanged(nextIndex);
        }
    }

    private int findIndexById(String categoryId) {
        if (categoryId == null) {
            return -1;
        }
        for (int i = 0; i < categories.size(); i++) {
            if (categoryId.equals(categories.get(i).getId())) {
                return i;
            }
        }
        return -1;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemCategoryCardBinding binding = ItemCategoryCardBinding.inflate(inflater, parent, false);
        return new CategoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        boolean isSelected = category.getId().equals(selectedCategoryId);
        holder.bind(category, isSelected, listener);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {

        private final ItemCategoryCardBinding binding;

        CategoryViewHolder(ItemCategoryCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Category category, boolean selected, OnCategoryClickListener listener) {
            Context context = binding.getRoot().getContext();

            binding.tvCategoryName.setText(category.getName());
            binding.tvCategorySubtitle.setText(category.getSubtitle());
            binding.tvCategoryCount.setText(context.getString(R.string.category_count_format, category.getQuestionCount()));
            binding.ivCategoryIcon.setImageResource(resolveCategoryIcon(category.getId()));

            if (selected) {
                binding.cardCategoryRoot.setBackgroundResource(R.drawable.bg_category_selected);
                binding.tvCategoryCount.setBackgroundResource(R.drawable.bg_pill_cyan);
                binding.tvCategoryCount.setTextColor(ContextCompat.getColor(context, R.color.accent_cyan));
                ImageViewCompat.setImageTintList(
                        binding.ivCategoryIcon,
                        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.accent_cyan))
                );
            } else {
                binding.cardCategoryRoot.setBackgroundResource(R.drawable.bg_category_default);
                binding.tvCategoryCount.setBackgroundResource(R.drawable.bg_option_letter_badge);
                binding.tvCategoryCount.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
                ImageViewCompat.setImageTintList(
                        binding.ivCategoryIcon,
                        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.text_secondary))
                );
            }

            binding.cardCategoryRoot.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCategorySelected(category);
                }
            });
        }

        private static int resolveCategoryIcon(String categoryId) {
            if (Category.ID_COMPUTER_SCIENCE.equals(categoryId)) {
                return R.drawable.ic_category_cs;
            }
            if (Category.ID_ANDROID_DEV.equals(categoryId)) {
                return R.drawable.ic_category_android;
            }
            if (Category.ID_DFIR_CRYPTO.equals(categoryId)) {
                return R.drawable.ic_category_dfir;
            }
            if (Category.ID_LINUX_CLOUD.equals(categoryId)) {
                return R.drawable.ic_category_linux;
            }
            return R.drawable.ic_category_cyber;
        }
    }
}
