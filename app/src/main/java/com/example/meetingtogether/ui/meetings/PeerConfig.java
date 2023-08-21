package com.example.meetingtogether.ui.meetings;

public class PeerConfig {

    public static final String EXTRA_ROOMID = " com.example.meetingtogether.ROOMID";
    public static final String EXTRA_URLPARAMETERS = " com.example.meetingtogether.URLPARAMETERS";
    public static final String EXTRA_LOOPBACK = " com.example.meetingtogether.LOOPBACK";
    public static final String EXTRA_VIDEO_CALL = " com.example.meetingtogether.VIDEO_CALL";
    public static final String EXTRA_SCREENCAPTURE = " com.example.meetingtogether.SCREENCAPTURE";
    public static final String EXTRA_CAMERA2 = " com.example.meetingtogether.CAMERA2";
    public static final String EXTRA_VIDEO_WIDTH = " com.example.meetingtogether.VIDEO_WIDTH";
    public static final String EXTRA_VIDEO_HEIGHT = " com.example.meetingtogether.VIDEO_HEIGHT";
    public static final String EXTRA_VIDEO_FPS = " com.example.meetingtogether.VIDEO_FPS";
    public static final String EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED =
            "org.appsopt.apprtc.VIDEO_CAPTUREQUALITYSLIDER";
    public static final String EXTRA_VIDEO_BITRATE = " com.example.meetingtogether.VID`EO_BITRATE";
    public static final String EXTRA_VIDEOCODEC = " com.example.meetingtogether.VIDEOCODEC";
    public static final String EXTRA_HWCODEC_ENABLED = " com.example.meetingtogether.HWCODEC";
    public static final String EXTRA_CAPTURETOTEXTURE_ENABLED = " com.example.meetingtogether.CAPTURETOTEXTURE";
    public static final String EXTRA_FLEXFEC_ENABLED = " com.example.meetingtogether.FLEXFEC";
    public static final String EXTRA_AUDIO_BITRATE = " com.example.meetingtogether.AUDIO_BITRATE";
    public static final String EXTRA_AUDIOCODEC = " com.example.meetingtogether.AUDIOCODEC";
    public static final String EXTRA_NOAUDIOPROCESSING_ENABLED =
            " com.example.meetingtogether.NOAUDIOPROCESSING";
    public static final String EXTRA_AECDUMP_ENABLED = " com.example.meetingtogether.AECDUMP";
    public static final String EXTRA_SAVE_INPUT_AUDIO_TO_FILE_ENABLED =
            " com.example.meetingtogether.SAVE_INPUT_AUDIO_TO_FILE";
    public static final String EXTRA_OPENSLES_ENABLED = " com.example.meetingtogether.OPENSLES";
    public static final String EXTRA_DISABLE_BUILT_IN_AEC = " com.example.meetingtogether.DISABLE_BUILT_IN_AEC";
    public static final String EXTRA_DISABLE_BUILT_IN_AGC = " com.example.meetingtogether.DISABLE_BUILT_IN_AGC";
    public static final String EXTRA_DISABLE_BUILT_IN_NS = " com.example.meetingtogether.DISABLE_BUILT_IN_NS";
    public static final String EXTRA_DISABLE_WEBRTC_AGC_AND_HPF =
            " com.example.meetingtogether.DISABLE_WEBRTC_GAIN_CONTROL";
    public static final String EXTRA_DISPLAY_HUD = " com.example.meetingtogether.DISPLAY_HUD";
    public static final String EXTRA_TRACING = " com.example.meetingtogether.TRACING";
    public static final String EXTRA_CMDLINE = " com.example.meetingtogether.CMDLINE";
    public static final String EXTRA_RUNTIME = " com.example.meetingtogether.RUNTIME";
    public static final String EXTRA_VIDEO_FILE_AS_CAMERA = " com.example.meetingtogether.VIDEO_FILE_AS_CAMERA";
    public static final String EXTRA_SAVE_REMOTE_VIDEO_TO_FILE =
            " com.example.meetingtogether.SAVE_REMOTE_VIDEO_TO_FILE";
    public static final String EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH =
            " com.example.meetingtogether.SAVE_REMOTE_VIDEO_TO_FILE_WIDTH";
    public static final String EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT =
            " com.example.meetingtogether.SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT";
    public static final String EXTRA_USE_VALUES_FROM_INTENT =
            " com.example.meetingtogether.USE_VALUES_FROM_INTENT";
    public static final String EXTRA_DATA_CHANNEL_ENABLED = " com.example.meetingtogether.DATA_CHANNEL_ENABLED";
    public static final String EXTRA_ORDERED = " com.example.meetingtogether.ORDERED";
    public static final String EXTRA_MAX_RETRANSMITS_MS = " com.example.meetingtogether.MAX_RETRANSMITS_MS";
    public static final String EXTRA_MAX_RETRANSMITS = " com.example.meetingtogether.MAX_RETRANSMITS";
    public static final String EXTRA_PROTOCOL = " com.example.meetingtogether.PROTOCOL";
    public static final String EXTRA_NEGOTIATED = " com.example.meetingtogether.NEGOTIATED";
    public static final String EXTRA_ID = " com.example.meetingtogether.ID";
    public static final String EXTRA_ENABLE_RTCEVENTLOG = " com.example.meetingtogether.ENABLE_RTCEVENTLOG";


    public static final String VIDEO_TRACK_ID = "ARDAMSv0";
    public static final String AUDIO_TRACK_ID = "ARDAMSa0";
    public static final String VIDEO_TRACK_TYPE = "video";
    public static final String TAG = "TEST";
    //  public static final String TAG = "PCRTCClient";
    public static final String VIDEO_CODEC_VP8 = "VP8";
    public static final String VIDEO_CODEC_VP9 = "VP9";
    public static final String VIDEO_CODEC_H264 = "H264";
    public static final String VIDEO_CODEC_H264_BASELINE = "H264 Baseline";
    public static final String VIDEO_CODEC_H264_HIGH = "H264 High";
    public static final String VIDEO_CODEC_AV1 = "AV1";
    public static final String AUDIO_CODEC_OPUS = "opus";
    public static final String AUDIO_CODEC_ISAC = "ISAC";
    public static final String VIDEO_CODEC_PARAM_START_BITRATE = "x-google-start-bitrate";
    public static final String VIDEO_FLEXFEC_FIELDTRIAL =
            "WebRTC-FlexFEC-03-Advertised/Enabled/WebRTC-FlexFEC-03/Enabled/";
    public static final String DISABLE_WEBRTC_AGC_FIELDTRIAL =
            "WebRTC-Audio-MinimizeResamplingOnMobile/Enabled/";
    public static final String AUDIO_CODEC_PARAM_BITRATE = "maxaveragebitrate";
    public static final String AUDIO_ECHO_CANCELLATION_CONSTRAINT = "googEchoCancellation";
    public static final String AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT = "googAutoGainControl";
    public static final String AUDIO_HIGH_PASS_FILTER_CONSTRAINT = "googHighpassFilter";
    public static final String AUDIO_NOISE_SUPPRESSION_CONSTRAINT = "googNoiseSuppression";
    public static final String DTLS_SRTP_KEY_AGREEMENT_CONSTRAINT = "DtlsSrtpKeyAgreement";
    public static final int HD_VIDEO_WIDTH = 1280;
    public static final int HD_VIDEO_HEIGHT = 720;
    public static final int BPS_IN_KBPS = 1000;
    public static final String RTCEVENTLOG_OUTPUT_DIR_NAME = "rtc_event_log";
}
