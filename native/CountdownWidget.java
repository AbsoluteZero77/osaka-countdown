package com.matthew.osakacountdown;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;

import java.util.Calendar;

public class CountdownWidget extends AppWidgetProvider {

    private static long target() {
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(2027, Calendar.FEBRUARY, 3, 0, 0, 0);
        return c.getTimeInMillis();
    }

    @Override
    public void onUpdate(Context ctx, AppWidgetManager mgr, int[] ids) {
        RemoteViews v = build(ctx);
        for (int id : ids) {
            mgr.updateAppWidget(id, v);
        }
        schedule(ctx);
    }

    @Override
    public void onEnabled(Context ctx) {
        schedule(ctx);
    }

    @Override
    public void onDisabled(Context ctx) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pending(ctx));
    }

    private static PendingIntent pending(Context ctx) {
        Intent i = new Intent(ctx, CountdownWidget.class);
        i.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = AppWidgetManager.getInstance(ctx)
                .getAppWidgetIds(new ComponentName(ctx, CountdownWidget.class));
        i.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        return PendingIntent.getBroadcast(ctx, 0, i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private static void schedule(Context ctx) {
        long now = System.currentTimeMillis();
        long ms = target() - now;
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (ms <= 0) {
            am.cancel(pending(ctx));
            return;
        }
        long next;
        if (ms > 86400000L) {
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DAY_OF_YEAR, 1);
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 5);
            c.set(Calendar.MILLISECOND, 0);
            next = c.getTimeInMillis();
            if (target() - next < 86400000L) {
                next = target() - 86400000L + 5000L;
            }
        } else {
            next = (now / 60000L + 1) * 60000L + 1000L;
        }
        am.set(AlarmManager.RTC, next, pending(ctx));
    }

    private static RemoteViews build(Context ctx) {
        long ms = target() - System.currentTimeMillis();
        RemoteViews v = new RemoteViews(ctx.getPackageName(), R.layout.widget_countdown);
        if (ms <= 0) {
            v.setTextViewText(R.id.num, "Today!");
            v.setTextViewText(R.id.unit, "Osaka time");
        } else if (ms > 86400000L) {
            long d = ms / 86400000L;
            v.setTextViewText(R.id.num, String.valueOf(d));
            v.setTextViewText(R.id.unit, d == 1 ? "day to go" : "days to go");
        } else {
            long h = ms / 3600000L;
            long m = (ms % 3600000L) / 60000L;
            v.setTextViewText(R.id.num, h + "h " + (m < 10 ? "0" : "") + m + "m");
            v.setTextViewText(R.id.unit, "to go");
        }
        v.setViewVisibility(R.id.unit, View.VISIBLE);
        Intent open = ctx.getPackageManager().getLaunchIntentForPackage(ctx.getPackageName());
        if (open != null) {
            PendingIntent pi = PendingIntent.getActivity(ctx, 0, open,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            v.setOnClickPendingIntent(R.id.root, pi);
        }
        return v;
    }
}
