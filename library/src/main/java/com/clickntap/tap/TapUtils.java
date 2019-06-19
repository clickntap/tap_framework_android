package com.clickntap.tap;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import org.json.JSONObject;


public class TapUtils {

    public static void log(String message) {
        Log.d("--tap--", message);
    }

    public static void log(Throwable e) {
        Log.e("--tap--", e.getMessage(), e);
    }

    public static void log(JSONObject env) {
        log(env.toString());
    }

    public static void fade(final View view, final float alpha, final int visibility, final TapCallback callback) {
        ((Activity) view.getContext()).runOnUiThread(new Thread(new Runnable() {
            public void run() {
                view.animate()
                        .setDuration(250)
                        .alpha(alpha)
                        .setListener(new AnimatorListenerAdapter() {
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                view.setVisibility(visibility);
                                if (callback != null) {
                                    callback.done();
                                }
                            }
                        });
            }
        }));
    }

    public static void move(final View view, final int x, final int y) {
        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity) view.getContext()).getWindowManager().getDefaultDisplay().getMetrics(metrics);
        final float newx = (x * metrics.density);
        final float newy = (y * metrics.density);
        ((Activity) view.getContext()).runOnUiThread(new Thread(new Runnable() {
            public void run() {
                view.animate()
                        .setDuration(500)
                        .translationX(newx)
                        .translationY(newy);
            }
        }));
    }

    public static void fadeIn(final View view, final TapCallback callback) {
        fade(view, 1.0f, View.VISIBLE, callback);
    }

    public static void fadeIn(final View view) {
        view.setVisibility(View.VISIBLE);
        fade(view, 1.0f, View.VISIBLE, null);
    }

    public static void fadeOut(final View view, final TapCallback callback) {
        fade(view, 0.0f, View.INVISIBLE, callback);
    }

    public static void fadeOut(final View view) {
        fade(view, 0.0f, View.INVISIBLE, null);
    }

    public static FrameLayout addFrame(Context context, ViewGroup view, View subview) {
        FrameLayout frameLayout = new FrameLayout(context);
        RelativeLayout relativeLayout = new RelativeLayout(context);
        relativeLayout.setGravity(Gravity.CENTER);
        relativeLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        relativeLayout.addView(subview);
        frameLayout.addView(relativeLayout);
        frameLayout.setClickable(true);
        frameLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        view.addView(frameLayout);
        return frameLayout;
    }

    public static int getDeviceWidth(Activity context) {
        DisplayMetrics metrics = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return (int) (metrics.widthPixels / metrics.density);
    }

    public static int getDeviceHeight(Activity context) {
        DisplayMetrics metrics = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return (int) (metrics.heightPixels / metrics.density);
    }

    public static void setMargin(ViewGroup viewGroup, int left, int top, int right, int bottom) {
        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity) viewGroup.getContext()).getWindowManager().getDefaultDisplay().getMetrics(metrics);
        left = (int) (left * metrics.density);
        top = (int) (top * metrics.density);
        right = (int) (right * metrics.density);
        bottom = (int) (bottom * metrics.density);
        if (viewGroup.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ((ViewGroup.MarginLayoutParams) viewGroup.getLayoutParams()).setMargins(left, top, right, bottom);
        }
        viewGroup.requestLayout();
    }

    public static void setOrigin(ViewGroup viewGroup, int x, int y) {
        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity) viewGroup.getContext()).getWindowManager().getDefaultDisplay().getMetrics(metrics);
        x = (int) (x * metrics.density);
        y = (int) (y * metrics.density);
        if (viewGroup.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ((ViewGroup.MarginLayoutParams) viewGroup.getLayoutParams()).leftMargin = x;
            ((ViewGroup.MarginLayoutParams) viewGroup.getLayoutParams()).topMargin = y;
        }
        viewGroup.requestLayout();
    }

    public static void setLeft(ViewGroup viewGroup, int left) {
        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity) viewGroup.getContext()).getWindowManager().getDefaultDisplay().getMetrics(metrics);
        left = (int) (left * metrics.density);
        if (viewGroup.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ((ViewGroup.MarginLayoutParams) viewGroup.getLayoutParams()).leftMargin = left;
        }
        viewGroup.requestLayout();
    }

    public static void setTop(ViewGroup viewGroup, int top) {
        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity) viewGroup.getContext()).getWindowManager().getDefaultDisplay().getMetrics(metrics);
        top = (int) (top * metrics.density);
        if (viewGroup.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ((ViewGroup.MarginLayoutParams) viewGroup.getLayoutParams()).topMargin = top;
        }
        viewGroup.requestLayout();
    }

    public static int getLeft(ViewGroup viewGroup) {
        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity) viewGroup.getContext()).getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int left = 0;
        if (viewGroup.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            left = ((ViewGroup.MarginLayoutParams) viewGroup.getLayoutParams()).leftMargin;
        }
        return (int) (left / metrics.density);
    }

    public static int getTop(ViewGroup viewGroup) {
        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity) viewGroup.getContext()).getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int top = 0;
        if (viewGroup.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            top = ((ViewGroup.MarginLayoutParams) viewGroup.getLayoutParams()).topMargin;
        }
        return (int) (top / metrics.density);
    }

    public static void setFrame(ViewGroup viewGroup, int x, int y, int w, int h) {
        setOrigin(viewGroup, x, y);
        setSize(viewGroup, w, h);
    }

    public static void setSize(ViewGroup viewGroup, int w, int h) {
        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity) viewGroup.getContext()).getWindowManager().getDefaultDisplay().getMetrics(metrics);
        ViewGroup.LayoutParams params = viewGroup.getLayoutParams();
        params.width = (int) (w * metrics.density);
        params.height = (int) (h * metrics.density);
        viewGroup.requestLayout();
    }

    public interface TapCallback {
        void done();
    }


}
