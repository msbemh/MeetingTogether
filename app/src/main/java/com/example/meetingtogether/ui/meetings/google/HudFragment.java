/*
 *  Copyright 2015 The WebRTC Project Authors. All rights reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package com.example.meetingtogether.ui.meetings.google;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.meetingtogether.R;

import org.webrtc.RTCStats;
import org.webrtc.RTCStatsReport;

/**
 * HUD 통계 디스플레이를 위한 프래그먼트
 *
 * "HUD"의 뜻은 "Head-Up Display"의 약자로,
 * 자동차나 비행기 등의 운송 수단에서 운전자나 조종사의 시야를 분산시키지 않고
 * 정보를 제공하기 위해 전면 유리 등의 투명한 표면에 정보를 투영하는 시스템을 의미합니다.
 */
public class HudFragment extends Fragment {
  private TextView statView;
  private ImageButton toggleDebugButton;
  private boolean displayHud;
  private volatile boolean isRunning;
  private CpuMonitor cpuMonitor;

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View controlView = inflater.inflate(R.layout.fragment_hud, container, false);

    // Create UI controls.
    statView = controlView.findViewById(R.id.hud_stat_call);
    toggleDebugButton = controlView.findViewById(R.id.button_toggle_debug);

    toggleDebugButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (displayHud) {
          statView.setVisibility(
              statView.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE);
        }
      }
    });

    return controlView;
  }

  @Override
  public void onStart() {
    super.onStart();

    Bundle args = getArguments();
    if (args != null) {
      displayHud = args.getBoolean(CallActivity.EXTRA_DISPLAY_HUD, false);
    }
    int visibility = displayHud ? View.VISIBLE : View.INVISIBLE;
    statView.setVisibility(View.INVISIBLE);
    toggleDebugButton.setVisibility(visibility);
    isRunning = true;
  }

  @Override
  public void onStop() {
    isRunning = false;
    super.onStop();
  }

  public void setCpuMonitor(CpuMonitor cpuMonitor) {
    this.cpuMonitor = cpuMonitor;
  }

  public void updateEncoderStatistics(final RTCStatsReport report) {
    if (!isRunning || !displayHud) {
      return;
    }

    StringBuilder sb = new StringBuilder();

    if (cpuMonitor != null) {
      sb.append("CPU%: ")
          .append(cpuMonitor.getCpuUsageCurrent())
          .append("/")
          .append(cpuMonitor.getCpuUsageAverage())
          .append(". Freq: ")
          .append(cpuMonitor.getFrequencyScaleAverage())
          .append("\n");
    }

    for (RTCStats stat : report.getStatsMap().values()) {
      sb.append(stat.toString()).append("\n");
    }

    statView.setText(sb.toString());
  }
}
