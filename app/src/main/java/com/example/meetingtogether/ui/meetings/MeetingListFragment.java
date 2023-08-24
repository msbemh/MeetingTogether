package com.example.meetingtogether.ui.meetings;

import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_AECDUMP_ENABLED;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_AUDIOCODEC;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_AUDIO_BITRATE;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_CAMERA2;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_CAPTURETOTEXTURE_ENABLED;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_CMDLINE;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_DATA_CHANNEL_ENABLED;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_DISABLE_BUILT_IN_AEC;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_DISABLE_BUILT_IN_AGC;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_DISABLE_BUILT_IN_NS;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_DISABLE_WEBRTC_AGC_AND_HPF;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_DISPLAY_HUD;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_ENABLE_RTCEVENTLOG;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_FLEXFEC_ENABLED;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_HWCODEC_ENABLED;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_ID;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_LOOPBACK;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_MAX_RETRANSMITS;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_MAX_RETRANSMITS_MS;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_NEGOTIATED;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_NOAUDIOPROCESSING_ENABLED;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_OPENSLES_ENABLED;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_ORDERED;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_PROTOCOL;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_ROOMID;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_RUNTIME;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_SAVE_INPUT_AUDIO_TO_FILE_ENABLED;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_SCREENCAPTURE;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_TRACING;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_VIDEOCODEC;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_VIDEO_BITRATE;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_VIDEO_CALL;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_VIDEO_FILE_AS_CAMERA;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_VIDEO_FPS;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_VIDEO_HEIGHT;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_VIDEO_WIDTH;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.meetingtogether.R;
import com.example.meetingtogether.databinding.FragmentMeetinglistBinding;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.Random;

public class MeetingListFragment extends Fragment {

    private FragmentMeetinglistBinding binding;

    private MeetingListFragment.MyPagerAdapter mAdapter;
    private ViewPager2 mViewPager;
    private TabLayout mTabLayout;

    /**
     * Video/Audio 설정 값
     */
    private SharedPreferences sharedPref;
    private String keyprefResolution;
    private String keyprefFps;
    private String keyprefVideoBitrateType;
    private String keyprefVideoBitrateValue;
    private String keyprefAudioBitrateType;
    private String keyprefAudioBitrateValue;
    private String keyprefRoomServerUrl;
    private String keyprefRoom;
    private String keyprefRoomList;

    private static boolean commandLineRun;

    private static final String TAG = "TEST";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /**
         * 설정 Key 값들을 불러온다.
         */
        PreferenceManager.setDefaultValues(getActivity(), R.xml.preferences, false);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        keyprefResolution = getString(R.string.pref_resolution_key);
        keyprefFps = getString(R.string.pref_fps_key);
        keyprefVideoBitrateType = getString(R.string.pref_maxvideobitrate_key);
        keyprefVideoBitrateValue = getString(R.string.pref_maxvideobitratevalue_key);
        keyprefAudioBitrateType = getString(R.string.pref_startaudiobitrate_key);
        keyprefAudioBitrateValue = getString(R.string.pref_startaudiobitratevalue_key);
        keyprefRoomServerUrl = getString(R.string.pref_room_server_url_key);
        keyprefRoom = getString(R.string.pref_room_key);
        keyprefRoomList = getString(R.string.pref_room_list_key);

    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentMeetinglistBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        /**
         * ViewPager 에 어댑터 세팅
         */
        mViewPager = binding.viewPager;
        mAdapter = new MeetingListFragment.MyPagerAdapter(this);
        mViewPager.setAdapter(mAdapter);

        /**
         * TabLayout 을 ViewPager 와 연결
         */
        mTabLayout = binding.tabLayout;
        new TabLayoutMediator(mTabLayout, mViewPager, (tab, position) -> {
            String title = "";
            switch(position){
                case  0:
                    title = "public";
                    break;
                case 1:
                    title = "private";
                    break;
                case 2:
                    title = "예약";
                    break;
            }
            tab.setText(title);
        }).attach();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * Pager Adapter Class 선언
     */
    public class MyPagerAdapter extends FragmentStateAdapter {
        private static final int NUM_PAGES = 3;

        public MyPagerAdapter(Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            Fragment fragment = null;
            switch(position){
                case 0 :
                    fragment = new PublicFragment();
                    break;
                case 1 :
                    fragment = new PrivateFragment();
                    break;
                case 2 :
                    fragment = new ReserveFragment();
                    break;
            }
            return fragment;
        }

        @Override
        public int getItemCount() {
            return NUM_PAGES;
        }

    }

    /**
     * 회의 방으로 진입
     */
    @SuppressWarnings("StringSplitter")
    public void connectToRoom(String roomId, boolean commandLineRun, boolean loopback,
                               boolean useValuesFromIntent, int runTimeMs) {
        MeetingListFragment.commandLineRun = commandLineRun;

        // roomId is random for loopback.
        if (loopback) {
            roomId = Integer.toString((new Random()).nextInt(100000000));
        }

//        String roomUrl = sharedPref.getString(
//                keyprefRoomServerUrl, getString(R.string.pref_room_server_url_default));

//        String roomUrl = sharedPref.getString(
//                keyprefRoomServerUrl, getString(R.string.pref_room_server_url_custom));

        String roomUrl = getString(R.string.pref_room_server_url_custom);

        // Video call enabled flag.
        boolean videoCallEnabled = sharedPrefGetBoolean(R.string.pref_videocall_key,
                EXTRA_VIDEO_CALL, R.string.pref_videocall_default, useValuesFromIntent);

        // Use screencapture option.
        boolean useScreencapture = sharedPrefGetBoolean(R.string.pref_screencapture_key,
                EXTRA_SCREENCAPTURE, R.string.pref_screencapture_default, useValuesFromIntent);

        // Use Camera2 option.
        boolean useCamera2 = sharedPrefGetBoolean(R.string.pref_camera2_key, EXTRA_CAMERA2,
                R.string.pref_camera2_default, useValuesFromIntent);

        // Get default codecs.
        String videoCodec = sharedPrefGetString(R.string.pref_videocodec_key,
                EXTRA_VIDEOCODEC, R.string.pref_videocodec_default, useValuesFromIntent);
        String audioCodec = sharedPrefGetString(R.string.pref_audiocodec_key,
                EXTRA_AUDIOCODEC, R.string.pref_audiocodec_default, useValuesFromIntent);

        // Check HW codec flag.
        boolean hwCodec = sharedPrefGetBoolean(R.string.pref_hwcodec_key,
                EXTRA_HWCODEC_ENABLED, R.string.pref_hwcodec_default, useValuesFromIntent);

        // Check Capture to texture.
        boolean captureToTexture = sharedPrefGetBoolean(R.string.pref_capturetotexture_key,
                EXTRA_CAPTURETOTEXTURE_ENABLED, R.string.pref_capturetotexture_default,
                useValuesFromIntent);

        // Check FlexFEC.
        boolean flexfecEnabled = sharedPrefGetBoolean(R.string.pref_flexfec_key,
                EXTRA_FLEXFEC_ENABLED, R.string.pref_flexfec_default, useValuesFromIntent);

        // Check Disable Audio Processing flag.
        boolean noAudioProcessing = sharedPrefGetBoolean(R.string.pref_noaudioprocessing_key,
                EXTRA_NOAUDIOPROCESSING_ENABLED, R.string.pref_noaudioprocessing_default,
                useValuesFromIntent);

        boolean aecDump = sharedPrefGetBoolean(R.string.pref_aecdump_key,
                EXTRA_AECDUMP_ENABLED, R.string.pref_aecdump_default, useValuesFromIntent);

        boolean saveInputAudioToFile =
                sharedPrefGetBoolean(R.string.pref_enable_save_input_audio_to_file_key,
                        EXTRA_SAVE_INPUT_AUDIO_TO_FILE_ENABLED,
                        R.string.pref_enable_save_input_audio_to_file_default, useValuesFromIntent);

        // Check OpenSL ES enabled flag.
        boolean useOpenSLES = sharedPrefGetBoolean(R.string.pref_opensles_key,
                EXTRA_OPENSLES_ENABLED, R.string.pref_opensles_default, useValuesFromIntent);

        // Check Disable built-in AEC flag.
        boolean disableBuiltInAEC = sharedPrefGetBoolean(R.string.pref_disable_built_in_aec_key,
                EXTRA_DISABLE_BUILT_IN_AEC, R.string.pref_disable_built_in_aec_default,
                useValuesFromIntent);

        // Check Disable built-in AGC flag.
        boolean disableBuiltInAGC = sharedPrefGetBoolean(R.string.pref_disable_built_in_agc_key,
                EXTRA_DISABLE_BUILT_IN_AGC, R.string.pref_disable_built_in_agc_default,
                useValuesFromIntent);

        // Check Disable built-in NS flag.
        boolean disableBuiltInNS = sharedPrefGetBoolean(R.string.pref_disable_built_in_ns_key,
                EXTRA_DISABLE_BUILT_IN_NS, R.string.pref_disable_built_in_ns_default,
                useValuesFromIntent);

        // Check Disable gain control
        boolean disableWebRtcAGCAndHPF = sharedPrefGetBoolean(
                R.string.pref_disable_webrtc_agc_and_hpf_key, EXTRA_DISABLE_WEBRTC_AGC_AND_HPF,
                R.string.pref_disable_webrtc_agc_and_hpf_key, useValuesFromIntent);

        // Get video resolution from settings.
        int videoWidth = 0;
        int videoHeight = 0;
        if (useValuesFromIntent) {
            videoWidth = getActivity().getIntent().getIntExtra(EXTRA_VIDEO_WIDTH, 0);
            videoHeight = getActivity().getIntent().getIntExtra(EXTRA_VIDEO_HEIGHT, 0);
        }
        if (videoWidth == 0 && videoHeight == 0) {
            String resolution =
                    sharedPref.getString(keyprefResolution, getString(R.string.pref_resolution_default));
            String[] dimensions = resolution.split("[ x]+");
            if (dimensions.length == 2) {
                try {
                    videoWidth = Integer.parseInt(dimensions[0]);
                    videoHeight = Integer.parseInt(dimensions[1]);
                } catch (NumberFormatException e) {
                    videoWidth = 0;
                    videoHeight = 0;
                    Log.e(TAG, "Wrong video resolution setting: " + resolution);
                }
            }
        }

        // Get camera fps from settings.
        int cameraFps = 0;
        if (useValuesFromIntent) {
            cameraFps = getActivity().getIntent().getIntExtra(EXTRA_VIDEO_FPS, 0);
        }
        if (cameraFps == 0) {
            String fps = sharedPref.getString(keyprefFps, getString(R.string.pref_fps_default));
            String[] fpsValues = fps.split("[ x]+");
            if (fpsValues.length == 2) {
                try {
                    cameraFps = Integer.parseInt(fpsValues[0]);
                } catch (NumberFormatException e) {
                    cameraFps = 0;
                    Log.e(TAG, "Wrong camera fps setting: " + fps);
                }
            }
        }

        // Check capture quality slider flag.
        boolean captureQualitySlider = sharedPrefGetBoolean(R.string.pref_capturequalityslider_key,
                EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED,
                R.string.pref_capturequalityslider_default, useValuesFromIntent);

        // Get video and audio start bitrate.
        int videoStartBitrate = 0;
        if (useValuesFromIntent) {
            videoStartBitrate = getActivity().getIntent().getIntExtra(EXTRA_VIDEO_BITRATE, 0);
        }
        if (videoStartBitrate == 0) {
            String bitrateTypeDefault = getString(R.string.pref_maxvideobitrate_default);
            String bitrateType = sharedPref.getString(keyprefVideoBitrateType, bitrateTypeDefault);
            if (!bitrateType.equals(bitrateTypeDefault)) {
                String bitrateValue = sharedPref.getString(
                        keyprefVideoBitrateValue, getString(R.string.pref_maxvideobitratevalue_default));
                videoStartBitrate = Integer.parseInt(bitrateValue);
            }
        }

        int audioStartBitrate = 0;
        if (useValuesFromIntent) {
            audioStartBitrate = getActivity().getIntent().getIntExtra(EXTRA_AUDIO_BITRATE, 0);
        }
        if (audioStartBitrate == 0) {
            String bitrateTypeDefault = getString(R.string.pref_startaudiobitrate_default);
            String bitrateType = sharedPref.getString(keyprefAudioBitrateType, bitrateTypeDefault);
            if (!bitrateType.equals(bitrateTypeDefault)) {
                String bitrateValue = sharedPref.getString(
                        keyprefAudioBitrateValue, getString(R.string.pref_startaudiobitratevalue_default));
                audioStartBitrate = Integer.parseInt(bitrateValue);
            }
        }

        // Check statistics display option.
        boolean displayHud = sharedPrefGetBoolean(R.string.pref_displayhud_key,
                EXTRA_DISPLAY_HUD, R.string.pref_displayhud_default, useValuesFromIntent);

        boolean tracing = sharedPrefGetBoolean(R.string.pref_tracing_key, EXTRA_TRACING,
                R.string.pref_tracing_default, useValuesFromIntent);

        // Check Enable RtcEventLog.
        boolean rtcEventLogEnabled = sharedPrefGetBoolean(R.string.pref_enable_rtceventlog_key,
                EXTRA_ENABLE_RTCEVENTLOG, R.string.pref_enable_rtceventlog_default,
                useValuesFromIntent);

        // Get datachannel options
        boolean dataChannelEnabled = sharedPrefGetBoolean(R.string.pref_enable_datachannel_key,
                EXTRA_DATA_CHANNEL_ENABLED, R.string.pref_enable_datachannel_default,
                useValuesFromIntent);
        boolean ordered = sharedPrefGetBoolean(R.string.pref_ordered_key, EXTRA_ORDERED,
                R.string.pref_ordered_default, useValuesFromIntent);
        boolean negotiated = sharedPrefGetBoolean(R.string.pref_negotiated_key,
                EXTRA_NEGOTIATED, R.string.pref_negotiated_default, useValuesFromIntent);
        int maxRetrMs = sharedPrefGetInteger(R.string.pref_max_retransmit_time_ms_key,
                EXTRA_MAX_RETRANSMITS_MS, R.string.pref_max_retransmit_time_ms_default,
                useValuesFromIntent);
        int maxRetr =
                sharedPrefGetInteger(R.string.pref_max_retransmits_key, EXTRA_MAX_RETRANSMITS,
                        R.string.pref_max_retransmits_default, useValuesFromIntent);
        int id = sharedPrefGetInteger(R.string.pref_data_id_key, EXTRA_ID,
                R.string.pref_data_id_default, useValuesFromIntent);
        String protocol = sharedPrefGetString(R.string.pref_data_protocol_key,
                EXTRA_PROTOCOL, R.string.pref_data_protocol_default, useValuesFromIntent);

        // Start AppRTCMobile activity.
        Log.d(TAG, "Connecting to room " + roomId + " at URL " + roomUrl);
        if (validateUrl(roomUrl)) {
            Uri uri = Uri.parse(roomUrl);
            Intent intent = new Intent(getActivity(), MeetingRoomActivity.class);
            intent.setData(uri);
            intent.putExtra(EXTRA_ROOMID, roomId);
            intent.putExtra(EXTRA_LOOPBACK, loopback);
            intent.putExtra(EXTRA_VIDEO_CALL, videoCallEnabled);
            intent.putExtra(EXTRA_SCREENCAPTURE, useScreencapture);
            intent.putExtra(EXTRA_CAMERA2, useCamera2);
            intent.putExtra(EXTRA_VIDEO_WIDTH, videoWidth);
            intent.putExtra(EXTRA_VIDEO_HEIGHT, videoHeight);
            intent.putExtra(EXTRA_VIDEO_FPS, cameraFps);
            intent.putExtra(EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED, captureQualitySlider);
            intent.putExtra(EXTRA_VIDEO_BITRATE, videoStartBitrate);
            intent.putExtra(EXTRA_VIDEOCODEC, videoCodec);
            intent.putExtra(EXTRA_HWCODEC_ENABLED, hwCodec);
            intent.putExtra(EXTRA_CAPTURETOTEXTURE_ENABLED, captureToTexture);
            intent.putExtra(EXTRA_FLEXFEC_ENABLED, flexfecEnabled);
            intent.putExtra(EXTRA_NOAUDIOPROCESSING_ENABLED, noAudioProcessing);
            intent.putExtra(EXTRA_AECDUMP_ENABLED, aecDump);
            intent.putExtra(EXTRA_SAVE_INPUT_AUDIO_TO_FILE_ENABLED, saveInputAudioToFile);
            intent.putExtra(EXTRA_OPENSLES_ENABLED, useOpenSLES);
            intent.putExtra(EXTRA_DISABLE_BUILT_IN_AEC, disableBuiltInAEC);
            intent.putExtra(EXTRA_DISABLE_BUILT_IN_AGC, disableBuiltInAGC);
            intent.putExtra(EXTRA_DISABLE_BUILT_IN_NS, disableBuiltInNS);
            intent.putExtra(EXTRA_DISABLE_WEBRTC_AGC_AND_HPF, disableWebRtcAGCAndHPF);
            intent.putExtra(EXTRA_AUDIO_BITRATE, audioStartBitrate);
            intent.putExtra(EXTRA_AUDIOCODEC, audioCodec);
            intent.putExtra(EXTRA_DISPLAY_HUD, displayHud);
            intent.putExtra(EXTRA_TRACING, tracing);
            intent.putExtra(EXTRA_ENABLE_RTCEVENTLOG, rtcEventLogEnabled);
            intent.putExtra(EXTRA_CMDLINE, commandLineRun);
            intent.putExtra(EXTRA_RUNTIME, runTimeMs);
            intent.putExtra(EXTRA_DATA_CHANNEL_ENABLED, dataChannelEnabled);

            if (dataChannelEnabled) {
                intent.putExtra(EXTRA_ORDERED, ordered);
                intent.putExtra(EXTRA_MAX_RETRANSMITS_MS, maxRetrMs);
                intent.putExtra(EXTRA_MAX_RETRANSMITS, maxRetr);
                intent.putExtra(EXTRA_PROTOCOL, protocol);
                intent.putExtra(EXTRA_NEGOTIATED, negotiated);
                intent.putExtra(EXTRA_ID, id);
            }

            if (useValuesFromIntent) {
                if (getActivity().getIntent().hasExtra(EXTRA_VIDEO_FILE_AS_CAMERA)) {
                    String videoFileAsCamera =
                            getActivity().getIntent().getStringExtra(EXTRA_VIDEO_FILE_AS_CAMERA);
                    intent.putExtra(EXTRA_VIDEO_FILE_AS_CAMERA, videoFileAsCamera);
                }

                if (getActivity().getIntent().hasExtra(EXTRA_SAVE_REMOTE_VIDEO_TO_FILE)) {
                    String saveRemoteVideoToFile =
                            getActivity().getIntent().getStringExtra(EXTRA_SAVE_REMOTE_VIDEO_TO_FILE);
                    intent.putExtra(EXTRA_SAVE_REMOTE_VIDEO_TO_FILE, saveRemoteVideoToFile);
                }

                if (getActivity().getIntent().hasExtra(EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH)) {
                    int videoOutWidth =
                            getActivity().getIntent().getIntExtra(EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH, 0);
                    intent.putExtra(EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH, videoOutWidth);
                }

                if (getActivity().getIntent().hasExtra(EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT)) {
                    int videoOutHeight =
                            getActivity().getIntent().getIntExtra(EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT, 0);
                    intent.putExtra(EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT, videoOutHeight);
                }
            }

            startActivity(intent);
        }
    }

    /**
     * Get a value from the shared preference or from the intent, if it does not
     * exist the default is used.
     */
    @Nullable
    private String sharedPrefGetString(
            int attributeId, String intentName, int defaultId, boolean useFromIntent) {
        String defaultValue = getString(defaultId);
        if (useFromIntent) {
            String value = getActivity().getIntent().getStringExtra(intentName);
            if (value != null) {
                return value;
            }
            return defaultValue;
        } else {
            String attributeName = getString(attributeId);
            return sharedPref.getString(attributeName, defaultValue);
        }
    }

    /**
     * Get a value from the shared preference or from the intent, if it does not
     * exist the default is used.
     */
    private boolean sharedPrefGetBoolean(
            int attributeId, String intentName, int defaultId, boolean useFromIntent) {
        boolean defaultValue = Boolean.parseBoolean(getString(defaultId));
        if (useFromIntent) {
            return getActivity().getIntent().getBooleanExtra(intentName, defaultValue);
        } else {
            String attributeName = getString(attributeId);
            return sharedPref.getBoolean(attributeName, defaultValue);
        }
    }

    /**
     * Get a value from the shared preference or from the intent, if it does not
     * exist the default is used.
     */
    private int sharedPrefGetInteger(
            int attributeId, String intentName, int defaultId, boolean useFromIntent) {
        String defaultString = getString(defaultId);
        int defaultValue = Integer.parseInt(defaultString);
        if (useFromIntent) {
            return getActivity().getIntent().getIntExtra(intentName, defaultValue);
        } else {
            String attributeName = getString(attributeId);
            String value = sharedPref.getString(attributeName, defaultString);
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Wrong setting for: " + attributeName + ":" + value);
                return defaultValue;
            }
        }
    }

    private boolean validateUrl(String url) {
        if (URLUtil.isHttpsUrl(url) || URLUtil.isHttpUrl(url)) {
            return true;
        }

        new AlertDialog.Builder(getActivity())
                .setTitle(getText(R.string.invalid_url_title))
                .setMessage(getString(R.string.invalid_url_text, url))
                .setCancelable(false)
                .setNeutralButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                .create()
                .show();
        return false;
    }


}