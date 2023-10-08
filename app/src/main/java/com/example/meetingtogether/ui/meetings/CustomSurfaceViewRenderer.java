package com.example.meetingtogether.ui.meetings;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;

import org.webrtc.EglBase;
import org.webrtc.GlRectDrawer;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.ThreadUtils;

public class CustomSurfaceViewRenderer extends SurfaceViewRenderer {
    public SurfaceEglRenderer eglRenderer;
    public String resourceName = this.getResourceName();

    public CustomSurfaceViewRenderer(Context context) {
        super(context);
        this.eglRenderer = new SurfaceEglRenderer(this.resourceName);
    }

    public CustomSurfaceViewRenderer(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.eglRenderer = new SurfaceEglRenderer(this.resourceName);
    }

    public String getResourceName() {
        try {
            return this.getResources().getResourceEntryName(this.getId());
        } catch (Resources.NotFoundException var2) {
            return "";
        }
    }

    public void init(EglBase.Context sharedContext, RendererCommon.RendererEvents rendererEvents) {
        this.init(sharedContext, rendererEvents, EglBase.CONFIG_PLAIN, new GlRectDrawer());
    }

    public void init(EglBase.Context sharedContext, RendererCommon.RendererEvents rendererEvents, int[] configAttributes, RendererCommon.GlDrawer drawer) {
        super.init(sharedContext, rendererEvents, configAttributes, drawer);
        this.eglRenderer.init(sharedContext, this, configAttributes, drawer);
    }
}
