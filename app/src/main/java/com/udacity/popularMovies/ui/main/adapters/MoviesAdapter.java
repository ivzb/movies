package com.udacity.popularMovies.ui.main.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.udacity.popularMovies.BR;
import com.udacity.popularMovies.data.network.model.MoviesResponse;
import com.udacity.popularMovies.databinding.MovieRecyclerItemBinding;
import com.udacity.popularMovies.ui.main.MainItemActionHandler;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.ViewHolder> {

    @Inject
    MainItemActionHandler mItemActionHandler;
    private List<MoviesResponse.Movie> mMovies;
    private Context mContext;

    public MoviesAdapter(List<MoviesResponse.Movie> movies) {
        this.mMovies = movies;
    }

    public Context getContext() {
        return this.mContext;
    }

    public void addAchievements(Collection<MoviesResponse.Movie> movies) {
        int start = this.getItemCount();
        this.mMovies.addAll(movies);
        this.notifyItemRangeInserted(start, movies.size());
    }

    @Override
    public MoviesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        this.mContext = parent.getContext();

        LayoutInflater layoutInflater = LayoutInflater.from(this.mContext);
        MovieRecyclerItemBinding binding = MovieRecyclerItemBinding.inflate(layoutInflater, parent, false);

        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        MoviesResponse.Movie movie = this.mMovies.get(position);
        viewHolder.setBinding(movie, this.mItemActionHandler);
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return this.mMovies.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private MovieRecyclerItemBinding binding;

        ViewHolder(MovieRecyclerItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        MovieRecyclerItemBinding getBinding() {
            return this.binding;
        }

        void setBinding(MoviesResponse.Movie movie, MainItemActionHandler itemActionHandler) {
            MovieRecyclerItemBinding binding = this.getBinding();

            binding.setVariable(BR.movie, movie);
            binding.setVariable(BR.itemActionHandler, itemActionHandler);
            binding.executePendingBindings();
        }
    }
}