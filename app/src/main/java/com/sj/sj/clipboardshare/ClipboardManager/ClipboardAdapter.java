package com.sj.sj.clipboardshare.ClipboardManager;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.sj.sj.clipboardshare.R;

import java.util.ArrayList;

public class ClipboardAdapter extends RecyclerView.Adapter<ClipboardAdapter.ViewHolder> {

    private static ClipboardAdapter instance;

    private transient Context context;
    private ArrayList<ClipObject> clipObjectList;

    public static ClipboardAdapter getInstance(Context context) {
        if (instance == null) {
            instance = new ClipboardAdapter(context.getApplicationContext());
        }
        return instance;
    }

    private ClipboardAdapter(Context context) {
        this.context = context;
        this.clipObjectList = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cardview, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final ClipObject item = clipObjectList.get(position);
        holder.title.setText(item.getString());
        holder.cardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, item.getString(), Toast.LENGTH_SHORT).show();
            }
        });
        final int finalPosition = holder.getAdapterPosition();
        holder.cardview.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder alt_bld = new AlertDialog.Builder(v.getContext());
                alt_bld.setMessage(context.getString(R.string.question_before_remove))
                        .setCancelable(true)
                        .setPositiveButton(context.getString(R.string.yes), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                remove(finalPosition);
                                notifyDataSetChanged();
                                Toast.makeText(context, context.getString(R.string.removed), Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton(context.getString(R.string.no), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = alt_bld.create();
                alert.show();

                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.clipObjectList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        CardView cardview;

        ViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            cardview = (CardView) itemView.findViewById(R.id.card_view);
        }
    }

    void add(String string) {
        clipObjectList.add(0, new ClipObject(string));
    }

    private void remove(int position) {
        clipObjectList.remove(position);
    }

    public void clear() {
        clipObjectList.clear();
    }

    public int getCount() {
        return clipObjectList.size();
    }

    public ClipObject getItem(int position) {
        return clipObjectList.get(position);
    }

    public boolean isEmpty() {
        return getCount() == 0;
    }

}
