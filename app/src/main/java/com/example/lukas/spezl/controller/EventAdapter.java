package com.example.lukas.spezl.controller;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.lukas.spezl.model.Event;
import com.example.lukas.spezl.R;
import com.example.lukas.spezl.view.EventActivity;

import java.text.DateFormat;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventHolder> {

    private final String TAG_EVENT_ID = "TAG_EVENT_ID";
    private final String TAG_EVENT_NAME = "TAG_EVENT_NAME";
    private final String TAG_DESCRIPTION = "TAG_DESCRIPTION";
    private final String TAG_MAX_PARTICIPANTS = "TAG_PARTICIPANTS";
    private final String TAG_AMOUNT_PARTICIPANTS = "TAG_AMOUNT_PARTICIPANTS";
    private final String TAG_EVENT_TOWN = "TAG_EVENT_TOWN";
    private final String TAG_EVENT_ADDRESS = "TAG_EVENT_ADDRESS";
    private final String TAG_EVENT_CATEGORY = "TAG_EVENT_CATEGORY";
    private final String TAG_OWNER_ID = "TAG_OWNER_ID";

    private List<Event> eventList;

    private Context context;

    public class EventHolder extends RecyclerView.ViewHolder {
        public TextView nameView, descriptionView, datumView, townView, participantView;

        public RelativeLayout rootLayout;

        public EventHolder(View view) {
            super(view);

            this.nameView = (TextView) view.findViewById(R.id.eventName);
            this.descriptionView = (TextView) view.findViewById(R.id.eventDescription);
            this.datumView = (TextView) view.findViewById(R.id.eventDate);
            this.townView = (TextView) view.findViewById(R.id.eventTown);
            this.participantView = (TextView) view.findViewById(R.id.eventParticipant);

            this.rootLayout = (RelativeLayout) view.findViewById(R.id.rootLayout);
            rootLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(rootLayout.getContext(), EventActivity.class);

                    intent.putExtra(TAG_EVENT_ID, eventList.get(getAdapterPosition()).getuId());
                    intent.putExtra(TAG_EVENT_NAME, eventList.get(getAdapterPosition()).getName());
                    intent.putExtra(TAG_DESCRIPTION, eventList.get(getAdapterPosition()).getDescription());
                    intent.putExtra(TAG_MAX_PARTICIPANTS, eventList.get(getAdapterPosition()).getMaxParticipants());
                    intent.putExtra(TAG_EVENT_TOWN, eventList.get(getAdapterPosition()).getTown());
                    intent.putExtra(TAG_EVENT_ADDRESS, eventList.get(getAdapterPosition()).getAddress());
                    intent.putExtra(TAG_EVENT_CATEGORY, eventList.get(getAdapterPosition()).getCategory());
                    intent.putExtra(TAG_OWNER_ID, eventList.get(getAdapterPosition()).getOwnerId());

                    if (eventList.get(getAdapterPosition()).getParticipantIds() != null) {
                        intent.putExtra(TAG_AMOUNT_PARTICIPANTS, eventList.get(getAdapterPosition()).getParticipantIds().size());
                    } else {
                        intent.putExtra(TAG_AMOUNT_PARTICIPANTS, 0);
                    }

                    view.getContext().startActivity(intent);
                }
            });
        }
    }

    public EventAdapter(List<Event> moviesList, Context context) {
        this.eventList = moviesList;
        this.context = context;

    }

    @Override
    public EventHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_main_row, parent, false);
        return new EventHolder(itemView);
    }

    @Override
    public void onBindViewHolder(EventHolder holder, int position) {
        // The current event.
        Event event = eventList.get(position);

        // Check if participants already joined the
        int participants = 0;
        if (event.getParticipantIds() != null) {
            participants = event.getParticipantIds().size();
        }

        DateFormat dfDate = android.text.format.DateFormat.getDateFormat(context);
        DateFormat dfTime = android.text.format.DateFormat.getTimeFormat(context);

        holder.nameView.setText(event.getName());
        holder.descriptionView.setText(event.getDescription());
        holder.datumView.setText(dfDate.format(event.getDate()) + " " + dfTime.format(event.getDate()));
        holder.townView.setText(event.getTown());

        if (event.getMaxParticipants() == 0) {
            holder.participantView.setText(String.valueOf(participants));
        } else {
            holder.participantView.setText(String.valueOf(participants) + "/" + event.getMaxParticipants().intValue());
        }

    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }
}