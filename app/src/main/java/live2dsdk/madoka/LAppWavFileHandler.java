/*
 *
 *  * Copyright(c) Live2D Inc. All rights reserved.
 *  *
 *  * Use of this source code is governed by the Live2D Open Software license
 *  * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 *
 */

package live2dsdk.madoka;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;

public class LAppWavFileHandler extends Thread {
    public LAppWavFileHandler(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public void run() {
        loadWavFile();
    }

    public void loadWavFile() {
        byte[] voiceBuffer = LAppPal.loadFileAsBytes(filePath);

        int bufferSize = AudioTrack.getMinBufferSize(
            88187,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        );

        AudioTrack audioTrack = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            audioTrack = new AudioTrack.Builder()
                .setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build())
                .setAudioFormat(new AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(88187)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build())
                .setBufferSizeInBytes(bufferSize)
                .build();
            audioTrack.play();

            int offset = 100;
            audioTrack.write(voiceBuffer, offset, voiceBuffer.length - offset);
        }
    }

    private final String filePath;
}
