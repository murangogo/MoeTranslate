/*
 *
 *  * Copyright(c) Live2D Inc. All rights reserved.
 *  *
 *  * Use of this source code is governed by the Live2D Open Software license
 *  * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 *
 */

package live2dsdk.madoka;

import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Build;

import java.io.IOException;

public class LAppWavFileHandler extends Thread {
    public LAppWavFileHandler(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public void run() {
        loadWavFile();
    }

    public void loadWavFile() {
        // 対応していないAPI(API24未満)の場合は音声再生を行わない。

        MediaExtractor mediaExtractor = new MediaExtractor();
        try {
            AssetFileDescriptor afd = LAppDelegate.getInstance().getActivity().getAssets().openFd(filePath);
            mediaExtractor.setDataSource(afd);
        } catch (IOException e) {
            // 例外が発生したらエラーだけだして再生せずreturnする。
            e.printStackTrace();
            return;
        }

        MediaFormat mf = mediaExtractor.getTrackFormat(0);
        int samplingRate = mf.getInteger(MediaFormat.KEY_SAMPLE_RATE);

        int bufferSize = AudioTrack.getMinBufferSize(
                samplingRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
        );

        AudioTrack audioTrack;
        audioTrack = new AudioTrack.Builder()
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build())
                .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(samplingRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build())
                .setBufferSizeInBytes(bufferSize)
                .build();
        audioTrack.play();

        // ぶつぶつ音を回避
        int offset = 100;
        byte[] voiceBuffer = LAppPal.loadFileAsBytes(filePath);
        audioTrack.write(voiceBuffer, offset, voiceBuffer.length - offset);
    }

    private final String filePath;
}
