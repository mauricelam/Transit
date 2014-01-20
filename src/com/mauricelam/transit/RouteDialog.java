package com.mauricelam.transit;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.*;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import include.Helper;
import org.holoeverywhere.widget.Spinner;

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
        title.setText(route.getName().toUpperCase());
        title.setSelected(true);

        Button close = (Button) view.findViewById(R.id.close_button);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.alarm_time, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        final Spinner alarmSpinner = (Spinner) view.findViewById(R.id.alarmswitch);
        alarmSpinner.setAdapter(adapter);
        alarmSpinner.setOnItemSelectedListener(new org.holoeverywhere.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(org.holoeverywhere.widget.AdapterView<?> parent, View view, int pos, long id) {
                AlarmController controller = AlarmController.sharedInstance();
                if (pos > 0) {
                    int[] alarmTime = new int[]{0, 2, 5, 10};
                    boolean alarmSet = controller.setAlarm(route, alarmTime[pos]);
                    if (!alarmSet) {
                        final Alarm previousAlarm = controller.getAlarm(route);
                        Helper.setTimeout(new Runnable() {
                            @Override
                            public void run() {
                                setSpinnerValue(alarmSpinner, previousAlarm);
                            }
                        }, 100);
                    }
                } else {
                    controller.removeAlarm(route);
                }
            }

            @Override
            public void onNothingSelected(org.holoeverywhere.widget.AdapterView<?> parent) {
            }
        });
        Alarm alarm = AlarmController.sharedInstance().getAlarm(route);
        setSpinnerValue(alarmSpinner, alarm);

        RouteView routeview = (RouteView) view.findViewById(R.id.routeview);
        routeview.setColor(route.getColor());

        dialog.setContentView(view);
        dialog.setCanceledOnTouchOutside(true);

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        int dialogWidth = display.getWidth() - 80;
        dialog.getWindow().setLayout(dialogWidth, Math.min(500, display.getHeight() - 50));
        title.setMaxWidth(dialogWidth * 3 / 5);

        if (savedInstanceState != null)
            this.trip = savedInstanceState.getParcelable("trip");
        if (this.trip == null) {
            new RouteTask().execute();
        } else {
            loadTripInfo();
        }

        return dialog;
    }

    private void setSpinnerValue(Spinner spinner, Alarm alarm) {
        if (alarm == null) {
            spinner.setSelection(0);
        } else {
            int pos = 0;
            int timeAhead = alarm.getTimeAhead();
            if (timeAhead == 2) pos = 1;
            else if (timeAhead == 5) pos = 2;
            else if (timeAhead == 10) pos = 3;
            spinner.setSelection(pos);
        }
    }

    private void loadTripInfo () {
        TextView tagline = (TextView) view.findViewById(R.id.tagline);
        if (trip != null) {
            tagline.setText(trip.getHeadSign().toUpperCase());

            List<Stop> stops = trip.getStops();
            if (stops.size() > 0) {
                RouteView message = (RouteView) view.findViewById(R.id.routeview);
                StringBuilder sb = new StringBuilder();
                for (Stop stop : stops) {
                    sb.append(stop.getName()).append('\n');
                }
                sb.setLength(sb.length()-1);
                message.setText(sb.toString());
            }
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
            trip = Connector.getTripInfo(route.getTrip(), currentStop);
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
