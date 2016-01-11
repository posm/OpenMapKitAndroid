package org.redcross.openmapkit.deployments;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.redcross.openmapkit.R;

import java.util.List;

public class DeploymentsRecyclerAdapter extends RecyclerView.Adapter<DeploymentsRecyclerAdapter.DeploymentsViewHolder> {
    private List<Deployment> deploymentsList;
    private Context context;

    public DeploymentsRecyclerAdapter(Context context, List<Deployment> deploymentsList) {
        this.deploymentsList = deploymentsList;
        this.context = context;
    }

    @Override
    public DeploymentsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_deployments, parent, false);
        DeploymentsViewHolder viewHolder = new DeploymentsViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(DeploymentsViewHolder holder, int position) {
        Deployment deployment = deploymentsList.get(position);
        holder.nameTextView.setText(deployment.getName());
        holder.descriptionTextView.setText(deployment.getDescription());
    }

    @Override
    public int getItemCount() {
        return deploymentsList == null ? 0 : deploymentsList.size();
    }

    public class DeploymentsViewHolder extends RecyclerView.ViewHolder {
        protected TextView nameTextView;
        protected TextView descriptionTextView;

        public DeploymentsViewHolder(View itemView) {
            super(itemView);
            nameTextView = (TextView)itemView.findViewById(R.id.name);
            descriptionTextView = (TextView)itemView.findViewById(R.id.description);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent deploymentDetailsActivity = new Intent(context, DeploymentDetailsActivity.class);
                    context.startActivity(deploymentDetailsActivity);
                }
            });
        }
    }
}
