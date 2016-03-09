package org.redcross.openmapkit.deployments;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONObject;
import org.redcross.openmapkit.R;

public class DeploymentsRecyclerAdapter extends RecyclerView.Adapter<DeploymentsRecyclerAdapter.DeploymentsViewHolder> {
    private Context context;

    public DeploymentsRecyclerAdapter(Context context) {
        this.context = context;
    }

    @Override
    public DeploymentsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_deployments, parent, false);
        return new DeploymentsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DeploymentsViewHolder holder, int position) {
        Deployment deployment = Deployments.singleton().get(position);
        String title = deployment.title();
        if (title == null) return;
        holder.nameTextView.setText(title);
        JSONObject manifest = deployment.json().optJSONObject("manifest");
        if (manifest == null) return;
        String description = manifest.optString("description");
        if (description == null) return;
        holder.descriptionTextView.setText(description);
    }

    @Override
    public int getItemCount() {
        return Deployments.singleton().size();
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
                    deploymentDetailsActivity.putExtra("POSITION", getLayoutPosition());
                    context.startActivity(deploymentDetailsActivity);
                }
            });
        }
    }
}
