package sg.com.commute_solutions.bustracker.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import sg.com.commute_solutions.bustracker.R;
import sg.com.commute_solutions.bustracker.data.Job;
import sg.com.commute_solutions.bustracker.data.RoutePoint;
import sg.com.commute_solutions.bustracker.databinding.RowAdapterLayoutBinding;
import sg.com.commute_solutions.bustracker.util.StringUtil;

public class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.ViewHolder> {

    private List<Job> jobData;
    private Context mCtx;
    private String currentJobName;
    private boolean isToday = true;
    private OnClickListener onClickListener;

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public String getCurrentJobName() {
        return currentJobName;
    }

    public void setCurrentJobName(String currentJobName) {
        this.currentJobName = currentJobName;
    }

    public boolean isToday() {
        return isToday;
    }

    public void setToday(boolean today) {
        isToday = today;
    }

    public interface OnClickListener {
        void onClick(int position, Job model);
    }
    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        RowAdapterLayoutBinding rowAdapterLayoutBinding;

        public ViewHolder(RowAdapterLayoutBinding binding) {
            super(binding.getRoot());
            // Define click listener for the ViewHolder's View

            rowAdapterLayoutBinding = binding;
        }

        public RowAdapterLayoutBinding getBinding() {
            return rowAdapterLayoutBinding;
        }
    }

    /**
     * Initialize the dataset of the Adapter
     *
     * @param jobs String[] containing the data to populate views to be used
     * by RecyclerView
     */
    public RouteAdapter(List<Job> jobs, Context ctx, String currentJob, boolean today) {
        jobData = jobs;
        mCtx = ctx;
        currentJobName = currentJob;
        isToday = today;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item

        return new ViewHolder(RowAdapterLayoutBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false));
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        Job job = jobData.get(position);
        viewHolder.getBinding().textViewRouteName.setText(job.getJobName());

        List<RoutePoint> jobRoutes = job.getRoutePoints();
        viewHolder.getBinding().textViewRouteStartTime.setText(jobRoutes.get(0).getTime());
        viewHolder.getBinding().textViewRouteStartLocation.setText(jobRoutes.get(0).getPointName());
        viewHolder.getBinding().textViewRouteEndTime.setText(jobRoutes.get(jobRoutes.size() - 1).getTime());
        viewHolder.getBinding().textViewRouteEndLocation.setText(jobRoutes.get(jobRoutes.size() - 1).getPointName());

        if (!StringUtil.deNull(currentJobName).isEmpty() && !StringUtil.deNull(job.getJobName()).isEmpty() && job.getJobName().equals(currentJobName)){
            viewHolder.getBinding().routeLayout.setBackground(AppCompatResources.getDrawable(mCtx, R.drawable.popover_background_dark));
        } else {
            viewHolder.getBinding().routeLayout.setBackground(AppCompatResources.getDrawable(mCtx, R.drawable.popover_background));
        }

        viewHolder.itemView.setOnClickListener(view -> {
            if (onClickListener != null) {
                onClickListener.onClick(position, jobData.get(position));
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return jobData.size();
    }

    public void setJobData(List<Job> jobData) {
        this.jobData = jobData;
        notifyDataSetChanged();
    }
}
