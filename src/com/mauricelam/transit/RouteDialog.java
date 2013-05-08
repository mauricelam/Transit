package com.mauricelam.transit;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.*;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.List;

/**
 * User: mauricelam
 * Date: 4/5/13
 * Time: 5:38 PM
 */
public class RouteDialog extends DialogFragment {

    private Route route;
    private Dialog dialog;
    private Trip trip;
    private View view;
    private Stop currentStop;

    public RouteDialog() {
        super();
    }

    public static RouteDialog newInstance(Route route, Stop currentStop) {
        RouteDialog dialog = new RouteDialog();
        Bundle args = new Bundle();
        args.putParcelable("route", route);
        args.putParcelable("stop", currentStop);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("trip", trip);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        this.route = this.getArguments().getParcelable("route");
        this.currentStop = this.getArguments().getParcelable("stop");

        Context context = new ContextThemeWrapper(getActivity(), R.style.HoloDialog);
        dialog = new Dialog(context, R.style.HoloDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.routedialog, null);

        TextView title = (TextView) view.findViewById(R.id.title);
        title.setText(route.getName());

        Button close = (Button) view.findViewById(R.id.close_button);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        CompoundButton alarm = (CompoundButton) view.findViewById(R.id.alarmswitch);
        alarm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) {
                    AlarmController.sharedInstance().addAlarm(route);
                } else {
                    AlarmController.sharedInstance().removeAlarm(route);
                }
            }
        });
        alarm.setChecked(AlarmController.sharedInstance().hasAlarm(route));
        alarm.setEnabled(route.getMins() > 5);

        RouteView routeview = (RouteView) view.findViewById(R.id.routeview);
        routeview.setColor(route.getColor());

        dialog.setContentView(view);
        dialog.setCanceledOnTouchOutside(true);

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        dialog.getWindow().setLayout(display.getWidth() - 80, Math.min(500, display.getHeight() - 50));

        if (savedInstanceState != null)
            this.trip = savedInstanceState.getParcelable("trip");
        if (this.trip == null) {
            new RouteTask().execute();
        } else {
            loadTripInfo();
        }

        return dialog;
    }

    private void loadTripInfo () {
        TextView tagline = (TextView) view.findViewById(R.id.tagline);
        if (trip == null) {
            return;
        }
        tagline.setText(trip.getHeadSign());

        List<Stop> stops = trip.getStops();
        if (stops.size() > 0) {
            TextView message = (TextView) view.findViewById(R.id.routeview);
            StringBuilder sb = new StringBuilder();
            for (Stop stop : stops) {
                sb.append(stop.getName()).append('\n');
            }
            sb.setLength(sb.length()-1);
            message.setText(sb.toString());
        }
        setLoading(false);
    }

    private void setLoading(boolean loading) {
        int loadingVisible = (loading) ? View.VISIBLE : View.GONE;
        int contentVisible = (loading) ? View.GONE : View.VISIBLE;
        view.findViewById(R.id.loader).setVisibility(loadingVisible);
        view.findViewById(R.id.routeview).setVisibility(contentVisible);
    }

    private class RouteTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            String escTripId = Uri.encode(route.getTrip());
            trip = Connector.getTripInfo(escTripId, currentStop);
            if (trip != null)
                trip.setRoute(route);
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            loadTripInfo();
        }
    }

}
